package dev.cristianruiz.companion.games.dto

data class UserGamesDto(
    val appId: Int,
    val playTimeForeverMinutes: Int,
    val name: String,
    val imgUrl: String?
)