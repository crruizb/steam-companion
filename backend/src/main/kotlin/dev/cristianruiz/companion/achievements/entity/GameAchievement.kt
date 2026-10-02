package dev.cristianruiz.companion.achievements.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable
import java.time.Instant

/** One achievement of a game as Steam describes it, shared by every user who owns the game. */
@Entity
@Table(name = "game_achievements")
class GameAchievement(
    @EmbeddedId
    val id: GameAchievementId,

    @Column(name = "display_name")
    val displayName: String,

    @Column(name = "description")
    val description: String?,

    @Column(name = "icon_url")
    val iconUrl: String?,

    @Column(name = "icon_gray_url")
    val iconGrayUrl: String?,

    @Column(name = "hidden")
    val hidden: Boolean,

    // Share of all players who unlocked it, 0..100; null when Steam has no figure
    @Column(name = "global_percent")
    val globalPercent: Double?,

    @Column(name = "updated_at")
    val updatedAt: Instant = Instant.now()
)

@Embeddable
data class GameAchievementId(
    @Column(name = "app_id")
    val appId: Int,
    @Column(name = "api_name")
    val apiName: String
) : Serializable
