package dev.cristianruiz.companion.auth

import dev.cristianruiz.companion.auth.entity.RefreshToken
import dev.cristianruiz.companion.user.UserRepository
import dev.cristianruiz.companion.user.dto.UserDto
import dev.cristianruiz.companion.user.entity.User
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class AuthControllerIntegrationTest {

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
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    private lateinit var testUserDto: UserDto

    @BeforeEach
    fun setUp() {
        testUserDto = userRepository.save(
            User(
                steamId = "76561198000000000",
                username = "testuser",
                profileUrl = "https://steamcommunity.com/profiles/76561198000000000"
            )
        ).toDto()
    }

    @AfterEach
    fun tearDown() {
        refreshTokenRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `refresh should issue new cookies and rotate the refresh token`() {
        val refreshToken = jwtService.generateRefreshToken(testUserDto)

        val setCookies = mockMvc.perform(post("/api/auth/refresh").cookie(Cookie("refreshToken", refreshToken)))
            .andExpect(status().isNoContent)
            .andReturn().response.getHeaders(HttpHeaders.SET_COOKIE)

        val accessCookie = setCookies.single { it.startsWith("accessToken=") }
        val refreshCookie = setCookies.single { it.startsWith("refreshToken=") }
        listOf(accessCookie, refreshCookie).forEach {
            assertTrue(it.contains("HttpOnly") && it.contains("Secure") && it.contains("SameSite=Lax"), it)
        }
        assertTrue(accessCookie.contains("Path=/;"), accessCookie)
        assertTrue(refreshCookie.contains("Path=/api/auth"), refreshCookie)
        assertNotEquals(refreshToken, refreshCookie.substringAfter("=").substringBefore(";"))

        // The old refresh token was revoked by the rotation
        mockMvc.perform(post("/api/auth/refresh").cookie(Cookie("refreshToken", refreshToken)))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `refresh should return 401 without a valid refresh token`() {
        mockMvc.perform(post("/api/auth/refresh"))
            .andExpect(status().isUnauthorized)
        mockMvc.perform(post("/api/auth/refresh").cookie(Cookie("refreshToken", "invalid.jwt.token")))
            .andExpect(status().isUnauthorized)
        // An access token can't be used as a refresh token
        mockMvc.perform(post("/api/auth/refresh").cookie(Cookie("refreshToken", jwtService.generateAccessToken(testUserDto))))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `refresh tokens should not be accepted by the API`() {
        val refreshToken = jwtService.generateRefreshToken(testUserDto)

        mockMvc.perform(get("/api/user/me").header("Authorization", "Bearer $refreshToken"))
            .andExpect(status().isUnauthorized)
        mockMvc.perform(get("/api/user/me").header("Authorization", "Bearer ${jwtService.generateAccessToken(testUserDto)}"))
            .andExpect(status().isOk)
    }

    @Test
    fun `refresh tokens should be stored hashed`() {
        val refreshToken = jwtService.generateRefreshToken(testUserDto)

        val stored = refreshTokenRepository.findAll().single()
        assertNotEquals(refreshToken, stored.tokenHash)
        assertTrue(stored.tokenHash.matches(Regex("[0-9a-f]{64}")), stored.tokenHash)
    }

    @Test
    fun `cleanup should delete only expired refresh tokens`() {
        refreshTokenRepository.save(
            RefreshToken(tokenHash = "expired", steamId = testUserDto.steamId, expiryDate = OffsetDateTime.now().minusDays(1))
        )
        jwtService.generateRefreshToken(testUserDto)

        jwtService.cleanupExpiredTokens()

        assertEquals(listOf(false), refreshTokenRepository.findAll().map { it.tokenHash == "expired" })
    }
}
