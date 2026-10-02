package dev.cristianruiz.companion.achievements

import dev.cristianruiz.companion.achievements.dto.AchievementsImportStatus
import dev.cristianruiz.companion.achievements.dto.AchievementsPerDate
import dev.cristianruiz.companion.achievements.dto.AchievementsPerSqlDate
import dev.cristianruiz.companion.achievements.dto.ImportState
import dev.cristianruiz.companion.achievements.dto.GameAchievementDto
import dev.cristianruiz.companion.achievements.entity.Achievements
import dev.cristianruiz.companion.achievements.entity.GameAchievement
import dev.cristianruiz.companion.achievements.entity.GameAchievementId
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
import io.mockk.just
import io.mockk.runs
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.sql.Date
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
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

    @MockK
    private lateinit var gameAchievementsRepository: GameAchievementsRepository

    @MockK
    private lateinit var achievementSchemaService: AchievementSchemaService

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
        achievementsService = AchievementsService(
            steamUserApiClient, gamesRepository, achievementsRepository, gameAchievementsRepository, achievementSchemaService
        )
        every { gamesRepository.updateAchievementProgress(any(), any(), any(), any()) } just runs
        every { achievementSchemaService.refreshIfStale(any(), any()) } just runs
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
        verify { gamesRepository.updateAchievementProgress(user.id, 570, total = 3, unlocked = 2) }
        verify { achievementSchemaService.refreshIfStale(570, any()) }
    }

    @Test
    fun `should record zero progress for games without achievements`() {
        // Given: Steam leaves out the achievements list for games that have none
        every { steamUserApiClient.getPlayerAchievements(user.steamId, 570) } returns PlayerAchievementsResponse(
            PlayerStats(steamID = user.steamId, gameName = "Dota 2")
        )

        // When
        val imported = achievementsService.importGameAchievements(user, 570, "Dota 2")

        // Then
        assertEquals(0, imported)
        verify { gamesRepository.updateAchievementProgress(user.id, 570, total = 0, unlocked = 0) }
        verify(exactly = 0) { achievementsRepository.saveAll(any<List<Achievements>>()) }
        // Nothing to describe, so no requests for names, icons and rarity
        verify(exactly = 0) { achievementSchemaService.refreshIfStale(any(), any()) }
    }

    @Test
    fun `should list unlocked achievements newest first, then locked ones from most to least common`() {
        // Given
        val firstUnlock = OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        val laterUnlock = firstUnlock.plusDays(10)
        every { achievementsRepository.findByUserIdAndAppId(user.id, 570) } returns listOf(
            Achievements(userId = user.id, appId = 570, name = "FIRST", achieved = true, unlockTime = firstUnlock),
            Achievements(userId = user.id, appId = 570, name = "LATER", achieved = true, unlockTime = laterUnlock),
            Achievements(userId = user.id, appId = 570, name = "NOT_IN_SCHEMA", achieved = true, unlockTime = firstUnlock.minusDays(1))
        )
        every { gameAchievementsRepository.findByAppId(570) } returns listOf(
            gameAchievement("FIRST", globalPercent = 80.0),
            gameAchievement("LATER", globalPercent = 2.5),
            gameAchievement("RARE_LOCKED", globalPercent = 1.0),
            gameAchievement("COMMON_LOCKED", globalPercent = 60.0),
            gameAchievement("SECRET_LOCKED", globalPercent = 5.0, hidden = true)
        )

        // When
        val result = achievementsService.gameAchievements(user, 570)

        // Then
        assertEquals(true, result.hasDetails)
        assertEquals(
            listOf("LATER", "FIRST", "NOT_IN_SCHEMA", "COMMON_LOCKED", "SECRET_LOCKED", "RARE_LOCKED"),
            result.achievements.map { it.apiName }
        )
        val byName = result.achievements.associateBy { it.apiName }
        assertEquals(
            GameAchievementDto("LATER", "Name of LATER", "About LATER", "icon/LATER", true, laterUnlock, 2.5),
            byName["LATER"]
        )
        // Locked: gray icon; a hidden one also keeps its description secret
        assertEquals("gray/COMMON_LOCKED", byName["COMMON_LOCKED"]?.iconUrl)
        assertEquals("About COMMON_LOCKED", byName["COMMON_LOCKED"]?.description)
        assertEquals(null, byName["SECRET_LOCKED"]?.description)
        // Unlocked but unknown to the stored details: falls back to the API name
        assertEquals("NOT_IN_SCHEMA", byName["NOT_IN_SCHEMA"]?.displayName)
    }

    @Test
    fun `should list only unlocked achievements before details are fetched`() {
        // Given
        every { achievementsRepository.findByUserIdAndAppId(user.id, 570) } returns listOf(
            Achievements(userId = user.id, appId = 570, name = "FIRST", achieved = true, unlockTime = null)
        )
        every { gameAchievementsRepository.findByAppId(570) } returns emptyList()

        // When
        val result = achievementsService.gameAchievements(user, 570)

        // Then
        assertEquals(false, result.hasDetails)
        assertEquals(listOf("FIRST"), result.achievements.map { it.displayName })
    }

    @Test
    fun `should leave progress untouched when Steam has no stats for the game`() {
        // Given
        every { steamUserApiClient.getPlayerAchievements(user.steamId, 570) } throws RuntimeException("400 Bad Request")

        // When
        achievementsService.importGameAchievements(user, 570, "Dota 2")

        // Then
        verify(exactly = 0) { gamesRepository.updateAchievementProgress(any(), any(), any(), any()) }
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

    private fun gameAchievement(apiName: String, globalPercent: Double?, hidden: Boolean = false) = GameAchievement(
        id = GameAchievementId(570, apiName),
        displayName = "Name of $apiName",
        description = "About $apiName",
        iconUrl = "icon/$apiName",
        iconGrayUrl = "gray/$apiName",
        hidden = hidden,
        globalPercent = globalPercent
    )

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
