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
    private val gamesRepository: GamesRepository
) {

    private val log = LoggerFactory.getLogger(GamesService::class.java)

    fun importGames(user: User): String {
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