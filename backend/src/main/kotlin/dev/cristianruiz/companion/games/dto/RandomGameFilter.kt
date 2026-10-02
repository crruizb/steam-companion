package dev.cristianruiz.companion.games.dto

import dev.cristianruiz.companion.games.entity.UserGames
import java.time.Duration
import java.time.Instant

/** Narrows the random game picker to a kind of game. */
enum class RandomGameFilter(val label: String) {
    ANY("Any game"),
    NEVER_PLAYED("Never played"),
    UNDER_TWO_HOURS("Under 2 hours"),
    NOT_PLAYED_IN_A_YEAR("Not played in a year"),
    ACHIEVEMENTS_LEFT("Achievements left");

    fun matches(game: UserGames, now: Instant): Boolean = when (this) {
        ANY -> true
        NEVER_PLAYED -> game.playTimeForeverMinutes == 0
        UNDER_TWO_HOURS -> game.playTimeForeverMinutes in 1 until 120
        // Played before, but not recently; games without a last played date can't qualify
        NOT_PLAYED_IN_A_YEAR -> game.lastPlayedAt?.isBefore(now.minus(Duration.ofDays(365))) == true
        ACHIEVEMENTS_LEFT -> (game.achievementsTotal ?: 0) > (game.achievementsUnlocked ?: 0)
    }
}
