package dev.cristianruiz.companion.user

import dev.cristianruiz.companion.user.dto.UserDto
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun findBySteamId(steamId: String): UserDto? {
        return userRepository.findBySteamId(steamId)
            .getOrElse { return null }
            .toDto()
    }

    fun saveUser(user: UserDto): UserDto {
        return userRepository
            .save(user.toUser())
            .toDto()
    }
}