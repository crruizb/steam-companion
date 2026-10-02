package dev.cristianruiz.companion.achievements

import dev.cristianruiz.companion.achievements.entity.GameAchievement
import dev.cristianruiz.companion.achievements.entity.GameAchievementId
import dev.cristianruiz.companion.steam.SteamUserApiClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.Duration
import java.time.Instant

/** Keeps the names, icons and rarity of each game's achievements, fetched from Steam. */
@Service
class AchievementSchemaService(
    private val steamUserApiClient: SteamUserApiClient,
    private val gameAchievementsRepository: GameAchievementsRepository,
    private val transactionTemplate: TransactionTemplate,
) {

    private val log = LoggerFactory.getLogger(AchievementSchemaService::class.java)

    /**
     * Fetches the game's achievement details from Steam unless they were fetched within [maxAge].
     * The details are shared by all users and rarity changes slowly, so most imports skip the requests.
     * Failures are logged and leave the stored details as they were.
     */
    fun refreshIfStale(appId: Int, maxAge: Duration = Duration.ofDays(7)) {
        try {
            val lastUpdated = gameAchievementsRepository.findLastUpdated(appId)
            if (lastUpdated != null && lastUpdated.isAfter(Instant.now().minus(maxAge))) return

            val schema = steamUserApiClient.getGameSchema(appId)
                ?.game?.availableGameStats?.achievements.orEmpty()
            if (schema.isEmpty()) return
            // Rarity is extra: without it the names and icons are still worth storing
            val percents = try {
                steamUserApiClient.getGlobalAchievementPercentages(appId)
                    ?.achievementPercentages?.achievements.orEmpty()
                    .associate { it.name to it.percent }
            } catch (e: Exception) {
                log.warn("Failed to fetch achievement rarity for app $appId, error: ${e.message}")
                emptyMap()
            }

            val now = Instant.now()
            val details = schema.map {
                GameAchievement(
                    id = GameAchievementId(appId, it.name),
                    displayName = it.displayName,
                    description = it.description?.takeIf(String::isNotBlank),
                    iconUrl = it.icon,
                    iconGrayUrl = it.iconGray,
                    hidden = it.hidden == 1,
                    globalPercent = percents[it.name],
                    updatedAt = now
                )
            }
            // Steam requests happen above, so the transaction only spans the database writes
            transactionTemplate.executeWithoutResult {
                gameAchievementsRepository.deleteByAppId(appId)
                gameAchievementsRepository.saveAll(details)
            }
        } catch (e: Exception) {
            log.warn("Failed to fetch achievement details for app $appId, error: ${e.message}")
        }
    }
}
