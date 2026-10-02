package dev.cristianruiz.companion.achievements.dto

import java.time.OffsetDateTime

data class GameAchievementsDto(
    val appId: Int,
    // False until an import has fetched the game's names, icons and rarity from Steam
    val hasDetails: Boolean,
    val achievements: List<GameAchievementDto>
)

data class GameAchievementDto(
    val apiName: String,
    val displayName: String,
    // Null for hidden achievements that are still locked
    val description: String?,
    val iconUrl: String?,
    val unlocked: Boolean,
    val unlockTime: OffsetDateTime?,
    val globalPercent: Double?
)

data class RareAchievementDto(
    val appId: Int,
    val gameName: String,
    val displayName: String,
    val description: String?,
    val iconUrl: String?,
    val globalPercent: Double?,
    val unlockTime: OffsetDateTime?
)
