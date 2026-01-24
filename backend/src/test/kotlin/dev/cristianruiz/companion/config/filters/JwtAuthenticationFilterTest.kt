package dev.cristianruiz.companion.config.filters

import dev.cristianruiz.companion.auth.JwtService
import dev.cristianruiz.companion.config.SteamAuthenticationToken
import dev.cristianruiz.companion.user.UserService
import dev.cristianruiz.companion.user.dto.UserDto
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

@ExtendWith(MockKExtension::class)
class JwtAuthenticationFilterTest {

    @MockK
    private lateinit var jwtService: JwtService

    @MockK
    private lateinit var userService: UserService

    @MockK
    private lateinit var request: HttpServletRequest

    @MockK
    private lateinit var response: HttpServletResponse

    @MockK
    private lateinit var filterChain: FilterChain

    @MockK
    private lateinit var securityContext: SecurityContext

    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    private val testSteamId = "76561198123456789"
    private val testJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test"
    private val testUserDto = UserDto(
        id = 1L,
        steamId = testSteamId,
        username = "testuser",
        displayName = "Test User",
        avatarUrl = "http://avatar.url",
        profileUrl = "http://profile.url",
        ownedGames = emptySet()
    )

    @BeforeEach
    fun setUp() {
        jwtAuthenticationFilter = JwtAuthenticationFilter(jwtService, userService)
        SecurityContextHolder.setContext(securityContext)
        every { securityContext.authentication = any() } just Runs
        every { filterChain.doFilter(request, response) } just Runs
    }

    @Test
    fun `doFilterInternal should authenticate valid JWT token`() {
        // Given
        every { request.getHeader("Authorization") } returns "Bearer $testJwt"
        every { jwtService.extractSteamId(testJwt) } returns testSteamId
        every { securityContext.authentication } returns null
        every { userService.findBySteamId(testSteamId) } returns testUserDto
        every { jwtService.isTokenValid(testJwt, testSteamId) } returns true

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        verify { jwtService.extractSteamId(testJwt) }
        verify { userService.findBySteamId(testSteamId) }
        verify { jwtService.isTokenValid(testJwt, testSteamId) }
        verify { securityContext.authentication = any<SteamAuthenticationToken>() }
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `doFilterInternal should not authenticate when Authorization header is null`() {
        // Given
        every { request.getHeader("Authorization") } returns null

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        verify(exactly = 0) { jwtService.extractSteamId(any()) }
        verify(exactly = 0) { userService.findBySteamId(any()) }
        verify(exactly = 0) { jwtService.isTokenValid(any(), any()) }
        verify(exactly = 0) { securityContext.authentication = any() }
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `doFilterInternal should not authenticate when Authorization header does not start with Bearer`() {
        // Given
        every { request.getHeader("Authorization") } returns "Basic sometoken"

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        verify(exactly = 0) { jwtService.extractSteamId(any()) }
        verify(exactly = 0) { userService.findBySteamId(any()) }
        verify(exactly = 0) { jwtService.isTokenValid(any(), any()) }
        verify(exactly = 0) { securityContext.authentication = any() }
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `doFilterInternal should not authenticate when user is not found`() {
        // Given
        every { request.getHeader("Authorization") } returns "Bearer $testJwt"
        every { jwtService.extractSteamId(testJwt) } returns testSteamId
        every { securityContext.authentication } returns null
        every { userService.findBySteamId(testSteamId) } returns null

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        verify { jwtService.extractSteamId(testJwt) }
        verify { userService.findBySteamId(testSteamId) }
        verify(exactly = 0) { jwtService.isTokenValid(any(), any()) }
        verify(exactly = 0) { securityContext.authentication = any() }
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `doFilterInternal should not authenticate when token is invalid`() {
        // Given
        every { request.getHeader("Authorization") } returns "Bearer $testJwt"
        every { jwtService.extractSteamId(testJwt) } returns testSteamId
        every { securityContext.authentication } returns null
        every { userService.findBySteamId(testSteamId) } returns testUserDto
        every { jwtService.isTokenValid(testJwt, testSteamId) } returns false

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        verify { jwtService.extractSteamId(testJwt) }
        verify { userService.findBySteamId(testSteamId) }
        verify { jwtService.isTokenValid(testJwt, testSteamId) }
        verify(exactly = 0) { securityContext.authentication = any() }
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `doFilterInternal should not authenticate when SecurityContext already has authentication`() {
        // Given
        val existingAuth = mockk<SteamAuthenticationToken>()
        every { request.getHeader("Authorization") } returns "Bearer $testJwt"
        every { jwtService.extractSteamId(testJwt) } returns testSteamId
        every { securityContext.authentication } returns existingAuth

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        verify { jwtService.extractSteamId(testJwt) }
        verify(exactly = 0) { userService.findBySteamId(any()) }
        verify(exactly = 0) { jwtService.isTokenValid(any(), any()) }
        verify(exactly = 0) { securityContext.authentication = any() }
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `doFilterInternal should handle JWT extraction exception gracefully`() {
        // Given
        every { request.getHeader("Authorization") } returns "Bearer $testJwt"
        every { jwtService.extractSteamId(testJwt) } throws RuntimeException("Invalid JWT")

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        verify { jwtService.extractSteamId(testJwt) }
        verify(exactly = 0) { userService.findBySteamId(any()) }
        verify(exactly = 0) { jwtService.isTokenValid(any(), any()) }
        verify(exactly = 0) { securityContext.authentication = any() }
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `doFilterInternal should handle user service exception gracefully`() {
        // Given
        every { request.getHeader("Authorization") } returns "Bearer $testJwt"
        every { jwtService.extractSteamId(testJwt) } returns testSteamId
        every { securityContext.authentication } returns null
        every { userService.findBySteamId(testSteamId) } throws RuntimeException("Database error")

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        verify { jwtService.extractSteamId(testJwt) }
        verify { userService.findBySteamId(testSteamId) }
        verify(exactly = 0) { jwtService.isTokenValid(any(), any()) }
        verify(exactly = 0) { securityContext.authentication = any() }
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `doFilterInternal should handle token validation exception gracefully`() {
        // Given
        every { request.getHeader("Authorization") } returns "Bearer $testJwt"
        every { jwtService.extractSteamId(testJwt) } returns testSteamId
        every { securityContext.authentication } returns null
        every { userService.findBySteamId(testSteamId) } returns testUserDto
        every { jwtService.isTokenValid(testJwt, testSteamId) } throws RuntimeException("Token validation error")

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        verify { jwtService.extractSteamId(testJwt) }
        verify { userService.findBySteamId(testSteamId) }
        verify { jwtService.isTokenValid(testJwt, testSteamId) }
        verify(exactly = 0) { securityContext.authentication = any() }
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `doFilterInternal should extract JWT correctly from Bearer token`() {
        // Given
        val fullToken = "Bearer $testJwt"
        every { request.getHeader("Authorization") } returns fullToken
        every { jwtService.extractSteamId(testJwt) } returns testSteamId
        every { securityContext.authentication } returns null
        every { userService.findBySteamId(testSteamId) } returns testUserDto
        every { jwtService.isTokenValid(testJwt, testSteamId) } returns true

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        verify { jwtService.extractSteamId(testJwt) } // Should extract the JWT without "Bearer " prefix
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `doFilterInternal should handle empty Bearer token`() {
        // Given
        every { request.getHeader("Authorization") } returns "Bearer "

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        // Then
        verify { jwtService.extractSteamId("") }
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `doFilterInternal should always call filter chain regardless of authentication outcome`() {
        // Given - various scenarios
        val scenarios = listOf(
            { every { request.getHeader("Authorization") } returns null },
            {
                every { request.getHeader("Authorization") } returns "Bearer $testJwt"
                every { jwtService.extractSteamId(testJwt) } throws RuntimeException("Error")
            },
            {
                every { request.getHeader("Authorization") } returns "Bearer $testJwt"
                every { jwtService.extractSteamId(testJwt) } returns testSteamId
                every { securityContext.authentication } returns null
                every { userService.findBySteamId(testSteamId) } returns null
            }
        )

        scenarios.forEach { scenario ->
            clearMocks(filterChain)
            scenario()
            every { filterChain.doFilter(request, response) } just Runs

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

            // Then
            verify { filterChain.doFilter(request, response) }
        }
    }
}
