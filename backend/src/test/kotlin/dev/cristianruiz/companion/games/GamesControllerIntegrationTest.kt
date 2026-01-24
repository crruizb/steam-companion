package dev.cristianruiz.companion.games

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.cristianruiz.companion.auth.JwtService
import dev.cristianruiz.companion.steam.PlayerAchievementsResponse
import dev.cristianruiz.companion.steam.PlayerOwnedGame
import dev.cristianruiz.companion.steam.PlayerOwnedGames
import dev.cristianruiz.companion.steam.PlayerOwnedGamesResponse
import dev.cristianruiz.companion.steam.SteamUserApiClient
import dev.cristianruiz.companion.user.UserRepository
import dev.cristianruiz.companion.user.dto.UserDto
import dev.cristianruiz.companion.user.entity.User
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.text.get

@Service
@Profile("test")
class TestSteamUserApiClient: SteamUserApiClient {
    override fun getPlayerSummaries(steamIds: List<String>) = throw NotImplementedError()
    override fun getOwnedGames(steamId: String): PlayerOwnedGamesResponse {
        val games = listOf(
            PlayerOwnedGame(
                appId = 570,
                name = "Dota 2",
                playtimeForever = 1200,
                imgIconUrl = "iconurl1"
            ),
            PlayerOwnedGame(
                appId = 730,
                name = "Counter-Strike: Global Offensive",
                playtimeForever = 300,
                imgIconUrl = "iconurl2"
            )
        )
        return PlayerOwnedGamesResponse(
            response = PlayerOwnedGames(
                games = games,
                gameCount = games.size
            )
        )
    }

    override fun getPlayerAchievements(
        steamId: String,
        appId: Int
    ): PlayerAchievementsResponse? {
        TODO("Not yet implemented")
    }
}

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class GamesControllerIntegrationTest {

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

    private lateinit var testUser: User
    private lateinit var testUserDto: UserDto
    private lateinit var validJwtToken: String

    @BeforeEach
    fun setUp() {
        testUser = User(
            steamId = "76561198000000000",
            username = "testuser",
            displayName = "Test User",
            avatarUrl = "https://example.com/avatar.jpg",
            profileUrl = "https://steamcommunity.com/profiles/76561198000000000"
        )
        testUserDto = testUser.toDto()

        testUser = userRepository.save(testUser)

        validJwtToken = jwtService.generateToken(testUserDto, 123, "ACCESS")
    }

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
    }

    @Test
    fun `should import user games`() {
        mockMvc.perform(
            post("/api/games/import")
                .header("Authorization", "Bearer $validJwtToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isAccepted)

        val result = mockMvc.perform(
            get("/api/user/games")
                .header("Authorization", "Bearer $validJwtToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andReturn()

        val objectMapper = jacksonObjectMapper()
        val json = result.response.contentAsString
        val ownedGames = objectMapper.readTree(json).get("ownedGames")

        val expectedGames = listOf(
            mapOf(
                "appId" to 570,
                "name" to "Dota 2",
                "playTimeForeverMinutes" to 1200,
                "imgUrl" to "https://media.steampowered.com/steamcommunity/public/images/apps/570/iconurl1.jpg"
            ),
            mapOf(
                "appId" to 730,
                "name" to "Counter-Strike: Global Offensive",
                "playTimeForeverMinutes" to 300,
                "imgUrl" to "https://media.steampowered.com/steamcommunity/public/images/apps/730/iconurl2.jpg"
            )
        )

        for (expected in expectedGames) {
            assertTrue(
                ownedGames.any { game ->
                    game["appId"].asInt() == expected["appId"] &&
                            game["name"].asText() == expected["name"] &&
                            game["playTimeForeverMinutes"].asInt() == expected["playTimeForeverMinutes"] &&
                            game["imgUrl"].asText() == expected["imgUrl"]
                }
            )
        }
    }

    @Test
    fun `should return a random game from user games`() {
        mockMvc.perform(
            post("/api/games/import")
                .header("Authorization", "Bearer $validJwtToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isAccepted)

        val result = mockMvc.perform(
            get("/api/games/random")
                .header("Authorization", "Bearer $validJwtToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andReturn()

        val objectMapper = jacksonObjectMapper()
        val json = result.response.contentAsString
        val randomGame = objectMapper.readTree(json).get("appId").asInt()

        assertTrue(randomGame == 730 || randomGame == 570)
    }

    @Test
    fun `should return 404 if user has no gams imported`() {
        mockMvc.perform(
            get("/api/games/random")
                .header("Authorization", "Bearer $validJwtToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("User has no games imported."))
    }
}
