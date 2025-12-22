package dev.cristianruiz.companion.steam

import com.fasterxml.jackson.annotation.JsonProperty

data class PlayerAchievementsResponse(
    @JsonProperty("playerstats")
    val playerStats: PlayerStats
)

data class PlayerStats(
    val steamID: String,
    val gameName: String,
    val achievements: List<Achievement>
)

data class Achievement(
    @JsonProperty("apiname")
    val apiName: String,
    val achieved: Int,
    @JsonProperty("unlocktime")
    val unlockTime: Long
)