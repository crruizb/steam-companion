package dev.cristianruiz.companion.achievements

import dev.cristianruiz.companion.achievements.dto.RareAchievementDto
import dev.cristianruiz.companion.achievements.entity.GameAchievement
import dev.cristianruiz.companion.achievements.entity.GameAchievementId
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface GameAchievementsRepository : JpaRepository<GameAchievement, GameAchievementId> {

    @Query("SELECT ga FROM GameAchievement ga WHERE ga.id.appId = :appId")
    fun findByAppId(appId: Int): List<GameAchievement>

    @Query("SELECT max(ga.updatedAt) FROM GameAchievement ga WHERE ga.id.appId = :appId")
    fun findLastUpdated(appId: Int): Instant?

    @Modifying
    @Query("DELETE FROM GameAchievement ga WHERE ga.id.appId = :appId")
    fun deleteByAppId(appId: Int)

    /** The user's unlocked achievements, rarest first. */
    @Query("SELECT new dev.cristianruiz.companion.achievements.dto.RareAchievementDto(" +
            "a.appId, ug.name, ga.displayName, ga.description, ga.iconUrl, ga.globalPercent, a.unlockTime) " +
            "FROM Achievements a " +
            "JOIN GameAchievement ga ON ga.id.appId = a.appId AND ga.id.apiName = a.name " +
            "JOIN UserGames ug ON ug.id.userId = a.userId AND ug.id.appId = a.appId " +
            "WHERE a.userId = :userId AND ga.globalPercent IS NOT NULL " +
            "ORDER BY ga.globalPercent ASC, a.unlockTime DESC")
    fun findRarestUnlocked(userId: Long, pageable: Pageable): List<RareAchievementDto>
}
