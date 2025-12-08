package dev.cristianruiz.companion.games

import WithMockCustomUser
import dev.cristianruiz.companion.auth.JwtService
import dev.cristianruiz.companion.games.dto.UserGamesDto
import dev.cristianruiz.companion.user.UserService
import dev.cristianruiz.companion.user.entity.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

@WebMvcTest(controllers = [GamesController::class])
@ExtendWith(MockitoExtension::class)
class GamesControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var gamesService: GamesService

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
    fun `should import games for authenticated user`() {
        Mockito.doNothing().`when`(gamesService).importGames(testUser)
        mockMvc.perform(
            post("/api/games/import")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isAccepted)

        Mockito.verify(gamesService, Mockito.times(1)).importGames(testUser)
    }

    @Test
    fun `should return 401 unathenticated when user is not logged in`() {
        mockMvc.perform(
            post("/api/games/import")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockCustomUser
    fun `should return a random game from user games`() {
        Mockito.`when`(gamesService.getRandomGame(testUser)).thenReturn(UserGamesDto(
            appId = 570,
            name = "Dota 2",
            playTimeForeverMinutes = 1500,
            imgUrl = "https://media.steampowered.com/steamcommunity/public/images/apps/570/icon1.jpg"
        ))
        mockMvc.perform(
            get("/api/games/random")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.appId").value(570))
            .andExpect(jsonPath("$.name").value("Dota 2"))
            .andExpect(jsonPath("$.playTimeForeverMinutes").value(1500))
            .andExpect(jsonPath("$.imgUrl").value("https://media.steampowered.com/steamcommunity/public/images/apps/570/icon1.jpg"))

        Mockito.verify(gamesService, Mockito.times(1)).getRandomGame(testUser)
    }
}