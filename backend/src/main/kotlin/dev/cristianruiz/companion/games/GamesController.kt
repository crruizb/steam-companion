package dev.cristianruiz.companion.games

import dev.cristianruiz.companion.user.dto.UserDto
import dev.cristianruiz.companion.user.entity.User
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/games")
class GamesController(
    private val gamesService: GamesService
) {

    @PostMapping("/import")
    fun importGames(authentication: Authentication): ResponseEntity.BodyBuilder {
        val user = authentication.principal as User
        gamesService.importGames(user)

        return ResponseEntity.status(HttpStatus.ACCEPTED)
    }
}