package dev.cristianruiz.companion.achievements.dto

import java.time.OffsetDateTime

/** Per-game highlights of one year (UTC). Day-by-day numbers come from the heatmap. */
data class YearReviewDto(
    val year: Int,
    /** Games with the most achievements unlocked in the year */
    val topGames: List<YearGameDto>,
    /** The year's rarest unlocks */
    val rarest: List<RareAchievementDto>,
    /** Games whose last missing achievement was unlocked in the year */
    val perfected: List<PerfectedGameDto>
)

data class YearGameDto(
    val appId: Int,
    val name: String,
    val achievements: Long
)

data class PerfectedGameDto(
    val appId: Int,
    val name: String,
    val achievementsTotal: Int,
    val completedAt: OffsetDateTime
)
