package dev.cristianruiz.companion.config.filters

import dev.cristianruiz.companion.config.SteamAuthenticationToken
import dev.cristianruiz.companion.auth.JwtService
import dev.cristianruiz.companion.user.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import kotlin.text.startsWith
import kotlin.text.substring

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userService: UserService
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    public override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                val jwt = authHeader.substring(7)
                val steamId = jwtService.extractSteamId(jwt)

                if (SecurityContextHolder.getContext().authentication == null) {
                    val user = userService.findBySteamId(steamId)

                    if (user != null && jwtService.isTokenValid(jwt, steamId)) {
                        val authToken = SteamAuthenticationToken(user.toUser())
                        SecurityContextHolder.getContext().authentication = authToken
                    }
                }
            } catch (e: Exception) {
                // Invalid token, continue without authentication
                log.warn(e.message, e)
            }
        }

        filterChain.doFilter(request, response)
    }
}