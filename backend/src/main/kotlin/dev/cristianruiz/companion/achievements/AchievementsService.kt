package dev.cristianruiz.companion.achievements

import dev.cristianruiz.companion.achievements.dto.AchievementsHeatmap
import dev.cristianruiz.companion.achievements.dto.AchievementsPerDate
import dev.cristianruiz.companion.achievements.entity.Achievements
import dev.cristianruiz.companion.exceptions.BadRequestException
import dev.cristianruiz.companion.games.GamesRepository
import dev.cristianruiz.companion.games.GamesService
import dev.cristianruiz.companion.steam.SteamUserApiClient
import dev.cristianruiz.companion.user.entity.User
import jakarta.persistence.EntityManager
import jakarta.persistence.Tuple
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Calendar

@Service
class AchievementsService(
    private val steamUserApiClient: SteamUserApiClient,
    private val gamesRepository: GamesRepository,
    private val achievementsRepository: AchievementsRepository,
) {

    private val importScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val log = LoggerFactory.getLogger(GamesService::class.java)


     fun importAchievements(user: User) {
        val userGames = gamesRepository.findByUserId(user.id)
        if (userGames.isEmpty()) {
            throw BadRequestException("User has no games imported")
        }
        importScope.launch {
            supervisorScope {
                userGames.forEach { ug ->
                    launch {
                        try {
                            val achievementsResponse = steamUserApiClient.getPlayerAchievements(
                                steamId = user.steamId,
                                appId = ug.id.appId
                            )
                            val achievements = achievementsResponse?.playerStats?.achievements ?: return@launch
                            val achievementsEntity = achievements.mapNotNull {
                                if (it.achieved == 0) return@mapNotNull null // Skip unachieved achievements
                                Achievements(
                                    userId = user.id,
                                    appId = ug.id.appId,
                                    name = it.apiName,
                                    achieved = it.achieved == 1,
                                    unlockTime = Instant.ofEpochSecond(it.unlockTime)
                                        .atOffset(ZoneOffset.UTC)
                                )
                            }

                            achievementsRepository.saveAll(achievementsEntity)
                            delay(500L) // To avoid hitting Steam API rate limits
                        } catch (e: Exception) {
                            log.warn("Failed to import achievements for game: ${ug.name}, error: ${e.message}")
                        }
                    }
                }
            }
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