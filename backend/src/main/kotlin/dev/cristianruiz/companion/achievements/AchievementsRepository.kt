package dev.cristianruiz.companion.achievements

import dev.cristianruiz.companion.achievements.entity.Achievements
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AchievementsRepository: JpaRepository<Achievements, Long> {
}