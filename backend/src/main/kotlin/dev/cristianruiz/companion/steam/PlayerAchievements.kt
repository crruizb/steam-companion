package dev.cristianruiz.companion.steam

import com.fasterxml.jackson.annotation.JsonProperty

data class PlayerAchievementsResponse(
    @JsonProperty("playerstats")
    val playerStats: PlayerStats
)

data class PlayerStats(
    val steamID: String,
    val gameName: String,
    // Absent for games without achievements
    val achievements: List<Achievement>? = null
)

data class Achievement(
    @JsonProperty("apiname")
    val apiName: String,
    val achieved: Int,
    @JsonProperty("unlocktime")
    val unlockTime: Long
)