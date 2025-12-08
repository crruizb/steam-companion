package dev.cristianruiz.companion

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
open class SteamCompanionBackendApplication

fun main(args: Array<String>) {
	runApplication<SteamCompanionBackendApplication>(*args)
}
