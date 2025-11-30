package dev.cristianruiz.companion.games.entity

import dev.cristianruiz.companion.games.dto.UserGamesDto
import dev.cristianruiz.companion.user.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.LocalDateTime

@Entity
@Table(name = "user_games")
@EntityListeners(AuditingEntityListener::class)
data class UserGames(
    @EmbeddedId
    val id: UserGamesId,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(name = "name")
    var name: String,

    @Column(name = "play_time_forever_minutes")
    var playTimeForeverMinutes: Int,

    @Column(name = "img_url")
    var imgUrl: String?,

    @CreatedDate
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null,

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)  {
    fun toUserGamesDto(): UserGamesDto {
        return UserGamesDto(
            appId = this.id.appId,
            playTimeForeverMinutes = this.playTimeForeverMinutes,
            name = this.name,
            imgUrl = this.imgUrl
        )
    }
}

@Embeddable
data class UserGamesId(
    @Column(name = "user_id")
    val userId: Long,
    @Column(name = "app_id")
    val appId: Int
)