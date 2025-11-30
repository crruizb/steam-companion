package dev.cristianruiz.companion.steam

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class SteamUserApi(
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

}

data class PlayerSummaryResponse(
    val response: PlayerSummaries
)

data class PlayerSummaries(
    val players: List<PlayerSummary>
)

data class PlayerSummary(
    @JsonProperty("steamid")
    val steamId: String,
    @JsonProperty("personaname")
    val personaName: String,
    @JsonProperty("profileurl")
    val profileUrl: String,
    val avatar: String,
    @JsonProperty("avatarmedium")
    val avatarMedium: String,
    @JsonProperty("avatarfull")
    val avatarFull: String
)