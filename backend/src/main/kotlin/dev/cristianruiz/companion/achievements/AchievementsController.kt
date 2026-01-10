package dev.cristianruiz.companion.achievements

import dev.cristianruiz.companion.achievements.dto.AchievementsHeatmap
import dev.cristianruiz.companion.user.entity.User
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/achievements")
class AchievementsController(
    private val achievementsService: AchievementsService
) {

    @PostMapping("/import")
    fun importAchievements(authentication: Authentication): ResponseEntity<Void> {
        val user = authentication.principal as User
        achievementsService.importAchievements(user)

        return ResponseEntity.accepted().build()
    }

    @GetMapping
    fun getAchievements(authentication: Authentication): ResponseEntity<AchievementsHeatmap> {
        println("test")
        val user = authentication.principal as User
        val heatmap = achievementsService.achievementsHeatmap(user)

        return ResponseEntity.ok(heatmap)
    }
}