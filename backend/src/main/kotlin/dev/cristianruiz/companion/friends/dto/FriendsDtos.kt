package dev.cristianruiz.companion.friends.dto

import java.time.Instant

data class FriendDto(
    val steamId: String,
    val displayName: String,
    val avatarUrl: String?,
    val profileUrl: String?,
    // Null for friendships older than Steam started recording it
    val friendSince: Instant?
)

data class LibraryComparisonDto(
    val friendSteamId: String,
    // False when the friend's game details are private on Steam; the lists are empty then
    val friendLibraryPublic: Boolean,
    val myGameCount: Int,
    val friendGameCount: Int,
    /** Games both own, most played together first */
    val sharedGames: List<SharedGameDto>,
    /** The friend's most played games that the user doesn't own */
    val friendOnlyTopGames: List<FriendGameDto>
)

data class SharedGameDto(
    val appId: Int,
    val name: String,
    val myMinutes: Int,
    val friendMinutes: Int
)

data class FriendGameDto(
    val appId: Int,
    val name: String,
    val friendMinutes: Int
)

data class AchievementComparisonDto(
    val appId: Int,
    // False when the friend's achievements for the game are private on Steam
    val friendAchievementsPublic: Boolean,
    val achievements: List<AchievementComparisonRowDto>
)

data class AchievementComparisonRowDto(
    val apiName: String,
    val displayName: String,
    // Null for hidden achievements neither of them has unlocked
    val description: String?,
    val iconUrl: String?,
    val globalPercent: Double?,
    val myUnlockTime: Instant?,
    val friendUnlockTime: Instant?
)
