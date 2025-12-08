package dev.cristianruiz.companion.user

import WithMockCustomUser
import dev.cristianruiz.companion.auth.JwtService
import dev.cristianruiz.companion.games.dto.UserGamesDto
import dev.cristianruiz.companion.user.entity.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [UserController::class])
@ExtendWith(MockitoExtension::class)
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var userService: UserService

    @MockitoBean
    private lateinit var jwtService: JwtService

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        testUser = User(
            id = 1L,
            steamId = "76561198000000000",
            username = "testuser",
            displayName = "Test User",
            avatarUrl = "https://example.com/avatar.jpg",
            profileUrl = "https://steamcommunity.com/profiles/76561198000000000"
        )
    }

    @Test
    @WithMockCustomUser
    fun `should return authenticated user when using custom annotation`() {
        mockMvc.perform(
            get("/api/user/me")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.steamId").value("76561198000000000"))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.displayName").value("Test User"))
    }

    @Test
    fun `should return 401 when no authentication provided`() {
        mockMvc.perform(get("/api/user/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockCustomUser
    fun `should return authenticated user with games`() {
        Mockito.`when`(userService.findBySteamIdWithGames("76561198000000000"))
            .thenReturn(testUser
                .toDto()
                .copy(ownedGames = setOf(UserGamesDto(appId = 570, name = "Dota 2", playTimeForeverMinutes = 1500, imgUrl = ""))))

        mockMvc.perform(
            get("/api/user/games")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.steamId").value("76561198000000000"))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.displayName").value("Test User"))
            .andExpect(jsonPath("$.ownedGames[0].appId").value(570))
            .andExpect(jsonPath("$.ownedGames[0].name").value("Dota 2"))
            .andExpect(jsonPath("$.ownedGames[0].playTimeForeverMinutes").value(1500))
            .andExpect(jsonPath("$.ownedGames[0].imgUrl").value(""))
    }

}
