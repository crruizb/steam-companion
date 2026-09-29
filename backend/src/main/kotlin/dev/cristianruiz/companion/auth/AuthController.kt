package dev.cristianruiz.companion.auth

import dev.cristianruiz.companion.user.UserService
import dev.cristianruiz.companion.user.dto.UserDto
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView
import java.net.URLEncoder.encode
import java.nio.charset.StandardCharsets
import java.time.Duration

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val steamOpenIdService: SteamOpenIdService,
    private val jwtService: JwtService,
    private val userService: UserService,
) {

    @Value("\${app.frontend.url}")
    private lateinit var frontendUrl: String

    @Value("\${app.cookie.same-site:Lax}")
    private lateinit var cookieSameSite: String

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
                setAuthCookies(response, user)
                // The frontend loads the user from /api/user/me using the cookies set above
                RedirectView("$frontendUrl/auth/callback?success=true")
            } else {
                RedirectView(authErrorUrl())
            }
        } catch (e: Exception) {
            log.error("Steam authentication error", e)
            RedirectView(authErrorUrl())
        }
    }

    private fun authErrorUrl(): String {
        val error = encode("Authentication failed. Please try again.", StandardCharsets.UTF_8)
        return "$frontendUrl/auth/callback?success=false&error=$error"
    }

    @PostMapping("/refresh")
    fun refreshToken(
        @CookieValue(REFRESH_COOKIE, required = false) refreshToken: String?,
        response: HttpServletResponse
    ): ResponseEntity<Void> {
        val isValid = refreshToken != null && runCatching { jwtService.isRefreshTokenValid(refreshToken) }.getOrDefault(false)
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val user = userService.findBySteamId(jwtService.extractSteamId(refreshToken!!))
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        jwtService.revokeRefreshToken(refreshToken)
        setAuthCookies(response, user)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/logout")
    fun logout(@CookieValue(REFRESH_COOKIE, required = false) refreshToken: String?, response: HttpServletResponse): ResponseEntity<Void> {
        refreshToken?.let { token ->
            try {
                val steamId = jwtService.extractSteamId(token)
                jwtService.logoutBySteamId(steamId)
            } catch (e: Exception) {
                log.warn("Error during logout", e)
            }
        }

        addCookie(response, ACCESS_COOKIE, "", ACCESS_COOKIE_PATH, Duration.ZERO)
        addCookie(response, REFRESH_COOKIE, "", REFRESH_COOKIE_PATH, Duration.ZERO)

        return ResponseEntity.noContent().build()
    }

    private fun setAuthCookies(response: HttpServletResponse, user: UserDto) {
        addCookie(
            response, ACCESS_COOKIE, jwtService.generateAccessToken(user),
            ACCESS_COOKIE_PATH, jwtService.accessTokenExpiration
        )
        addCookie(
            response, REFRESH_COOKIE, jwtService.generateRefreshToken(user),
            REFRESH_COOKIE_PATH, jwtService.refreshTokenExpiration
        )
    }

    private fun addCookie(response: HttpServletResponse, name: String, value: String, path: String, maxAge: Duration) {
        val cookie = ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(true)
            .sameSite(cookieSameSite)
            .path(path)
            .maxAge(maxAge)
            .build()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    companion object {
        const val ACCESS_COOKIE = "accessToken"
        const val REFRESH_COOKIE = "refreshToken"
        private const val ACCESS_COOKIE_PATH = "/"
        private const val REFRESH_COOKIE_PATH = "/api/auth"
    }
}
