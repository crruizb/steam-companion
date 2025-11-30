package dev.cristianruiz.companion.games

import dev.cristianruiz.companion.games.entity.UserGames
import dev.cristianruiz.companion.games.entity.UserGamesId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GamesRepository: JpaRepository<UserGames, UserGamesId> {
}