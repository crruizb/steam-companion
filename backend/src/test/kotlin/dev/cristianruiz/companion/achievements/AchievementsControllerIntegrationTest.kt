package dev.cristianruiz.companion.achievements

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.cristianruiz.companion.achievements.entity.Achievements
import dev.cristianruiz.companion.achievements.entity.GameAchievement
import dev.cristianruiz.companion.achievements.entity.GameAchievementId
import dev.cristianruiz.companion.auth.JwtService
import dev.cristianruiz.companion.games.GamesRepository
import dev.cristianruiz.companion.games.entity.UserGames
import dev.cristianruiz.companion.games.entity.UserGamesId
import dev.cristianruiz.companion.user.UserRepository
import dev.cristianruiz.companion.user.entity.User
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class AchievementsControllerIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:15")
            .withDatabaseName("steam_companion_test")
            .withUsername("test")
            .withPassword("test")
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var gamesRepository: GamesRepository

    @Autowired
    private lateinit var achievementsRepository: AchievementsRepository

    @Autowired
    private lateinit var gameAchievementsRepository: GameAchievementsRepository

    private lateinit var token: String

    private val unlockTime = OffsetDateTime.of(2024, 5, 1, 12, 0, 0, 0, ZoneOffset.UTC)

    @BeforeEach
    fun setUp() {
        val user = userRepository.save(
            User(
                steamId = "76561198000000000",
                username = "testuser",
                displayName = "Test User",
                avatarUrl = "https://example.com/avatar.jpg",
                profileUrl = "https://steamcommunity.com/profiles/76561198000000000"
            )
        )
        token = jwtService.generateAccessToken(user.toDto())

        gamesRepository.saveAll(listOf(
            UserGames(id = UserGamesId(user.id, 570), user = user, name = "Dota 2", playTimeForeverMinutes = 10, imgUrl = null),
            UserGames(id = UserGamesId(user.id, 730), user = user, name = "CS2", playTimeForeverMinutes = 10, imgUrl = null)
        ))
        achievementsRepository.saveAll(listOf(
            Achievements(userId = user.id, appId = 570, name = "COMMON", achieved = true, unlockTime = unlockTime),
            Achievements(userId = user.id, appId = 570, name = "RARE", achieved = true, unlockTime = unlockTime),
            Achievements(userId = user.id, appId = 730, name = "RAREST", achieved = true, unlockTime = unlockTime)
        ))
        gameAchievementsRepository.saveAll(listOf(
            gameAchievement(570, "COMMON", 75.0),
            gameAchievement(570, "RARE", 3.2),
            gameAchievement(570, "LOCKED", 50.0),
            gameAchievement(730, "RAREST", 0.4)
        ))
    }

    @AfterEach
    fun tearDown() {
        gameAchievementsRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `should list a game's achievements with the user's unlocks`() {
        val json = getJson("/api/achievements/games/570")

        assertEquals(true, json["hasDetails"].asBoolean())
        val achievements = json["achievements"]
        assertEquals(listOf("COMMON", "RARE", "LOCKED"), achievements.map { it["apiName"].asText() }.let {
            // Both unlocks share a time, so only check that the locked one comes last
            it.take(2).sorted() + it.drop(2)
        })
        val locked = achievements.single { it["apiName"].asText() == "LOCKED" }
        assertEquals(false, locked["unlocked"].asBoolean())
        assertEquals("Name of LOCKED", locked["displayName"].asText())
        assertEquals("gray/LOCKED", locked["iconUrl"].asText())
    }

    @Test
    fun `should list the user's rarest unlocks across games`() {
        val json = getJson("/api/achievements/rarest")

        assertEquals(listOf("RAREST", "RARE", "COMMON"), json.map { it["displayName"].asText().removePrefix("Name of ") })
        assertEquals("CS2", json[0]["gameName"].asText())
        assertEquals(0.4, json[0]["globalPercent"].asDouble())
    }

    private fun getJson(path: String) = jacksonObjectMapper().readTree(
        mockMvc.perform(get(path).header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString
    )

    private fun gameAchievement(appId: Int, apiName: String, globalPercent: Double) = GameAchievement(
        id = GameAchievementId(appId, apiName),
        displayName = "Name of $apiName",
        description = "About $apiName",
        iconUrl = "icon/$apiName",
        iconGrayUrl = "gray/$apiName",
        hidden = false,
        globalPercent = globalPercent
    )
}
