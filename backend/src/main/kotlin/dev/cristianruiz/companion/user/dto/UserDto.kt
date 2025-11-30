package dev.cristianruiz.companion.user.dto

import dev.cristianruiz.companion.user.entity.User

data class UserDto(
    val steamId: String,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val profileUrl: String
) {
    fun toUser(): User {
        return User(
            steamId = this.steamId,
            username = this.username,
            displayName = this.displayName,
            avatarUrl = this.avatarUrl,
            profileUrl = this.profileUrl.ifEmpty { null }
        )
    }
}