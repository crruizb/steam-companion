package dev.cristianruiz.companion.achievements.dto

import java.sql.Date
import java.time.LocalDate

data class AchievementsHeatmap (
    val achievementsPerDate: Map<Int, List<AchievementsPerDate>>
)

data class AchievementsPerDate(
    val unlockDate: LocalDate,
    val count: Long,
)

data class AchievementsPerSqlDate(
    val unlockDate: Date,
    val count: Long,
)