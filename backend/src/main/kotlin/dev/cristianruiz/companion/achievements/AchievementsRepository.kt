package dev.cristianruiz.companion.achievements

import dev.cristianruiz.companion.achievements.dto.AchievementsPerDate
import dev.cristianruiz.companion.achievements.dto.AchievementsPerSqlDate
import dev.cristianruiz.companion.achievements.entity.Achievements
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AchievementsRepository: JpaRepository<Achievements, Long> {

    @Query("SELECT CAST(date(a.unlockTime) AS DATE), count(a) " +
            "FROM Achievements a " +
            "WHERE a.userId = :userId " +
            "GROUP BY 1 " +
            "ORDER BY 1")
    fun getAchievementsGroupedByUnlockTime(userId: Long): List<AchievementsPerSqlDate>

    @Query("SELECT a.name FROM Achievements a WHERE a.userId = :userId AND a.appId = :appId")
    fun findNamesByUserIdAndAppId(userId: Long, appId: Int): List<String>

    fun findByUserIdAndAppId(userId: Long, appId: Int): List<Achievements>
}
