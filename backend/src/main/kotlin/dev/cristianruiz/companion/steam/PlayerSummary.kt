package dev.cristianruiz.companion.steam

import com.fasterxml.jackson.annotation.JsonProperty

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