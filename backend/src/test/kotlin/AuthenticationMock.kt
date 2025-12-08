import dev.cristianruiz.companion.user.entity.User
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory::class)
annotation class WithMockCustomUser(
    val steamId: String = "76561198000000000",
    val username: String = "testuser",
    val displayName: String = "Test User"
)

/**
 * Security Context Factory that creates authentication with User as principal
 */
class WithMockCustomUserSecurityContextFactory : WithSecurityContextFactory<WithMockCustomUser> {
    override fun createSecurityContext(annotation: WithMockCustomUser): SecurityContext {
        val user = User(
            id = 1L,
            steamId = annotation.steamId,
            username = annotation.username,
            displayName = annotation.displayName,
            avatarUrl = "https://example.com/avatar.jpg",
            profileUrl = "https://steamcommunity.com/profiles/${annotation.steamId}"
        )

        val authentication = UsernamePasswordAuthenticationToken(
            user,
            "password",
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )

        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication
        return context
    }
}