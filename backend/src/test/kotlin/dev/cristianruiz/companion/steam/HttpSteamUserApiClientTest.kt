package dev.cristianruiz.companion.steam

import org.junit.jupiter.api.BeforeEach
import org.springframework.http.MediaType
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestTemplate
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Parses real-shaped Steam responses through the same RestTemplate setup as the app.
 * Steam leaves out optional fields (for example on friends' profiles), so the Kotlin
 * defaults in the response classes must apply.
 */
class HttpSteamUserApiClientTest {

    private val restTemplate = RestTemplate()
    private lateinit var server: MockRestServiceServer
    private lateinit var client: HttpSteamUserApiClient

    @BeforeEach
    fun setUp() {
        server = MockRestServiceServer.bindTo(restTemplate).build()
        client = HttpSteamUserApiClient(restTemplate)
        ReflectionTestUtils.setField(client, "apiKey", "test-key")
    }

    @Test
    fun `should read owned games without a last played time`() {
        // Given: no rtime_last_played, as Steam sends for some friends' games
        server.expect(requestTo(org.hamcrest.Matchers.containsString("GetOwnedGames")))
            .andRespond(json("""{"response":{"game_count":1,"games":[{"appid":570,"name":"Dota 2","playtime_forever":42,"img_icon_url":"abc"}]}}"""))

        // When
        val games = client.getOwnedGames("222").response.games

        // Then
        assertEquals(listOf(PlayerOwnedGame(570, "Dota 2", 42, "abc", rtimeLastPlayed = 0)), games)
    }

    @Test
    fun `should read friends without a friend since time`() {
        // Given
        server.expect(requestTo(org.hamcrest.Matchers.containsString("GetFriendList")))
            .andRespond(json("""{"friendslist":{"friends":[{"steamid":"222","relationship":"friend"}]}}"""))

        // When
        val friends = client.getFriendList("111")?.friendsList?.friends

        // Then
        assertEquals(listOf(SteamFriend("222", friendSince = 0)), friends)
    }

    @Test
    fun `should read schema achievements without optional fields`() {
        // Given: no hidden flag, description or icons
        server.expect(requestTo(org.hamcrest.Matchers.containsString("GetSchemaForGame")))
            .andRespond(json("""{"game":{"availableGameStats":{"achievements":[{"name":"WIN","displayName":"Winner"}]}}}"""))

        // When
        val achievements = client.getGameSchema(570)?.game?.availableGameStats?.achievements

        // Then
        assertEquals(listOf(SchemaAchievement(name = "WIN", displayName = "Winner", hidden = 0)), achievements)
    }

    @Test
    fun `should read player stats for a game without achievements`() {
        // Given
        server.expect(requestTo(org.hamcrest.Matchers.containsString("GetPlayerAchievements")))
            .andRespond(json("""{"playerstats":{"steamID":"111","gameName":"Dota 2","success":true}}"""))

        // When
        val stats = client.getPlayerAchievements("111", 570)?.playerStats

        // Then
        assertEquals(null, stats?.achievements)
    }

    @Test
    fun `should read global percentages sent as strings`() {
        // Given: newer Steam responses quote the percent
        server.expect(requestTo(org.hamcrest.Matchers.containsString("GetGlobalAchievementPercentagesForApp")))
            .andRespond(json("""{"achievementpercentages":{"achievements":[{"name":"WIN","percent":"12.5"}]}}"""))

        // When
        val percentages = client.getGlobalAchievementPercentages(570)?.achievementPercentages?.achievements

        // Then
        assertEquals(listOf(GlobalAchievementPercentage("WIN", 12.5)), percentages)
    }

    private fun json(body: String) = withSuccess(body, MediaType.APPLICATION_JSON)
}
