package dev.cristianruiz.companion.user

import dev.cristianruiz.companion.games.dto.UserGamesDto
import dev.cristianruiz.companion.games.entity.UserGames
import dev.cristianruiz.companion.games.entity.UserGamesId
import java.util.Optional
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertEquals
import io.mockk.verify
import io.mockk.mockk
import io.mockk.junit5.MockKExtension
import io.mockk.impl.annotations.MockK
import io.mockk.every
import dev.cristianruiz.companion.user.entity.User
import dev.cristianruiz.companion.user.dto.UserDto

@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userService = UserService(userRepository)
    }

    @Test
    fun `saveUser saves and returns UserDto`() {
        // Given
        val userDto = UserDto(
            id = null,
            steamId = "123",
            username = "testuser",
            displayName = "Test User",
            avatarUrl = "http://avatar.url",
            profileUrl = "http://profile.url"
        )
        every { userRepository.save(userDto.toUser()) } returns userDto.copy(id = 1).toUser()

        // When
        val result = userService.saveUser(userDto)

        // Then
        assertEquals(userDto.copy(id = 1, ownedGames = emptySet()), result)
        verify { userRepository.save(userDto.toUser()) }
    }

    @Test
    fun `findBySteamIdWithGames returns null when not found`() {
        // Given
        every { userRepository.findBySteamIdWithGames("123") } returns Optional.empty()

        // When
        val result = userService.findBySteamIdWithGames("123")

        // Then
        assertNull(result)
    }

    @Test
    fun `findBySteamIdWithGames returns UserDto when found`() {
        // Given
        val userDto = UserDto(
            id = 1,
            steamId = "123",
            username = "testuser",
            displayName = "Test User",
            avatarUrl = "http://avatar.url",
            profileUrl = "http://profile.url",
            ownedGames = setOf(UserGamesDto(1, 1000, "test", "img"))
        )
        val user = User(
            id = 1,
            steamId = "123",
            username = "testuser",
            displayName = "Test User",
            avatarUrl = "http://avatar.url",
            profileUrl = "http://profile.url",
            userGames = mutableSetOf(UserGames(
                id = UserGamesId(1, 1),
                playTimeForeverMinutes = 1000,
                name = "test",
                imgUrl = "img",
                user = mockk()
            ))
        )

        every { userRepository.findBySteamIdWithGames("123") } returns Optional.of(user)

        // When
        val result = userService.findBySteamIdWithGames("123")

        // Then
        assertEquals(userDto, result)
    }

    @Test
    fun `findBySteamId returns null when not found`() {
        // Given
        every { userRepository.findBySteamId("123") } returns Optional.empty()

        // When
        val result = userService.findBySteamId("123")

        // Then
        assertNull(result)
    }

    @Test
    fun `findBySteamId returns UserDto when found`() {
        // Given
        val userDto = UserDto(
            id = 1,
            steamId = "123",
            username = "testuser",
            displayName = "Test User",
            avatarUrl = "http://avatar.url",
            profileUrl = "http://profile.url",
            ownedGames = emptySet()
        )
        val user = User(
            id = 1,
            steamId = "123",
            username = "testuser",
            displayName = "Test User",
            avatarUrl = "http://avatar.url",
            profileUrl = "http://profile.url",
        )

        every { userRepository.findBySteamId("123") } returns Optional.of(user)

        // When
        val result = userService.findBySteamId("123")

        // Then
        assertEquals(userDto, result)
    }
}





