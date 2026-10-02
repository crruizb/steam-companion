package dev.cristianruiz.companion.games

import dev.cristianruiz.companion.exceptions.BadRequestException
import dev.cristianruiz.companion.games.dto.RandomGameFilter
import dev.cristianruiz.companion.games.entity.UserGames
import dev.cristianruiz.companion.games.entity.UserGamesId
import dev.cristianruiz.companion.steam.PlayerOwnedGame
import dev.cristianruiz.companion.steam.PlayerOwnedGames
import dev.cristianruiz.companion.steam.PlayerOwnedGamesResponse
import dev.cristianruiz.companion.steam.SteamUserApiClient
import dev.cristianruiz.companion.user.entity.User
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class GamesServiceTest {

    @MockK
    private lateinit var steamUserApiClient: SteamUserApiClient

    @MockK
    private lateinit var gamesRepository: GamesRepository

    private lateinit var gamesService: GamesService

    @BeforeEach
    fun setUp() {
        gamesService = GamesService(steamUserApiClient, gamesRepository)
    }

    @Test
    fun `should import all user games and keep their achievement progress`() {
        // Given
        val user = User(
            id = 1,
            steamId = "123456789",
            username = "testuser",
            displayName = "Test User",
            avatarUrl = "http://avatar.url",
            profileUrl = "http://profile.url"
        )
        val playerOwnedGames = PlayerOwnedGames(
            gameCount = 2,
            games = listOf(
                PlayerOwnedGame(
                    appId = 570,
                    name = "Dota 2",
                    playtimeForever = 1500,
                    imgIconUrl = "icon1",
                    rtimeLastPlayed = 1700000000
                ),
                PlayerOwnedGame(
                    appId = 730,
                    name = "Counter-Strike: Global Offensive",
                    playtimeForever = 3000,
                    imgIconUrl = "icon2"
                )
            )
        )
        val playerOwnedGamesResponse = PlayerOwnedGamesResponse(response = playerOwnedGames)
        every { steamUserApiClient.getOwnedGames(user.steamId) } returns playerOwnedGamesResponse
        // Dota 2 was imported before and has been through an achievements import
        every { gamesRepository.findByUserId(user.id) } returns listOf(
            UserGames(
                id = UserGamesId(user.id, 570),
                name = "Dota 2",
                playTimeForeverMinutes = 1000,
                imgUrl = null,
                achievementsTotal = 40,
                achievementsUnlocked = 10,
                user = user
            )
        )

        val userGames = listOf(
            UserGames(
                id = UserGamesId(user.id, 570),
                name = "Dota 2",
                playTimeForeverMinutes = 1500,
                imgUrl = "https://media.steampowered.com/steamcommunity/public/images/apps/570/icon1.jpg",
                lastPlayedAt = Instant.ofEpochSecond(1700000000),
                achievementsTotal = 40,
                achievementsUnlocked = 10,
                user = user
            ),
            // Never played (Steam sends 0): no last played date
            UserGames(
                id = UserGamesId(user.id, 730),
                name = "Counter-Strike: Global Offensive",
                playTimeForeverMinutes = 3000,
                imgUrl = "https://media.steampowered.com/steamcommunity/public/images/apps/730/icon2.jpg",
                user = user
            )
        )
        every { gamesRepository.saveAll(userGames) } returns userGames
        // When
        gamesService.importGames(user)

        // Then
        verify { gamesRepository.saveAll(userGames) }
    }

    @Test
    fun `should throw bad request when steam game details are private`() {
        // Given
        val user = User(
            id = 1,
            steamId = "123456789",
            username = "testuser",
            displayName = "Test User",
            avatarUrl = "http://avatar.url",
            profileUrl = "http://profile.url"
        )
        // Steam responds with {"response":{}} for private profiles
        every { steamUserApiClient.getOwnedGames(user.steamId) } returns PlayerOwnedGamesResponse(PlayerOwnedGames())

        // When / Then
        assertFailsWith<BadRequestException> { gamesService.importGames(user) }
        verify(exactly = 0) { gamesRepository.saveAll(any<List<UserGames>>()) }
    }

    @Test
    fun `should import nothing when a public profile owns no games`() {
        // Given
        val user = User(
            id = 1,
            steamId = "123456789",
            username = "testuser",
            displayName = "Test User",
            avatarUrl = "http://avatar.url",
            profileUrl = "http://profile.url"
        )
        every { steamUserApiClient.getOwnedGames(user.steamId) } returns
            PlayerOwnedGamesResponse(PlayerOwnedGames(gameCount = 0))
        every { gamesRepository.findByUserId(user.id) } returns emptyList()
        every { gamesRepository.saveAll(emptyList<UserGames>()) } returns emptyList()

        // When
        gamesService.importGames(user)

        // Then
        verify { gamesRepository.saveAll(emptyList<UserGames>()) }
    }

    @Test
    fun `should throw error when user has no games imported`() {
        // Given
        val user = User(
            id = 1,
            steamId = "123456789",
            username = "testuser",
            displayName = "Test User",
            avatarUrl = "http://avatar.url",
            profileUrl = "http://profile.url"
        )
        every { gamesRepository.findByUserId(user.id) } returns emptyList()

        // When
        try {
            gamesService.getRandomGame(user)
        } catch (e: NoSuchElementException) {
            // Then
            assert(e.message == "User has no games imported.")
        }

        // Then
        verify { gamesRepository.findByUserId(user.id) }
    }

    @Test
    fun `should return a random game from user games`() {
        // Given
        val user = User(
            id = 1,
            steamId = "123456789",
            username = "testuser",
            displayName = "Test User",
            avatarUrl = "http://avatar.url",
            profileUrl = "http://profile.url"
        )
        every { gamesRepository.findByUserId(user.id) } returns listOf(
            UserGames(
                id = UserGamesId(user.id, 570),
                name = "Dota 2",
                playTimeForeverMinutes = 1500,
                imgUrl = "https://media.steampowered.com/steamcommunity/public/images/apps/570/icon1.jpg",
                user = user
            ),
            UserGames(
                id = UserGamesId(user.id, 730),
                name = "Counter-Strike: Global Offensive",
                playTimeForeverMinutes = 3000,
                imgUrl = "https://media.steampowered.com/steamcommunity/public/images/apps/730/icon2.jpg",
                user = user
            )
        )

        // When
        val result = gamesService.getRandomGame(user)

        // Then
        assertTrue(result.appId == 730 || result.appId == 570)
        verify { gamesRepository.findByUserId(user.id) }
    }

    @Test
    fun `should only pick games matching the filter`() {
        // Given
        val user = filterUser()
        val now = Instant.now()
        every { gamesRepository.findByUserId(user.id) } returns listOf(
            game(user, 1, "Never played", minutes = 0),
            game(user, 2, "Barely played", minutes = 30, lastPlayed = now.minus(Duration.ofDays(2))),
            game(user, 3, "Forgotten", minutes = 600, lastPlayed = now.minus(Duration.ofDays(400))),
            game(user, 4, "Unfinished", minutes = 600, lastPlayed = now, achievements = 3 to 10),
            game(user, 5, "Perfect", minutes = 600, lastPlayed = now, achievements = 10 to 10)
        )

        // When / Then: each filter has exactly one match, so the pick is deterministic
        mapOf(
            RandomGameFilter.NEVER_PLAYED to "Never played",
            RandomGameFilter.UNDER_TWO_HOURS to "Barely played",
            RandomGameFilter.NOT_PLAYED_IN_A_YEAR to "Forgotten",
            RandomGameFilter.ACHIEVEMENTS_LEFT to "Unfinished"
        ).forEach { (filter, expected) ->
            assertEquals(expected, gamesService.getRandomGame(user, filter).name, "filter $filter")
        }
    }

    @Test
    fun `should explain when no game matches the filter`() {
        // Given
        val user = filterUser()
        every { gamesRepository.findByUserId(user.id) } returns listOf(game(user, 1, "Played", minutes = 600))

        // When / Then
        val error = assertFailsWith<NoSuchElementException> {
            gamesService.getRandomGame(user, RandomGameFilter.NEVER_PLAYED)
        }
        assertEquals("No games match \"Never played\".", error.message)
    }

    @Test
    fun `should not pick the excluded game again unless it is the only match`() {
        // Given
        val user = filterUser()
        every { gamesRepository.findByUserId(user.id) } returns listOf(
            game(user, 1, "First", minutes = 0), game(user, 2, "Second", minutes = 0), game(user, 3, "Played", minutes = 600)
        )

        // When / Then: random, so repeat to make a lucky pass unlikely
        repeat(20) {
            assertEquals(2, gamesService.getRandomGame(user, RandomGameFilter.NEVER_PLAYED, excludeAppId = 1).appId)
        }
        // Rerolling the only match gives it back rather than nothing
        every { gamesRepository.findByUserId(user.id) } returns listOf(game(user, 1, "Only", minutes = 0))
        assertEquals(1, gamesService.getRandomGame(user, RandomGameFilter.NEVER_PLAYED, excludeAppId = 1).appId)
    }

    private fun filterUser() = User(
        id = 1,
        steamId = "123456789",
        username = "testuser",
        displayName = "Test User",
        avatarUrl = null,
        profileUrl = "http://profile.url"
    )

    private fun game(
        user: User,
        appId: Int,
        name: String,
        minutes: Int,
        lastPlayed: Instant? = null,
        achievements: Pair<Int, Int>? = null
    ) = UserGames(
        id = UserGamesId(user.id, appId),
        user = user,
        name = name,
        playTimeForeverMinutes = minutes,
        imgUrl = null,
        lastPlayedAt = lastPlayed,
        achievementsUnlocked = achievements?.first,
        achievementsTotal = achievements?.second
    )
}
