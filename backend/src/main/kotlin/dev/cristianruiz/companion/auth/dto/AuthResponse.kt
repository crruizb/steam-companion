package dev.cristianruiz.companion.auth.dto

import dev.cristianruiz.companion.user.dto.UserDto

data class AuthResponse(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val user: UserDto? = null,
    val error: String? = null
)