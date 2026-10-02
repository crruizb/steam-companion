package dev.cristianruiz.companion.friends

import dev.cristianruiz.companion.friends.dto.AchievementComparisonDto
import dev.cristianruiz.companion.friends.dto.FriendDto
import dev.cristianruiz.companion.friends.dto.LibraryComparisonDto
import dev.cristianruiz.companion.user.entity.User
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/friends")
class FriendsController(
    private val friendsService: FriendsService
) {

    @GetMapping
    fun friends(authentication: Authentication): ResponseEntity<List<FriendDto>> {
        val user = authentication.principal as User

        return ResponseEntity.ok(friendsService.friends(user))
    }

    @GetMapping("/{friendSteamId}/library")
    fun compareLibraries(
        authentication: Authentication,
        @PathVariable friendSteamId: String
    ): ResponseEntity<LibraryComparisonDto> {
        val user = authentication.principal as User

        return ResponseEntity.ok(friendsService.compareLibraries(user, friendSteamId))
    }

    @GetMapping("/{friendSteamId}/games/{appId}/achievements")
    fun compareAchievements(
        authentication: Authentication,
        @PathVariable friendSteamId: String,
        @PathVariable appId: Int
    ): ResponseEntity<AchievementComparisonDto> {
        val user = authentication.principal as User

        return ResponseEntity.ok(friendsService.compareAchievements(user, friendSteamId, appId))
    }
}
