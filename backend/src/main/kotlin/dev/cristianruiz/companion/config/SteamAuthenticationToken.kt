package dev.cristianruiz.companion.config

import dev.cristianruiz.companion.user.entity.User
import dev.cristianruiz.companion.user.dto.UserDto
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

class SteamAuthenticationToken(
    private val user: UserDto
) : Authentication {

    private var authenticated = true

    override fun getName(): String = user.username

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_USER"))
    }

    override fun getCredentials(): Any? = null

    override fun getDetails(): Any = user

    override fun getPrincipal(): Any = user

    override fun isAuthenticated(): Boolean = authenticated

    override fun setAuthenticated(isAuthenticated: Boolean) {
        this.authenticated = isAuthenticated
    }
}
