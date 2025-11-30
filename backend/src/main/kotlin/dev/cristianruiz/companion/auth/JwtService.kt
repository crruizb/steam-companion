package dev.cristianruiz.companion.auth

import dev.cristianruiz.companion.user.dto.UserDto
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService {

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.expiration:86400000}")
    private var jwtExpirationInMs: Long = 86400000

    fun generateToken(user: UserDto): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationInMs)

        return Jwts.builder()
            .subject(user.steamId)
            .claim("userId", user.steamId)
            .claim("username", user.username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact()
    }

    fun extractSteamId(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }

    fun isTokenValid(token: String, steamId: String): Boolean {
        val extractedSteamId = extractSteamId(token)
        return extractedSteamId == steamId && !isTokenExpired(token)
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
}