package dev.cristianruiz.companion.friends

import dev.cristianruiz.companion.achievements.AchievementSchemaService
import dev.cristianruiz.companion.achievements.GameAchievementsRepository
import dev.cristianruiz.companion.exceptions.BadRequestException
import dev.cristianruiz.companion.friends.dto.AchievementComparisonDto
import dev.cristianruiz.companion.friends.dto.AchievementComparisonRowDto
import dev.cristianruiz.companion.friends.dto.FriendDto
import dev.cristianruiz.companion.friends.dto.FriendGameDto
import dev.cristianruiz.companion.friends.dto.LibraryComparisonDto
import dev.cristianruiz.companion.friends.dto.SharedGameDto
import dev.cristianruiz.companion.games.GamesRepository
import dev.cristianruiz.companion.steam.Achievement
import dev.cristianruiz.companion.steam.SteamUserApiClient
import dev.cristianruiz.companion.user.entity.User
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.time.Instant

@Service
class FriendsService(
    private val steamUserApiClient: SteamUserApiClient,
    private val gamesRepository: GamesRepository,
    private val gameAchievementsRepository: GameAchievementsRepository,
    private val achievementSchemaService: AchievementSchemaService,
) {

    fun friends(user: User): List<FriendDto> {
        val friendSince = friendSince(user)
        // GetPlayerSummaries takes up to 100 ids per request
        return friendSince.keys.chunked(100)
            .flatMap { ids -> steamUserApiClient.getPlayerSummaries(ids).players }
            .map {
                FriendDto(
                    steamId = it.steamId,
                    displayName = it.personaName,
                    avatarUrl = it.avatarMedium,
                    profileUrl = it.profileUrl,
                    friendSince = friendSince[it.steamId]?.takeIf { since -> since > 0 }?.let(Instant::ofEpochSecond)
                )
            }
            .sortedBy { it.displayName.lowercase() }
    }

    /** The user's imported library against the friend's, read live from Steam. */
    fun compareLibraries(user: User, friendSteamId: String): LibraryComparisonDto {
        requireFriend(user, friendSteamId)
        val myGames = gamesRepository.findByUserId(user.id)
        if (myGames.isEmpty()) {
            throw BadRequestException("Import your games first, then you can compare libraries.")
        }
        val friendResult = steamUserApiClient.getOwnedGames(friendSteamId).response
        if (friendResult.games == null && friendResult.gameCount == null) {
            return LibraryComparisonDto(friendSteamId, false, myGames.size, 0, emptyList(), emptyList())
        }
        val friendGames = friendResult.games.orEmpty()
        val myGamesById = myGames.associateBy { it.id.appId }

        val shared = friendGames.mapNotNull { fg ->
            myGamesById[fg.appId]?.let { mine ->
                SharedGameDto(fg.appId, mine.name, mine.playTimeForeverMinutes, fg.playtimeForever)
            }
        }.sortedWith(compareByDescending<SharedGameDto> { it.myMinutes + it.friendMinutes }.thenBy { it.name })
        val friendOnly = friendGames
            .filter { it.appId !in myGamesById && it.playtimeForever > 0 }
            .sortedByDescending { it.playtimeForever }
            .take(10)
            .map { FriendGameDto(it.appId, it.name, it.playtimeForever) }

        return LibraryComparisonDto(
            friendSteamId = friendSteamId,
            friendLibraryPublic = true,
            myGameCount = myGames.size,
            friendGameCount = friendGames.size,
            sharedGames = shared,
            friendOnlyTopGames = friendOnly
        )
    }

    /** Both players' achievements for one game, read live from Steam, most common first. */
    fun compareAchievements(user: User, friendSteamId: String, appId: Int): AchievementComparisonDto {
        requireFriend(user, friendSteamId)
        val mine = try {
            steamUserApiClient.getPlayerAchievements(user.steamId, appId)?.playerStats?.achievements.orEmpty()
        } catch (e: HttpClientErrorException) {
            // Steam answers 400 for games without stats
            emptyList()
        }
        if (mine.isEmpty()) {
            throw BadRequestException("This game has no achievements on Steam.")
        }
        val theirs: List<Achievement>? = try {
            steamUserApiClient.getPlayerAchievements(friendSteamId, appId)?.playerStats?.achievements
        } catch (e: HttpClientErrorException) {
            // 403 when the friend's game details are private
            null
        }

        achievementSchemaService.refreshIfStale(appId)
        val details = gameAchievementsRepository.findByAppId(appId).associateBy { it.id.apiName }
        val myUnlocks = mine.unlockTimes()
        val friendUnlocks = theirs.orEmpty().unlockTimes()

        val rows = mine.map { achievement ->
            val detail = details[achievement.apiName]
            val anyUnlocked = achievement.apiName in myUnlocks || achievement.apiName in friendUnlocks
            AchievementComparisonRowDto(
                apiName = achievement.apiName,
                displayName = detail?.displayName ?: achievement.apiName,
                description = if (detail?.hidden == true && !anyUnlocked) null else detail?.description,
                iconUrl = if (anyUnlocked) detail?.iconUrl else detail?.iconGrayUrl ?: detail?.iconUrl,
                globalPercent = detail?.globalPercent,
                myUnlockTime = myUnlocks[achievement.apiName],
                friendUnlockTime = friendUnlocks[achievement.apiName]
            )
        }.sortedWith(compareByDescending<AchievementComparisonRowDto> { it.globalPercent ?: -1.0 }.thenBy { it.displayName })

        return AchievementComparisonDto(appId, friendAchievementsPublic = theirs != null, achievements = rows)
    }

    private fun List<Achievement>.unlockTimes(): Map<String, Instant> =
        filter { it.achieved == 1 }.associate { it.apiName to Instant.ofEpochSecond(it.unlockTime) }

    /** Steam id to friend-since time (Unix seconds) for each of the user's friends. */
    private fun friendSince(user: User): Map<String, Long> {
        val friends = try {
            steamUserApiClient.getFriendList(user.steamId)?.friendsList?.friends.orEmpty()
        } catch (e: HttpClientErrorException) {
            if (e.statusCode.value() == 401 || e.statusCode.value() == 403) {
                throw BadRequestException(
                    "Your Steam friends list is private. Set 'Friends List' to Public in your Steam privacy settings and try again."
                )
            }
            throw e
        }
        return friends.associate { it.steamId to it.friendSince }
    }

    // Comparisons only cover the user's own friends, not any Steam profile
    private fun requireFriend(user: User, friendSteamId: String) {
        if (friendSteamId !in friendSince(user)) {
            throw NoSuchElementException("That player isn't in your Steam friends list.")
        }
    }
}
