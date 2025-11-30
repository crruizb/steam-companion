package dev.cristianruiz.companion.user

import dev.cristianruiz.companion.user.dto.UserDto
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
open class UserService(
    private val userRepository: UserRepository
) {

    @Transactional
    open fun findBySteamId(steamId: String): UserDto? {
        return userRepository.findBySteamId(steamId)
            .getOrElse { return null }
            .toDto()
    }

    @Transactional
    open fun findBySteamIdWithGames(steamId: String): UserDto? {
        return userRepository.findBySteamIdWithGames(steamId)
            .getOrElse { return null }
            .toDto()
    }

    fun saveUser(user: UserDto): UserDto {
        return userRepository
            .save(user.toUser())
            .toDto()
    }
}