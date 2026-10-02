package dev.cristianruiz.companion.achievements

import dev.cristianruiz.companion.achievements.entity.GameAchievement
import dev.cristianruiz.companion.steam.AvailableGameStats
import dev.cristianruiz.companion.steam.GameSchema
import dev.cristianruiz.companion.steam.GameSchemaResponse
import dev.cristianruiz.companion.steam.GlobalAchievementPercentage
import dev.cristianruiz.companion.steam.GlobalAchievementPercentages
import dev.cristianruiz.companion.steam.GlobalAchievementPercentagesResponse
import dev.cristianruiz.companion.steam.SchemaAchievement
import dev.cristianruiz.companion.steam.SteamUserApiClient
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import java.time.Duration
import java.time.Instant
import java.util.function.Consumer
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class AchievementSchemaServiceTest {

    @MockK
    private lateinit var steamUserApiClient: SteamUserApiClient

    @MockK
    private lateinit var gameAchievementsRepository: GameAchievementsRepository

    @MockK
    private lateinit var transactionTemplate: TransactionTemplate

    private lateinit var service: AchievementSchemaService

    private val saved = slot<List<GameAchievement>>()

    @BeforeEach
    fun setUp() {
        service = AchievementSchemaService(steamUserApiClient, gameAchievementsRepository, transactionTemplate)
        every { transactionTemplate.executeWithoutResult(any()) } answers {
            firstArg<Consumer<TransactionStatus>>().accept(mockk(relaxed = true))
        }
        every { gameAchievementsRepository.deleteByAppId(any()) } just runs
        every { gameAchievementsRepository.saveAll(capture(saved)) } answers { saved.captured }
    }

    @Test
    fun `should store names, icons and rarity when the details are missing`() {
        // Given
        every { gameAchievementsRepository.findLastUpdated(570) } returns null
        every { steamUserApiClient.getGameSchema(570) } returns schema(
            SchemaAchievement(name = "WIN", displayName = "Winner", description = "Win a game", icon = "i1", iconGray = "g1"),
            SchemaAchievement(name = "SECRET", displayName = "Secret", hidden = 1, description = " ")
        )
        every { steamUserApiClient.getGlobalAchievementPercentages(570) } returns GlobalAchievementPercentagesResponse(
            GlobalAchievementPercentages(listOf(GlobalAchievementPercentage("WIN", 42.5)))
        )

        // When
        service.refreshIfStale(570)

        // Then
        verify { gameAchievementsRepository.deleteByAppId(570) }
        val byName = saved.captured.associateBy { it.id.apiName }
        assertEquals("Winner", byName["WIN"]?.displayName)
        assertEquals("i1", byName["WIN"]?.iconUrl)
        assertEquals("g1", byName["WIN"]?.iconGrayUrl)
        assertEquals(42.5, byName["WIN"]?.globalPercent)
        assertEquals(true, byName["SECRET"]?.hidden)
        assertEquals(null, byName["SECRET"]?.description)
        assertEquals(null, byName["SECRET"]?.globalPercent)
    }

    @Test
    fun `should skip Steam when the details are fresh`() {
        // Given
        every { gameAchievementsRepository.findLastUpdated(570) } returns Instant.now().minus(Duration.ofDays(1))

        // When
        service.refreshIfStale(570)

        // Then
        verify(exactly = 0) { steamUserApiClient.getGameSchema(any()) }
    }

    @Test
    fun `should still store names and icons when rarity can't be fetched`() {
        // Given
        every { gameAchievementsRepository.findLastUpdated(570) } returns Instant.now().minus(Duration.ofDays(30))
        every { steamUserApiClient.getGameSchema(570) } returns schema(SchemaAchievement(name = "WIN", displayName = "Winner"))
        every { steamUserApiClient.getGlobalAchievementPercentages(570) } throws RuntimeException("403 Forbidden")

        // When
        service.refreshIfStale(570)

        // Then
        assertEquals(listOf("Winner"), saved.captured.map { it.displayName })
        assertEquals(null, saved.captured.single().globalPercent)
    }

    @Test
    fun `should keep the stored details when the schema request fails`() {
        // Given
        every { gameAchievementsRepository.findLastUpdated(570) } returns null
        every { steamUserApiClient.getGameSchema(570) } throws RuntimeException("500 Internal Server Error")

        // When
        service.refreshIfStale(570)

        // Then
        verify(exactly = 0) { gameAchievementsRepository.deleteByAppId(any()) }
    }

    private fun schema(vararg achievements: SchemaAchievement) =
        GameSchemaResponse(GameSchema(AvailableGameStats(achievements.toList())))
}
