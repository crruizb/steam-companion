package dev.cristianruiz.companion.achievements

import dev.cristianruiz.companion.achievements.dto.AchievementsHeatmap
import dev.cristianruiz.companion.achievements.dto.AchievementsImportStatus
import dev.cristianruiz.companion.achievements.dto.AchievementsPerDate
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
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

@Service
class AchievementsService(
    private val steamUserApiClient: SteamUserApiClient,
    private val gamesRepository: GamesRepository,
    private val achievementsRepository: AchievementsRepository,
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
        val started = AchievementsImportStatus(ImportState.RUNNING, totalGames = userGames.size)
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
                userGames.forEach { ug ->
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
            val achievements = achievementsResponse?.playerStats?.achievements ?: return 0
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
            return achievementsEntity.size
        } catch (e: Exception) {
            log.warn("Failed to import achievements for game: $gameName, error: ${e.message}")
            return 0
        }
    }

    fun achievementsHeatmap(user: User): AchievementsHeatmap {
        val achievementsPerDate = achievementsRepository
            .getAchievementsGroupedByUnlockTime(user.id)
            .map { AchievementsPerDate(it.unlockDate.toLocalDate(), it.count) }
        val achievementsHeatmap = achievementsPerDate.groupBy { it.unlockDate.year }
        return AchievementsHeatmap(achievementsHeatmap)
    }
}