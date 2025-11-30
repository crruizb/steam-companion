package dev.cristianruiz.companion.user

import dev.cristianruiz.companion.user.entity.User
import dev.cristianruiz.companion.user.dto.UserDto
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class UserController {

    @GetMapping("/profile")
    fun getProfile(authentication: Authentication): Map<String, Any?> {
        val user = authentication.principal as UserDto
        return mapOf(
            "steamId" to user.steamId,
            "username" to user.username,
            "displayName" to user.displayName,
            "avatarUrl" to user.avatarUrl,
            "profileUrl" to user.profileUrl
        )
    }
}