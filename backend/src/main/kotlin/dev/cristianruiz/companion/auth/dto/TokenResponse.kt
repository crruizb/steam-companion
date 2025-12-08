package dev.cristianruiz.companion.auth.dto

data class TokenResponse (
    val accessToken: String,
    val refreshToken: String
)

data class RefreshTokenRequest (
    val refreshToken: String
)