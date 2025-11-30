package dev.cristianruiz.companion.user.entity

import dev.cristianruiz.companion.user.dto.UserDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "steam_id", unique = true, nullable = false)
    val steamId: String,

    @Column(name = "username", nullable = false)
    var username: String,

    @Column(name = "display_name")
    var displayName: String? = null,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Column(name = "profile_url")
    var profileUrl: String? = null,

    @CreatedDate
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
) {
    fun toDto(): UserDto {
        return UserDto(
            steamId = this.steamId,
            username = this.username,
            displayName = this.displayName ?: "",
            avatarUrl = this.avatarUrl ?: "",
            profileUrl = this.profileUrl ?: ""
        )
    }
}