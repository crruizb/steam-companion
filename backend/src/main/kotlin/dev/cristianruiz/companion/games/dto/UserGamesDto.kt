package dev.cristianruiz.companion.games.dto

import java.time.Instant

data class UserGamesDto(
    val appId: Int,
    val playTimeForeverMinutes: Int,
    val name: String,
    val imgUrl: String?,
    val lastPlayedAt: Instant? = null
)