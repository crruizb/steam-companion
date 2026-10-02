package dev.cristianruiz.companion.friends

import dev.cristianruiz.companion.achievements.AchievementSchemaService
import dev.cristianruiz.companion.achievements.GameAchievementsRepository
import dev.cristianruiz.companion.achievements.entity.GameAchievement
import dev.cristianruiz.companion.achievements.entity.GameAchievementId
import dev.cristianruiz.companion.exceptions.BadRequestException
import dev.cristianruiz.companion.friends.dto.FriendGameDto
import dev.cristianruiz.companion.friends.dto.SharedGameDto
import dev.cristianruiz.companion.games.GamesRepository
import dev.cristianruiz.companion.games.entity.UserGames
import dev.cristianruiz.companion.games.entity.UserGamesId
import dev.cristianruiz.companion.steam.Achievement
import dev.cristianruiz.companion.steam.FriendList
import dev.cristianruiz.companion.steam.FriendListResponse
import dev.cristianruiz.companion.steam.PlayerAchievementsResponse
import dev.cristianruiz.companion.steam.PlayerOwnedGame
import dev.cristianruiz.companion.steam.PlayerOwnedGames
import dev.cristianruiz.companion.steam.PlayerOwnedGamesResponse
import dev.cristianruiz.companion.steam.PlayerStats
import dev.cristianruiz.companion.steam.PlayerSummaries
import dev.cristianruiz.companion.steam.PlayerSummary
import dev.cristianruiz.companion.steam.SteamFriend
import dev.cristianruiz.companion.steam.SteamUserApiClient
import dev.cristianruiz.companion.user.entity.User
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class FriendsServiceTest {

    @MockK
    private lateinit var steamUserApiClient: SteamUserApiClient

    @MockK
    private lateinit var gamesRepository: GamesRepository

    @MockK
    private lateinit var gameAchievementsRepository: GameAchievementsRepository

    @MockK
    private lateinit var achievementSchemaService: AchievementSchemaService

    private lateinit var service: FriendsService

    private val user = User(
        id = 1,
        steamId = "111",
        username = "testuser",
        displayName = "Test User",
        avatarUrl = null,
        profileUrl = "http://profile.url"
    )
    private val friendId = "222"

    @BeforeEach
    fun setUp() {
        service = FriendsService(steamUserApiClient, gamesRepository, gameAchievementsRepository, achievementSchemaService)
        every { steamUserApiClient.getFriendList(user.steamId) } returns FriendListResponse(
            FriendList(listOf(SteamFriend(friendId, friendSince = 1600000000), SteamFriend("333", friendSince = 0)))
        )
        every { achievementSchemaService.refreshIfStale(any(), any()) } just runs
    }

    @Test
    fun `should list friends by name with their Steam profiles`() {
        // Given
        every { steamUserApiClient.getPlayerSummaries(listOf(friendId, "333")) } returns PlayerSummaries(
            listOf(summary(friendId, "zed"), summary("333", "Alice"))
        )

        // When
        val friends = service.friends(user)

        // Then
        assertEquals(listOf("Alice", "zed"), friends.map { it.displayName })
        assertEquals(null, friends[0].friendSince)
        assertEquals(Instant.ofEpochSecond(1600000000), friends[1].friendSince)
        assertEquals("avatar/$friendId", friends[1].avatarUrl)
    }

    @Test
    fun `should explain when the friends list is private`() {
        // Given
        every { steamUserApiClient.getFriendList(user.steamId) } throws HttpClientErrorException(HttpStatus.UNAUTHORIZED)

        // When / Then
        val error = assertFailsWith<BadRequestException> { service.friends(user) }
        assertEquals(true, error.message?.contains("friends list is private"))
    }

    @Test
    fun `should compare libraries and suggest the friend's games the user doesn't own`() {
        // Given
        every { gamesRepository.findByUserId(user.id) } returns listOf(
            userGame(570, "Dota 2", 100), userGame(730, "CS2", 10), userGame(10, "Only mine", 5)
        )
        every { steamUserApiClient.getOwnedGames(friendId) } returns PlayerOwnedGamesResponse(
            PlayerOwnedGames(
                games = listOf(
                    ownedGame(570, "Dota 2", 20), ownedGame(730, "CS2", 500),
                    ownedGame(20, "Their favourite", 900), ownedGame(30, "Never played", 0)
                ),
                gameCount = 4
            )
        )

        // When
        val comparison = service.compareLibraries(user, friendId)

        // Then
        assertEquals(true, comparison.friendLibraryPublic)
        assertEquals(3, comparison.myGameCount)
        assertEquals(4, comparison.friendGameCount)
        // Most played together first
        assertEquals(
            listOf(SharedGameDto(730, "CS2", 10, 500), SharedGameDto(570, "Dota 2", 100, 20)),
            comparison.sharedGames
        )
        assertEquals(listOf(FriendGameDto(20, "Their favourite", 900)), comparison.friendOnlyTopGames)
    }

    @Test
    fun `should report a private friend library`() {
        // Given
        every { gamesRepository.findByUserId(user.id) } returns listOf(userGame(570, "Dota 2", 100))
        every { steamUserApiClient.getOwnedGames(friendId) } returns PlayerOwnedGamesResponse(PlayerOwnedGames())

        // When
        val comparison = service.compareLibraries(user, friendId)

        // Then
        assertEquals(false, comparison.friendLibraryPublic)
        assertEquals(emptyList(), comparison.sharedGames)
    }

    @Test
    fun `should only compare with the user's own friends`() {
        assertFailsWith<NoSuchElementException> { service.compareLibraries(user, "999") }
        verify(exactly = 0) { steamUserApiClient.getOwnedGames(any()) }
    }

    @Test
    fun `should compare achievements of a game, most common first`() {
        // Given
        every { steamUserApiClient.getPlayerAchievements(user.steamId, 570) } returns achievements(
            Achievement("COMMON", 1, 1700000000), Achievement("RARE", 0, 0), Achievement("SECRET", 0, 0)
        )
        every { steamUserApiClient.getPlayerAchievements(friendId, 570) } returns achievements(
            Achievement("COMMON", 0, 0), Achievement("RARE", 1, 1710000000), Achievement("SECRET", 0, 0)
        )
        every { gameAchievementsRepository.findByAppId(570) } returns listOf(
            detail("COMMON", 80.0), detail("RARE", 2.0), detail("SECRET", 10.0, hidden = true)
        )

        // When
        val comparison = service.compareAchievements(user, friendId, 570)

        // Then
        assertEquals(true, comparison.friendAchievementsPublic)
        assertEquals(listOf("COMMON", "SECRET", "RARE"), comparison.achievements.map { it.apiName })
        val byName = comparison.achievements.associateBy { it.apiName }
        assertEquals(Instant.ofEpochSecond(1700000000), byName["COMMON"]?.myUnlockTime)
        assertEquals(null, byName["COMMON"]?.friendUnlockTime)
        assertEquals(Instant.ofEpochSecond(1710000000), byName["RARE"]?.friendUnlockTime)
        assertEquals("icon/RARE", byName["RARE"]?.iconUrl)
        // Nobody has it: gray icon, and a hidden one keeps its secret
        assertEquals("gray/SECRET", byName["SECRET"]?.iconUrl)
        assertEquals(null, byName["SECRET"]?.description)
    }

    @Test
    fun `should still show the user's achievements when the friend's are private`() {
        // Given
        every { steamUserApiClient.getPlayerAchievements(user.steamId, 570) } returns achievements(
            Achievement("COMMON", 1, 1700000000)
        )
        every { steamUserApiClient.getPlayerAchievements(friendId, 570) } throws HttpClientErrorException(HttpStatus.FORBIDDEN)
        every { gameAchievementsRepository.findByAppId(570) } returns emptyList()

        // When
        val comparison = service.compareAchievements(user, friendId, 570)

        // Then
        assertEquals(false, comparison.friendAchievementsPublic)
        assertEquals(listOf("COMMON"), comparison.achievements.map { it.displayName })
    }

    @Test
    fun `should reject games without achievements`() {
        // Given: Steam answers 400 for games without stats
        every { steamUserApiClient.getPlayerAchievements(user.steamId, 570) } throws HttpClientErrorException(HttpStatus.BAD_REQUEST)

        // When / Then
        assertFailsWith<BadRequestException> { service.compareAchievements(user, friendId, 570) }
    }

    private fun summary(steamId: String, name: String) = PlayerSummary(
        steamId = steamId, personaName = name, profileUrl = "profile/$steamId",
        avatar = "small/$steamId", avatarMedium = "avatar/$steamId", avatarFull = "full/$steamId"
    )

    private fun userGame(appId: Int, name: String, minutes: Int) = UserGames(
        id = UserGamesId(user.id, appId), user = user, name = name, playTimeForeverMinutes = minutes, imgUrl = null
    )

    private fun ownedGame(appId: Int, name: String, minutes: Int) =
        PlayerOwnedGame(appId = appId, name = name, playtimeForever = minutes, imgIconUrl = "")

    private fun achievements(vararg achievements: Achievement) =
        PlayerAchievementsResponse(PlayerStats(steamID = "x", gameName = "Dota 2", achievements = achievements.toList()))

    private fun detail(apiName: String, percent: Double, hidden: Boolean = false) = GameAchievement(
        id = GameAchievementId(570, apiName),
        displayName = apiName,
        description = "About $apiName",
        iconUrl = "icon/$apiName",
        iconGrayUrl = "gray/$apiName",
        hidden = hidden,
        globalPercent = percent
    )
}
