package dev.cristianruiz.companion

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@TestPropertySource(properties = [
    "app.jwt.secret=testSecretKeyThatIsAtLeast32CharactersLong",
    "app.steam.openid.realm=http://localhost:8080",
    "app.steam.openid.return-to=http://localhost:8080/api/auth/steam/callback",
    "app.steam.apiKey=testSteamApiKey"
])
@Testcontainers
class SteamCompanionBackendApplicationTests {

	companion object {
		// Own database, so the test doesn't need Postgres on localhost (e.g. in CI)
		@Container
		@ServiceConnection
		val postgres = PostgreSQLContainer("postgres:15")
	}

	@Test
	fun contextLoads() {
	}

}
