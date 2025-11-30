package dev.cristianruiz.companion.user

import dev.cristianruiz.companion.user.entity.User
import dev.cristianruiz.companion.user.dto.UserDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService
){

    @GetMapping("/me")
    fun getProfile(authentication: Authentication): ResponseEntity<UserDto> {
        val user = authentication.principal as User
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(user.toDto())
    }

    @GetMapping("/games")
    fun getProfileWithGames(authentication: Authentication): ResponseEntity<UserDto> {
        val user = authentication.principal as User
        val userDto = userService.findBySteamIdWithGames(user.steamId)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(userDto)
    }
}
