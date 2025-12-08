package dev.cristianruiz.companion.games

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
    fun `should import all user games`() {
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
                    imgIconUrl = "icon1"
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

        val userGames = listOf(
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
        every { gamesRepository.saveAll(userGames) } returns userGames
        // When
        gamesService.importGames(user)

        // Then
        verify { gamesRepository.saveAll(userGames) }
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

}