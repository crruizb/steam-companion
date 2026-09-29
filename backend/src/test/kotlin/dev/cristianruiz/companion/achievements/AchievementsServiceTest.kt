package dev.cristianruiz.companion.achievements

import dev.cristianruiz.companion.achievements.entity.Achievements
import dev.cristianruiz.companion.games.GamesRepository
import dev.cristianruiz.companion.steam.Achievement
import dev.cristianruiz.companion.steam.PlayerAchievementsResponse
import dev.cristianruiz.companion.steam.PlayerStats
import dev.cristianruiz.companion.steam.SteamUserApiClient
import dev.cristianruiz.companion.user.entity.User
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class AchievementsServiceTest {

    @MockK
    private lateinit var steamUserApiClient: SteamUserApiClient

    @MockK
    private lateinit var gamesRepository: GamesRepository

    @MockK
    private lateinit var achievementsRepository: AchievementsRepository

    private lateinit var achievementsService: AchievementsService

    private val user = User(
        id = 1,
        steamId = "123456789",
        username = "testuser",
        displayName = "Test User",
        avatarUrl = "http://avatar.url",
        profileUrl = "http://profile.url"
    )

    @BeforeEach
    fun setUp() {
        achievementsService = AchievementsService(steamUserApiClient, gamesRepository, achievementsRepository)
    }

    @Test
    fun `should only save unlocked achievements that were not imported before`() {
        // Given
        every { steamUserApiClient.getPlayerAchievements(user.steamId, 570) } returns PlayerAchievementsResponse(
            PlayerStats(
                steamID = user.steamId,
                gameName = "Dota 2",
                achievements = listOf(
                    Achievement(apiName = "ALREADY_IMPORTED", achieved = 1, unlockTime = 1700000000),
                    Achievement(apiName = "NEWLY_UNLOCKED", achieved = 1, unlockTime = 1710000000),
                    Achievement(apiName = "LOCKED", achieved = 0, unlockTime = 0)
                )
            )
        )
        every { achievementsRepository.findNamesByUserIdAndAppId(user.id, 570) } returns listOf("ALREADY_IMPORTED")
        val saved = slot<List<Achievements>>()
        every { achievementsRepository.saveAll(capture(saved)) } answers { saved.captured }

        // When
        achievementsService.importGameAchievements(user, 570, "Dota 2")

        // Then
        assertEquals(listOf("NEWLY_UNLOCKED"), saved.captured.map { it.name })
    }

    @Test
    fun `should not save anything when all achievements were already imported`() {
        // Given
        every { steamUserApiClient.getPlayerAchievements(user.steamId, 570) } returns PlayerAchievementsResponse(
            PlayerStats(
                steamID = user.steamId,
                gameName = "Dota 2",
                achievements = listOf(
                    Achievement(apiName = "ALREADY_IMPORTED", achieved = 1, unlockTime = 1700000000)
                )
            )
        )
        every { achievementsRepository.findNamesByUserIdAndAppId(user.id, 570) } returns listOf("ALREADY_IMPORTED")

        // When
        achievementsService.importGameAchievements(user, 570, "Dota 2")

        // Then
        verify(exactly = 0) { achievementsRepository.saveAll(any<List<Achievements>>()) }
    }
}
