package dev.cristianruiz.companion.user.dto

import dev.cristianruiz.companion.games.dto.UserGamesDto
import dev.cristianruiz.companion.user.entity.User

data class UserDto(
    val id: Long?,
    val steamId: String,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val profileUrl: String,
    val ownedGames: Set<UserGamesDto>? = null
) {
    fun toUser(): User {
        return User(
            id = this.id ?: -1,
            steamId = this.steamId,
            username = this.username,
            displayName = this.displayName,
            avatarUrl = this.avatarUrl,
            profileUrl = this.profileUrl.ifEmpty { null },
        )
    }
}