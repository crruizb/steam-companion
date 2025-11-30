package dev.cristianruiz.companion.games


import dev.cristianruiz.companion.games.entity.UserGames
import dev.cristianruiz.companion.games.entity.UserGamesId
import dev.cristianruiz.companion.steam.SteamUserApiClient
import dev.cristianruiz.companion.user.entity.User
import org.springframework.stereotype.Service

@Service
class GamesService(
    private val steamUserApiClient: SteamUserApiClient,
    private val gamesRepository: GamesRepository
) {

    fun importGames(user: User) {
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
    }
}