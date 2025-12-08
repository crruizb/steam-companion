package dev.cristianruiz.companion.auth

import dev.cristianruiz.companion.auth.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): RefreshToken?
    fun deleteByExpiryDateBefore(date: OffsetDateTime)

    @Modifying
    @Query("update RefreshToken set isRevoked = true where steamId = :steamId")
    fun logout(steamId: String)
}