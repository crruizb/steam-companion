package dev.cristianruiz.companion.achievements

import dev.cristianruiz.companion.achievements.dto.AchievementsImportStatus
import dev.cristianruiz.companion.achievements.dto.AchievementsPerDate
import dev.cristianruiz.companion.achievements.dto.AchievementsPerSqlDate
import dev.cristianruiz.companion.achievements.dto.ImportState
import dev.cristianruiz.companion.achievements.entity.Achievements
import dev.cristianruiz.companion.exceptions.BadRequestException
import dev.cristianruiz.companion.games.GamesRepository
import dev.cristianruiz.companion.games.entity.UserGames
import dev.cristianruiz.companion.games.entity.UserGamesId
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
import java.sql.Date
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    @Test
    fun `should report progress while importing and the totals once completed`() {
        // Given
        every { gamesRepository.findByUserId(user.id) } returns listOf(userGame(570, "Dota 2"), userGame(730, "CS2"))
        every { steamUserApiClient.getPlayerAchievements(user.steamId, 570) } returns PlayerAchievementsResponse(
            PlayerStats(
                steamID = user.steamId,
                gameName = "Dota 2",
                achievements = listOf(
                    Achievement(apiName = "FIRST", achieved = 1, unlockTime = 1700000000),
                    Achievement(apiName = "SECOND", achieved = 1, unlockTime = 1710000000)
                )
            )
        )
        every { steamUserApiClient.getPlayerAchievements(user.steamId, 730) } returns null
        every { achievementsRepository.findNamesByUserIdAndAppId(user.id, any()) } returns emptyList()
        every { achievementsRepository.saveAll(any<List<Achievements>>()) } answers { firstArg() }

        // When
        val started = achievementsService.importAchievements(user)

        // Then
        assertEquals(AchievementsImportStatus(ImportState.RUNNING, totalGames = 2), started)
        assertEquals(
            AchievementsImportStatus(ImportState.COMPLETED, processedGames = 2, totalGames = 2, importedAchievements = 2),
            awaitImportFinished()
        )
    }

    @Test
    fun `should not start a second import while one is running`() {
        // Given
        every { gamesRepository.findByUserId(user.id) } returns listOf(userGame(570, "Dota 2"), userGame(730, "CS2"))
        every { steamUserApiClient.getPlayerAchievements(user.steamId, any()) } returns null

        // When
        val first = achievementsService.importAchievements(user)
        val second = achievementsService.importAchievements(user)
        awaitImportFinished()

        // Then
        assertEquals(ImportState.RUNNING, first.state)
        assertEquals(ImportState.RUNNING, second.state)
        verify(exactly = 1) { steamUserApiClient.getPlayerAchievements(user.steamId, 570) }
    }

    @Test
    fun `should be idle before any import and reject imports without games`() {
        // Given
        every { gamesRepository.findByUserId(user.id) } returns emptyList()

        // When / Then
        assertEquals(AchievementsImportStatus.IDLE, achievementsService.importStatus(user))
        assertFailsWith<BadRequestException> { achievementsService.importAchievements(user) }
        assertEquals(AchievementsImportStatus.IDLE, achievementsService.importStatus(user))
    }

    @Test
    fun `should group achievements per day by year for the heatmap`() {
        // Given
        every { achievementsRepository.getAchievementsGroupedByUnlockTime(user.id) } returns listOf(
            AchievementsPerSqlDate(Date.valueOf("2024-12-31"), 3),
            AchievementsPerSqlDate(Date.valueOf("2025-01-01"), 1),
            AchievementsPerSqlDate(Date.valueOf("2025-03-15"), 5)
        )

        // When
        val heatmap = achievementsService.achievementsHeatmap(user)

        // Then
        assertEquals(
            mapOf(
                2024 to listOf(AchievementsPerDate(LocalDate.of(2024, 12, 31), 3)),
                2025 to listOf(
                    AchievementsPerDate(LocalDate.of(2025, 1, 1), 1),
                    AchievementsPerDate(LocalDate.of(2025, 3, 15), 5)
                )
            ),
            heatmap.achievementsPerDate
        )
    }

    @Test
    fun `should return an empty heatmap when there are no achievements`() {
        // Given
        every { achievementsRepository.getAchievementsGroupedByUnlockTime(user.id) } returns emptyList()

        // When
        val heatmap = achievementsService.achievementsHeatmap(user)

        // Then
        assertEquals(emptyMap(), heatmap.achievementsPerDate)
    }

    private fun userGame(appId: Int, name: String) = UserGames(
        id = UserGamesId(user.id, appId),
        user = user,
        name = name,
        playTimeForeverMinutes = 0,
        imgUrl = null
    )

    // The import runs in a background coroutine with a delay between games, so poll its status
    private fun awaitImportFinished(timeoutMillis: Long = 5_000): AchievementsImportStatus {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            val status = achievementsService.importStatus(user)
            if (status.state != ImportState.RUNNING) return status
            Thread.sleep(50)
        }
        error("Import did not finish within ${timeoutMillis}ms")
    }
}
