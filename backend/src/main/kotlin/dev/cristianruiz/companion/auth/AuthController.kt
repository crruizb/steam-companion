package dev.cristianruiz.companion.auth

import dev.cristianruiz.companion.auth.dto.AuthResponse
import dev.cristianruiz.companion.auth.dto.RefreshTokenRequest
import dev.cristianruiz.companion.auth.dto.TokenResponse
import dev.cristianruiz.companion.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val steamOpenIdService: SteamOpenIdService,
    private val jwtService: JwtService,
    private val userService: UserService,
) {

    private val log = LoggerFactory.getLogger(AuthController::class.java)

    @GetMapping("/steam/login")
    fun steamLogin(): RedirectView {
        val authUrl = steamOpenIdService.generateAuthUrl()
        return RedirectView(authUrl)
    }

    @GetMapping("/steam/callback")
    fun steamCallback(@RequestParam params: Map<String, String>): ResponseEntity<AuthResponse> {
        return try {
            val user = steamOpenIdService.verifyAndGetUser(params)
            if (user != null) {
                val accessToken = jwtService.generateAccessToken(user)
                val refreshToken = jwtService.generateRefreshToken(user)
                ResponseEntity.ok(
                    AuthResponse(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        user = user.copy(ownedGames = null),
                    )
                )
            } else {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse(error = "Authentication failed"))
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthResponse(error = "Internal server error: ${e.message}"))
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

    @DeleteMapping("/logout")
    fun logout(@RequestBody request: RefreshTokenRequest): ResponseEntity<Void> {
        val steamId = jwtService.extractSteamId(request.refreshToken)
        jwtService.logoutBySteamId(steamId)
        return ResponseEntity.noContent().build()
    }
}
