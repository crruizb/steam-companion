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
import java.security.MessageDigest
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Date
import java.util.HexFormat
import java.util.UUID
import javax.crypto.SecretKey

@Service
open class JwtService(
    private val refreshTokenRepository: RefreshTokenRepository
) {

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.access-token.expiration:900000}")
    private var accessTokenExpirationInMs: Long = 900000

    @Value("\${app.jwt.refresh-token.expiration:2592000000}")
    private var refreshTokenExpirationInMs: Long = 2592000000

    private val ACCESS_TOKEN = "ACCESS"
    private val REFRESH_TOKEN = "REFRESH"

    val accessTokenExpiration: Duration
        get() = Duration.ofMillis(accessTokenExpirationInMs)

    val refreshTokenExpiration: Duration
        get() = Duration.ofMillis(refreshTokenExpirationInMs)

    fun generateAccessToken(user: UserDto): String {
        return generateToken(user, accessTokenExpirationInMs, ACCESS_TOKEN)
    }

    fun generateRefreshToken(user: UserDto): String {
        val token = generateToken(user, refreshTokenExpirationInMs, REFRESH_TOKEN)

        val refreshToken = RefreshToken(
            tokenHash = hashToken(token),
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
            .id(UUID.randomUUID().toString())
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

    /**
     * Validates a token used to call the API. Only access tokens are accepted:
     * refresh tokens live much longer and must only be usable at /api/auth/refresh.
     */
    fun isTokenValid(token: String, steamId: String): Boolean {
        return extractTokenType(token) == ACCESS_TOKEN &&
            extractSteamId(token) == steamId &&
            !isTokenExpired(token)
    }

    fun isRefreshTokenValid(token: String): Boolean {
        if (!isRefreshToken(token)) return false
        val storedToken = refreshTokenRepository.findByTokenHash(hashToken(token)) ?: return false
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

    // Only a SHA-256 hash is stored, so a leaked refresh_tokens table can't be used to log in
    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(token.toByteArray(Charsets.UTF_8))
        return HexFormat.of().formatHex(digest)
    }

    fun revokeRefreshToken(token: String) {
        refreshTokenRepository.findByTokenHash(hashToken(token))?.let { refreshToken ->
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
