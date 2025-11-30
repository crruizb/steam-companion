package dev.cristianruiz.companion.steam

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class SteamUserApiClient(
    private val restTemplate: RestTemplate
) {

    @Value("\${app.steam.apiKey}")
    private lateinit var apiKey: String

    private val steamApiUrl = "https://api.steampowered.com"

    fun getPlayerSummaries(steamIds: List<String>): PlayerSummaries {
        val idsParam = steamIds.joinToString(",")
        val url = "$steamApiUrl/ISteamUser/GetPlayerSummaries/v0002/?key=$apiKey&steamids=$idsParam"
        return restTemplate.getForObject(url, PlayerSummaryResponse::class.java)?.response ?: PlayerSummaries(emptyList())
    }

    fun getOwnedGames(steamId: String): PlayerOwnedGamesResponse {
        val url = "$steamApiUrl/IPlayerService/GetOwnedGames/v0001/?key=$apiKey&steamid=$steamId&include_appinfo=true"
        return restTemplate
            .getForObject(url, PlayerOwnedGamesResponse::class.java) ?:
            PlayerOwnedGamesResponse(PlayerOwnedGames(emptyList(), gameCount = 0))
    }

}
