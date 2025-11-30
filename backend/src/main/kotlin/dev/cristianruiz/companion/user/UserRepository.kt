package dev.cristianruiz.companion.user

import dev.cristianruiz.companion.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findBySteamId(steamId: String): Optional<User>

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.userGames " +
            "WHERE u.steamId = :steamId ")
    fun findBySteamIdWithGames(@Param("steamId") steamId: String): Optional<User>
}