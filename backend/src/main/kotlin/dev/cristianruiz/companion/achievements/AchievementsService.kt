package dev.cristianruiz.companion.achievements

import dev.cristianruiz.companion.achievements.dto.AchievementsHeatmap
import dev.cristianruiz.companion.achievements.dto.AchievementsPerDate
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

@Service
class AchievementsService(
    private val steamUserApiClient: SteamUserApiClient,
    private val gamesRepository: GamesRepository,
    private val achievementsRepository: AchievementsRepository,
) {

    private val importScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val log = LoggerFactory.getLogger(AchievementsService::class.java)


    fun importAchievements(user: User) {
        val userGames = gamesRepository.findByUserId(user.id)
        if (userGames.isEmpty()) {
            throw BadRequestException("User has no games imported")
        }
        // Games are processed one at a time so the delay actually throttles requests to the Steam API
        importScope.launch {
            userGames.forEach { ug ->
                importGameAchievements(user, ug.id.appId, ug.name)
                delay(500L) // To avoid hitting Steam API rate limits
            }
        }
    }

    internal fun importGameAchievements(user: User, appId: Int, gameName: String) {
        try {
            val achievementsResponse = steamUserApiClient.getPlayerAchievements(
                steamId = user.steamId,
                appId = appId
            )
            val achievements = achievementsResponse?.playerStats?.achievements ?: return
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
        } catch (e: Exception) {
            log.warn("Failed to import achievements for game: $gameName, error: ${e.message}")
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