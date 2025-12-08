package dev.cristianruiz.companion.auth

import dev.cristianruiz.companion.auth.dto.AuthResponse
import dev.cristianruiz.companion.auth.dto.RefreshTokenRequest
import dev.cristianruiz.companion.auth.dto.TokenResponse
import dev.cristianruiz.companion.user.UserService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView
import java.net.URLEncoder.encode

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val steamOpenIdService: SteamOpenIdService,
    private val jwtService: JwtService,
    private val userService: UserService,
) {

    @Value("\${app.frontend.url}")
    private lateinit var frontendUrl: String

    private val log = LoggerFactory.getLogger(AuthController::class.java)

    @GetMapping("/steam/login")
    fun steamLogin(): RedirectView {
        val authUrl = steamOpenIdService.generateAuthUrl()
        return RedirectView(authUrl)
    }

    @GetMapping("/steam/callback")
    fun steamCallback(@RequestParam params: Map<String, String>, response: HttpServletResponse): RedirectView {
        return try {
            val user = steamOpenIdService.verifyAndGetUser(params)
            if (user != null) {
                val accessToken = jwtService.generateAccessToken(user)
                val refreshToken = jwtService.generateRefreshToken(user)

                val accessCookie = Cookie("accessToken", accessToken).apply {
                    isHttpOnly = true
                    secure = true
                    maxAge = 60 * 60 * 24 // 24 hours
                    path = "/"
                }

                val refreshCookie = Cookie("refreshToken", refreshToken).apply {
                    isHttpOnly = true
                    secure = true
                    maxAge = 60 * 60 * 24 * 30
                    path = "/"
                }

                response.addCookie(accessCookie)
                response.addCookie(refreshCookie)

                val userJson = encode(
                    "{\"steamId\":\"${user.steamId}\",\"username\":\"${user.username}\",\"displayName\":\"${user.displayName}\",\"avatarUrl\":\"${user.avatarUrl}\",\"profileUrl\":\"${user.profileUrl}\"}",
                    "UTF-8"
                )
                val callbackUrl = "$frontendUrl/auth/callback?success=true&user=$userJson"
                RedirectView(callbackUrl)
            } else {
                val errorUrl = "$frontendUrl/auth/callback?success=false&error=Authentication failed"
                RedirectView(errorUrl)
            }
        } catch (e: Exception) {
            log.error("Steam authentication error", e)
            val errorUrl = "$frontendUrl/auth/callback?success=false&error=${e.message}"
            RedirectView(errorUrl)
        }
    }

    @PostMapping("/refresh")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<TokenResponse> {
        if (!jwtService.isRefreshToken(request.refreshToken)) {
            return ResponseEntity.badRequest().build()
        }

        val steamId = jwtService.extractSteamId(request.refreshToken)
        if (!jwtService.isTokenValid(request.refreshToken, steamId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val user = userService.findBySteamId(steamId) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val newAccessToken = jwtService.generateAccessToken(user)
        val newRefreshToken = jwtService.generateRefreshToken(user)

        jwtService.revokeRefreshToken(request.refreshToken)
        return ResponseEntity.ok(TokenResponse(newAccessToken, newRefreshToken))
    }

    @PostMapping("/logout")
    fun logout(@CookieValue("refreshToken", required = false) refreshToken: String?, response: HttpServletResponse): ResponseEntity<Void> {
        refreshToken?.let { token ->
            try {
                val steamId = jwtService.extractSteamId(token)
                jwtService.logoutBySteamId(steamId)
            } catch (e: Exception) {
                log.warn("Error during logout", e)
            }
        }

        val accessCookie = Cookie("accessToken", "").apply {
            isHttpOnly = true
            secure = true
            maxAge = 0
            path = "/"
        }
        val refreshCookie = Cookie("refreshToken", "").apply {
            isHttpOnly = true
            secure = true
            maxAge = 0
            path = "/"
        }

        response.addCookie(accessCookie)
        response.addCookie(refreshCookie)

        return ResponseEntity.noContent().build()
    }
}
