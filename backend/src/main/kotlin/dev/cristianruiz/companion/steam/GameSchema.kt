package dev.cristianruiz.companion.steam

import com.fasterxml.jackson.annotation.JsonProperty

// GetSchemaForGame: every achievement a game has, with its name and icons
data class GameSchemaResponse(
    val game: GameSchema? = null
)

data class GameSchema(
    val availableGameStats: AvailableGameStats? = null
)

data class AvailableGameStats(
    val achievements: List<SchemaAchievement>? = null
)

data class SchemaAchievement(
    val name: String,
    val displayName: String,
    // Steam sends 1 for achievements whose description is a secret until unlocked
    val hidden: Int = 0,
    // Absent for most hidden achievements
    val description: String? = null,
    val icon: String? = null,
    @JsonProperty("icongray")
    val iconGray: String? = null
)

// GetGlobalAchievementPercentagesForApp: share of all players who unlocked each achievement
data class GlobalAchievementPercentagesResponse(
    @JsonProperty("achievementpercentages")
    val achievementPercentages: GlobalAchievementPercentages? = null
)

data class GlobalAchievementPercentages(
    val achievements: List<GlobalAchievementPercentage>? = null
)

data class GlobalAchievementPercentage(
    val name: String,
    val percent: Double
)
