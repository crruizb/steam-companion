package dev.cristianruiz.companion.achievements.entity

import dev.cristianruiz.companion.games.dto.UserGamesDto
import dev.cristianruiz.companion.user.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Entity
@Table(name = "achievements")
@EntityListeners(AuditingEntityListener::class)
open class Achievements(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id")
    val userId: Long,
    @Column(name = "app_id")
    val appId: Int,

    @Column(name = "name")
    var name: String,

    @Column(name = "achieved")
    var achieved: Boolean,

    @Column(name = "unlock_time")
    var unlockTime: OffsetDateTime?,

    @CreatedDate
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)
