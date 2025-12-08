package dev.cristianruiz.companion.games

import dev.cristianruiz.companion.games.dto.UserGamesDto
import dev.cristianruiz.companion.user.entity.User
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/games")
class GamesController(
    private val gamesService: GamesService
) {

    @PostMapping("/import")
    fun importGames(authentication: Authentication): ResponseEntity<Void> {
        val user = authentication.principal as User
        gamesService.importGames(user)

        return ResponseEntity.accepted().build()
    }

    @GetMapping("/random")
    fun randomGame(authentication: Authentication): ResponseEntity<UserGamesDto> {
        val user = authentication.principal as User
        val randomGame = gamesService.getRandomGame(user)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(randomGame)
    }
}