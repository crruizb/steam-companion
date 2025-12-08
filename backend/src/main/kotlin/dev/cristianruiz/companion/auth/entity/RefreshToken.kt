package dev.cristianruiz.companion.auth.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "refresh_tokens")
open class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val token: String,

    @Column(nullable = false)
    val steamId: String,

    @Column(nullable = false)
    val expiryDate: OffsetDateTime,

    @CreationTimestamp
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(nullable = false)
    var isRevoked: Boolean = false
)