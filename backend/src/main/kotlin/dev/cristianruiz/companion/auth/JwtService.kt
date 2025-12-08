package dev.cristianruiz.companion.auth

import dev.cristianruiz.companion.auth.entity.RefreshToken
import dev.cristianruiz.companion.user.dto.UserDto
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Date
import javax.crypto.SecretKey

@Service
open class JwtService(
    private val refreshTokenRepository: RefreshTokenRepository
) {

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.access-token.expiration:86400000}")
    private var accessTokenExpirationInMs: Long = 86400000

    @Value("\${app.jwt.refresh-token.expiration:2592000000}")
    private var refreshTokenExpirationInMs: Long = 2592000000

    private val ACCESS_TOKEN = "ACCESS"
    private val REFRESH_TOKEN = "REFRESH"

    fun generateAccessToken(user: UserDto): String {
        return generateToken(user, accessTokenExpirationInMs, ACCESS_TOKEN)
    }

    fun generateRefreshToken(user: UserDto): String {
        val token = generateToken(user, refreshTokenExpirationInMs, REFRESH_TOKEN)

        val refreshToken = RefreshToken(
            token = token,
            steamId = user.steamId,
            expiryDate = Date(System.currentTimeMillis() + refreshTokenExpirationInMs).toInstant()
                .atOffset(java.time.ZoneOffset.UTC)
        )
        refreshTokenRepository.save(refreshToken)

        return token
    }

    fun generateToken(user: UserDto, expirationTime: Long, tokenType: String): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationTime)

        return Jwts.builder()
            .subject(user.steamId)
            .claim("userId", user.steamId)
            .claim("username", user.username)
            .claim("tokenType", tokenType)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact()
    }

    fun extractSteamId(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }

    fun extractTokenType(token: String): String {
        return extractClaim(token) { claims -> claims["tokenType"] as String }
    }

    fun isRefreshToken(token: String): Boolean {
        return extractTokenType(token) == REFRESH_TOKEN
    }

    fun isTokenValid(token: String, steamId: String): Boolean {
        val extractedSteamId = extractSteamId(token)
        val isRefreshToken = isRefreshToken(token)
        return if (isRefreshToken) {
            isRefreshTokenValid(token)
        } else {
            extractedSteamId == steamId && !isTokenExpired(token)
        }
    }

    fun isRefreshTokenValid(token: String): Boolean {
        val storedToken = refreshTokenRepository.findByToken(token) ?: return false
        if (storedToken.isRevoked) return false
        if (storedToken.expiryDate.isBefore(Date().toInstant().atOffset(ZoneOffset.UTC))) return false
        return true
    }

    private fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    private fun extractExpiration(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }

    private fun getSigningKey(): SecretKey {
        val keyBytes = Decoders.BASE64.decode(jwtSecret)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun revokeRefreshToken(token: String) {
        refreshTokenRepository.findByToken(token)?.let { refreshToken ->
            refreshToken.isRevoked = true
            refreshTokenRepository.save(refreshToken)
        }
    }

    @Transactional
    open fun logoutBySteamId(steamId: String) {
        refreshTokenRepository.logout(steamId)
    }

    @Scheduled(fixedRate = 3600000) // Run every hour
    fun cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(OffsetDateTime.now())
    }
}