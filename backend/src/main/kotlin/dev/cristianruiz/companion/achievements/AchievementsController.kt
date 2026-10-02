package dev.cristianruiz.companion.achievements

import dev.cristianruiz.companion.achievements.dto.AchievementsHeatmap
import dev.cristianruiz.companion.achievements.dto.AchievementsImportStatus
import dev.cristianruiz.companion.achievements.dto.GameAchievementsDto
import dev.cristianruiz.companion.achievements.dto.RareAchievementDto
import dev.cristianruiz.companion.achievements.dto.YearReviewDto
import dev.cristianruiz.companion.user.entity.User
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/achievements")
class AchievementsController(
    private val achievementsService: AchievementsService
) {

    @PostMapping("/import")
    fun importAchievements(authentication: Authentication): ResponseEntity<AchievementsImportStatus> {
        val user = authentication.principal as User
        val status = achievementsService.importAchievements(user)

        return ResponseEntity.accepted().body(status)
    }

    @GetMapping("/import/status")
    fun importStatus(authentication: Authentication): ResponseEntity<AchievementsImportStatus> {
        val user = authentication.principal as User

        return ResponseEntity.ok(achievementsService.importStatus(user))
    }

    @GetMapping("/games/{appId}")
    fun gameAchievements(authentication: Authentication, @PathVariable appId: Int): ResponseEntity<GameAchievementsDto> {
        val user = authentication.principal as User

        return ResponseEntity.ok(achievementsService.gameAchievements(user, appId))
    }

    @GetMapping("/rarest")
    fun rarestAchievements(authentication: Authentication): ResponseEntity<List<RareAchievementDto>> {
        val user = authentication.principal as User

        return ResponseEntity.ok(achievementsService.rarestAchievements(user))
    }

    @GetMapping("/review/{year}")
    fun yearReview(authentication: Authentication, @PathVariable year: Int): ResponseEntity<YearReviewDto> {
        val user = authentication.principal as User

        return ResponseEntity.ok(achievementsService.yearReview(user, year))
    }

    @GetMapping
    fun getAchievements(authentication: Authentication): ResponseEntity<AchievementsHeatmap> {
        val user = authentication.principal as User
        val heatmap = achievementsService.achievementsHeatmap(user)

        return ResponseEntity.ok(heatmap)
    }
}