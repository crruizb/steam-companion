package dev.cristianruiz.companion.games


import dev.cristianruiz.companion.achievements.AchievementsRepository
import dev.cristianruiz.companion.achievements.entity.Achievements
import dev.cristianruiz.companion.auth.AuthController
import dev.cristianruiz.companion.games.dto.UserGamesDto
import dev.cristianruiz.companion.games.entity.UserGames
import dev.cristianruiz.companion.games.entity.UserGamesId
import dev.cristianruiz.companion.steam.SteamUserApiClient
import dev.cristianruiz.companion.user.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.time.Instant

@Service
class GamesService(
    private val steamUserApiClient: SteamUserApiClient,
    private val gamesRepository: GamesRepository,
    private val achievementsRepository: AchievementsRepository
) {
    private val importScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val log = LoggerFactory.getLogger(GamesService::class.java)

    fun importGames(user: User): String {
        importScope.launch {
            val ownedGamesResponse = steamUserApiClient.getOwnedGames(user.steamId)
            val ownedGames = ownedGamesResponse.response.games
            val userGames = ownedGames.map { og ->
                UserGames(
                    id = UserGamesId(user.id, og.appId),
                    name = og.name,
                    playTimeForeverMinutes = og.playtimeForever,
                    imgUrl = "https://media.steampowered.com/steamcommunity/public/images/apps/${og.appId}/${og.imgIconUrl}.jpg",
                    user = user
                )
            }

            gamesRepository.saveAll(userGames)
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
        return "Game import started. You will be notified when it completes."
    }

    fun getRandomGame(user: User): UserGamesDto {
        val userGames = gamesRepository.findByUserId(user.id)
        if (userGames.isEmpty()) {
            throw NoSuchElementException("User has no games imported.")
        }
        return userGames
            .random()
            .toUserGamesDto()
    }
}