package dev.cristianruiz.companion.games

import dev.cristianruiz.companion.games.entity.UserGames
import dev.cristianruiz.companion.games.entity.UserGamesId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface GamesRepository: JpaRepository<UserGames, UserGamesId> {

    fun findByUserId(userId: Long): List<UserGames>

    @Modifying
    @Transactional
    @Query("UPDATE UserGames ug SET ug.achievementsTotal = :total, ug.achievementsUnlocked = :unlocked " +
            "WHERE ug.id.userId = :userId AND ug.id.appId = :appId")
    fun updateAchievementProgress(userId: Long, appId: Int, total: Int, unlocked: Int)
}