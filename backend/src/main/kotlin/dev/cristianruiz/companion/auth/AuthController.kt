package dev.cristianruiz.companion.auth

import dev.cristianruiz.companion.auth.dto.AuthResponse
import dev.cristianruiz.companion.auth.JwtService
import dev.cristianruiz.companion.auth.SteamOpenIdService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val steamOpenIdService: SteamOpenIdService,
    private val jwtService: JwtService
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
                val token = jwtService.generateToken(user)
                ResponseEntity.ok(
                    AuthResponse(
                        token = token,
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
}
