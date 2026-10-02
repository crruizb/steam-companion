package dev.cristianruiz.companion.games.dto

import java.time.Instant

data class UserGamesDto(
    val appId: Int,
    val playTimeForeverMinutes: Int,
    val name: String,
    val imgUrl: String?,
    val lastPlayedAt: Instant? = null,
    // Null until an achievements import has covered the game
    val achievementsTotal: Int? = null,
    val achievementsUnlocked: Int? = null
)