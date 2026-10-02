package dev.cristianruiz.companion.achievements

import dev.cristianruiz.companion.achievements.dto.PerfectedGameDto
import dev.cristianruiz.companion.achievements.dto.RareAchievementDto
import dev.cristianruiz.companion.achievements.dto.YearGameDto
import dev.cristianruiz.companion.achievements.entity.GameAchievement
import dev.cristianruiz.companion.achievements.entity.GameAchievementId
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.OffsetDateTime

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

    /** Like [findRarestUnlocked], for unlocks in [from, to). */
    @Query("SELECT new dev.cristianruiz.companion.achievements.dto.RareAchievementDto(" +
            "a.appId, ug.name, ga.displayName, ga.description, ga.iconUrl, ga.globalPercent, a.unlockTime) " +
            "FROM Achievements a " +
            "JOIN GameAchievement ga ON ga.id.appId = a.appId AND ga.id.apiName = a.name " +
            "JOIN UserGames ug ON ug.id.userId = a.userId AND ug.id.appId = a.appId " +
            "WHERE a.userId = :userId AND ga.globalPercent IS NOT NULL " +
            "AND a.unlockTime >= :from AND a.unlockTime < :to " +
            "ORDER BY ga.globalPercent ASC, a.unlockTime DESC")
    fun findRarestUnlockedBetween(
        userId: Long, from: OffsetDateTime, to: OffsetDateTime, pageable: Pageable
    ): List<RareAchievementDto>

    /** Games by achievements unlocked in [from, to), most first. */
    @Query("SELECT new dev.cristianruiz.companion.achievements.dto.YearGameDto(a.appId, ug.name, count(a)) " +
            "FROM Achievements a " +
            "JOIN UserGames ug ON ug.id.userId = a.userId AND ug.id.appId = a.appId " +
            "WHERE a.userId = :userId AND a.unlockTime >= :from AND a.unlockTime < :to " +
            "GROUP BY a.appId, ug.name " +
            "ORDER BY count(a) DESC, ug.name ASC")
    fun findTopGamesBetween(userId: Long, from: OffsetDateTime, to: OffsetDateTime, pageable: Pageable): List<YearGameDto>

    /** Fully completed games whose latest unlock falls in [from, to), most recent first. */
    @Query("SELECT new dev.cristianruiz.companion.achievements.dto.PerfectedGameDto(" +
            "ug.id.appId, ug.name, ug.achievementsTotal, max(a.unlockTime)) " +
            "FROM UserGames ug " +
            "JOIN Achievements a ON a.userId = ug.id.userId AND a.appId = ug.id.appId " +
            "WHERE ug.id.userId = :userId AND ug.achievementsTotal > 0 " +
            "AND ug.achievementsUnlocked >= ug.achievementsTotal " +
            "GROUP BY ug.id.appId, ug.name, ug.achievementsTotal " +
            "HAVING max(a.unlockTime) >= :from AND max(a.unlockTime) < :to " +
            "ORDER BY max(a.unlockTime) DESC")
    fun findPerfectedBetween(userId: Long, from: OffsetDateTime, to: OffsetDateTime): List<PerfectedGameDto>
}
