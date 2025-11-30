package dev.cristianruiz.companion

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = [
    "app.jwt.secret=testSecretKeyThatIsAtLeast32CharactersLong",
    "app.steam.openid.realm=http://localhost:8080",
    "app.steam.openid.return-to=http://localhost:8080/api/auth/steam/callback",
    "app.steam.apiKey=testSteamApiKey"
])
class SteamCompanionBackendApplicationTests {

	@Test
	fun contextLoads() {
	}

}
