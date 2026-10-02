package dev.cristianruiz.companion.achievements.dto

enum class ImportState { IDLE, RUNNING, COMPLETED, FAILED }

data class AchievementsImportStatus(
    val state: ImportState,
    val processedGames: Int = 0,
    val totalGames: Int = 0,
    val importedAchievements: Int = 0,
) {
    companion object {
        val IDLE = AchievementsImportStatus(ImportState.IDLE)
    }
}
