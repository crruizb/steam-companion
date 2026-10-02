package dev.cristianruiz.companion.achievements

import dev.cristianruiz.companion.achievements.dto.AchievementsHeatmap
import dev.cristianruiz.companion.achievements.dto.AchievementsImportStatus
import dev.cristianruiz.companion.achievements.dto.AchievementsPerDate
import dev.cristianruiz.companion.achievements.dto.GameAchievementDto
import dev.cristianruiz.companion.achievements.dto.GameAchievementsDto
import dev.cristianruiz.companion.achievements.dto.RareAchievementDto
import dev.cristianruiz.companion.achievements.dto.YearReviewDto
import dev.cristianruiz.companion.achievements.dto.ImportState
import dev.cristianruiz.companion.achievements.entity.Achievements
import dev.cristianruiz.companion.exceptions.BadRequestException
import dev.cristianruiz.companion.games.GamesRepository
import dev.cristianruiz.companion.steam.SteamUserApiClient
import dev.cristianruiz.companion.user.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

@Service
class AchievementsService(
    private val steamUserApiClient: SteamUserApiClient,
    private val gamesRepository: GamesRepository,
    private val achievementsRepository: AchievementsRepository,
    private val gameAchievementsRepository: GameAchievementsRepository,
    private val achievementSchemaService: AchievementSchemaService,
) {

    private val importScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val log = LoggerFactory.getLogger(AchievementsService::class.java)

    // Progress of the latest import per user id, so the frontend can poll it. Kept in memory:
    // after a restart the status is IDLE again, which is fine since the import job is gone too
    private val importStatuses = ConcurrentHashMap<Long, AchievementsImportStatus>()

    fun importStatus(user: User): AchievementsImportStatus =
        importStatuses[user.id] ?: AchievementsImportStatus.IDLE

    /**
     * Starts importing achievements in the background and returns the initial status.
     * If an import is already running for the user, returns its status instead of starting another.
     */
    fun importAchievements(user: User): AchievementsImportStatus {
        val userGames = gamesRepository.findByUserId(user.id)
        if (userGames.isEmpty()) {
            throw BadRequestException("User has no games imported")
        }
        // A game never played can't have unlocks, and big libraries are mostly those: skip their requests
        val playedGames = userGames.filter { it.playTimeForeverMinutes > 0 }
        val started = AchievementsImportStatus(ImportState.RUNNING, totalGames = playedGames.size)
        // Atomic check-and-set, so two clicks can't start two imports
        val current = importStatuses.compute(user.id) { _, existing ->
            if (existing?.state == ImportState.RUNNING) existing else started
        }!!
        if (current !== started) {
            return current
        }

        // Games are processed one at a time so the delay actually throttles requests to the Steam API
        importScope.launch {
            var status = started
            try {
                playedGames.forEach { ug ->
                    val imported = importGameAchievements(user, ug.id.appId, ug.name)
                    status = status.copy(
                        processedGames = status.processedGames + 1,
                        importedAchievements = status.importedAchievements + imported,
                    )
                    importStatuses[user.id] = status
                    delay(500L.milliseconds) // To avoid hitting Steam API rate limits
                }
                importStatuses[user.id] = status.copy(state = ImportState.COMPLETED)
            } catch (e: Exception) {
                log.error("Achievements import failed for user ${user.id}", e)
                importStatuses[user.id] = status.copy(state = ImportState.FAILED)
            }
        }
        return started
    }

    /** Imports one game's newly unlocked achievements and returns how many were saved. */
    internal fun importGameAchievements(user: User, appId: Int, gameName: String): Int {
        try {
            val achievementsResponse = steamUserApiClient.getPlayerAchievements(
                steamId = user.steamId,
                appId = appId
            )
            val playerStats = achievementsResponse?.playerStats ?: return 0
            val achievements = playerStats.achievements.orEmpty()
            gamesRepository.updateAchievementProgress(
                userId = user.id,
                appId = appId,
                total = achievements.size,
                unlocked = achievements.count { it.achieved == 1 }
            )
            // Skip achievements imported previously, so re-imports only add newly unlocked ones
            val alreadyImported = achievementsRepository.findNamesByUserIdAndAppId(user.id, appId).toSet()
            val achievementsEntity = achievements
                .filter { it.achieved == 1 && it.apiName !in alreadyImported }
                .map {
                    Achievements(
                        userId = user.id,
                        appId = appId,
                        name = it.apiName,
                        achieved = true,
                        unlockTime = Instant.ofEpochSecond(it.unlockTime)
                            .atOffset(ZoneOffset.UTC)
                    )
                }

            if (achievementsEntity.isNotEmpty()) {
                achievementsRepository.saveAll(achievementsEntity)
            }
            // Details only matter once something is unlocked; otherwise the achievements dialog fetches them
            if (achievements.any { it.achieved == 1 }) {
                achievementSchemaService.refreshIfStale(appId)
            }
            return achievementsEntity.size
        } catch (e: Exception) {
            log.warn("Failed to import achievements for game: $gameName, error: ${e.message}")
            return 0
        }
    }

    /**
     * Every achievement of a game with the user's unlocks: unlocked ones newest first, then
     * locked ones from most to least common. Without stored details, only the unlocked ones by API name.
     */
    fun gameAchievements(user: User, appId: Int): GameAchievementsDto {
        // Imports skip games without unlocks, so their details may not be stored yet
        achievementSchemaService.refreshIfStale(appId)
        val unlockTimes = achievementsRepository.findByUserIdAndAppId(user.id, appId)
            .associate { it.name to it.unlockTime }
        val details = gameAchievementsRepository.findByAppId(appId)
        val detailNames = details.map { it.id.apiName }.toSet()

        val described = details.map {
            val unlocked = it.id.apiName in unlockTimes
            GameAchievementDto(
                apiName = it.id.apiName,
                displayName = it.displayName,
                description = if (it.hidden && !unlocked) null else it.description,
                iconUrl = if (unlocked) it.iconUrl else it.iconGrayUrl ?: it.iconUrl,
                unlocked = unlocked,
                unlockTime = unlockTimes[it.id.apiName],
                globalPercent = it.globalPercent
            )
        }
        // Unlocks the stored details don't know about (not fetched yet, or renamed on Steam)
        val undescribed = unlockTimes.filterKeys { it !in detailNames }.map { (name, unlockTime) ->
            GameAchievementDto(name, name, null, null, unlocked = true, unlockTime = unlockTime, globalPercent = null)
        }

        val (unlocked, locked) = (described + undescribed).partition { it.unlocked }
        return GameAchievementsDto(
            appId = appId,
            hasDetails = details.isNotEmpty(),
            achievements = unlocked.sortedByDescending { it.unlockTime } +
                    locked.sortedByDescending { it.globalPercent ?: -1.0 }
        )
    }

    fun rarestAchievements(user: User, limit: Int = 5): List<RareAchievementDto> =
        gameAchievementsRepository.findRarestUnlocked(user.id, PageRequest.of(0, limit))

    fun yearReview(user: User, year: Int): YearReviewDto {
        val from = OffsetDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        val to = from.plusYears(1)
        return YearReviewDto(
            year = year,
            topGames = gameAchievementsRepository.findTopGamesBetween(user.id, from, to, PageRequest.of(0, 5)),
            rarest = gameAchievementsRepository.findRarestUnlockedBetween(user.id, from, to, PageRequest.of(0, 3)),
            perfected = gameAchievementsRepository.findPerfectedBetween(user.id, from, to)
        )
    }

    fun achievementsHeatmap(user: User): AchievementsHeatmap {
        val achievementsPerDate = achievementsRepository
            .getAchievementsGroupedByUnlockTime(user.id)
            .map { AchievementsPerDate(it.unlockDate.toLocalDate(), it.count) }
        val achievementsHeatmap = achievementsPerDate.groupBy { it.unlockDate.year }
        return AchievementsHeatmap(achievementsHeatmap)
    }
}