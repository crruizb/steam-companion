package dev.cristianruiz.companion.user

import dev.cristianruiz.companion.auth.JwtService
import dev.cristianruiz.companion.user.dto.UserDto
import dev.cristianruiz.companion.user.entity.User
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class UserControllerIntegrationTest {

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

        validJwtToken = jwtService.generateToken(testUserDto)
    }

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
    }

    @Test
    fun `should return authenticated user with valid JWT token`() {
        mockMvc.perform(
            get("/api/user/me")
                .header("Authorization", "Bearer $validJwtToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(testUser.id))
            .andExpect(jsonPath("$.steamId").value(testUser.steamId))
            .andExpect(jsonPath("$.username").value(testUser.username))
            .andExpect(jsonPath("$.displayName").value(testUser.displayName))
            .andExpect(jsonPath("$.avatarUrl").value(testUser.avatarUrl))
            .andExpect(jsonPath("$.profileUrl").value(testUser.profileUrl))
    }

    @Test
    fun `should return 401 with invalid JWT token`() {
        val invalidToken = "invalid.jwt.token"

        mockMvc.perform(
            get("/api/user/me")
                .header("Authorization", "Bearer $invalidToken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should return 401 with malformed authorization header`() {
        mockMvc.perform(
            get("/api/user/me")
                .header("Authorization", "InvalidFormat sometoken")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should return 401 when no authorization header provided`() {
        mockMvc.perform(
            get("/api/user/me")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should return 401 when user not found for valid token`() {
        val validTokenButNoUser = jwtService.generateToken(testUserDto.copy(steamId = "nonExistent"))

        mockMvc.perform(
            get("/api/user/me")
                .header("Authorization", "Bearer $validTokenButNoUser")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
    }
}
