package dev.cristianruiz.companion.auth

import dev.cristianruiz.companion.steam.SteamUserApi
import dev.cristianruiz.companion.user.UserService
import dev.cristianruiz.companion.user.dto.UserDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

@Service
class SteamOpenIdService(
    private val userService: UserService,
    private val restTemplate: RestTemplate,
    private val steamUserApi: SteamUserApi,
) {

    @Value("\${app.steam.openid.realm}")
    private lateinit var realm: String

    @Value("\${app.steam.openid.return-to}")
    private lateinit var returnTo: String

    private val steamOpenIdUrl = "https://steamcommunity.com/openid/login"
    private val steamIdPattern = Pattern.compile("^https://steamcommunity.com/openid/id/(\\d+)$")

    fun generateAuthUrl(): String {
        val params = mapOf(
            "openid.ns" to "http://specs.openid.net/auth/2.0",
            "openid.mode" to "checkid_setup",
            "openid.return_to" to returnTo,
            "openid.realm" to realm,
            "openid.identity" to "http://specs.openid.net/auth/2.0/identifier_select",
            "openid.claimed_id" to "http://specs.openid.net/auth/2.0/identifier_select"
        )

        val queryString = params.entries.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, StandardCharsets.UTF_8)}=${URLEncoder.encode(value, StandardCharsets.UTF_8)}"
        }

        return "$steamOpenIdUrl?$queryString"
    }

    fun verifyAndGetUser(params: Map<String, String>): UserDto? {
        if (!verifyOpenIdResponse(params)) {
            return null
        }

        val identity = params["openid.identity"] ?: return null
        val steamId = extractSteamId(identity) ?: return null

        val playerSummary = steamUserApi.getPlayerSummaries(listOf(steamId)).players.firstOrNull()

        return userService.findBySteamId(steamId) ?: userService.saveUser(
            UserDto(
                steamId = steamId,
                username = "steam_$steamId",
                displayName = playerSummary?.personaName,
                avatarUrl = playerSummary?.avatarFull,
                profileUrl = "https://steamcommunity.com/profiles/$steamId"
            )
        )
    }

    private fun verifyOpenIdResponse(params: Map<String, String>): Boolean {
        val verificationParams = params.toMutableMap().apply {
            set("openid.mode", "check_authentication")
        }

        try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

            val requestBody = buildFormBody(verificationParams)
            val request = HttpEntity(requestBody, headers)

            val response = restTemplate.postForObject(steamOpenIdUrl, request, String::class.java)
            return response?.contains("is_valid:true") == true
        } catch (@Suppress("UNUSED_PARAMETER") e: Exception) {
            return false
        }
    }

    private fun extractSteamId(identity: String): String? {
        val matcher = steamIdPattern.matcher(identity)
        return if (matcher.matches()) matcher.group(1) else null
    }

    private fun buildFormBody(params: Map<String, String>): String {
        return params.entries.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, StandardCharsets.UTF_8)}=${URLEncoder.encode(value, StandardCharsets.UTF_8)}"
        }
    }
}