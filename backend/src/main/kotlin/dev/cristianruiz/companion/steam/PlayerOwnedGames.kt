package dev.cristianruiz.companion.steam

import com.fasterxml.jackson.annotation.JsonProperty

data class PlayerOwnedGamesResponse(
    val response: PlayerOwnedGames
)

data class PlayerOwnedGames(
    val games: List<PlayerOwnedGame>,
    @JsonProperty("game_count")
    val gameCount: Int
)

data class PlayerOwnedGame(
    @JsonProperty("appid")
    val appId: Int,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("playtime_forever")
    val playtimeForever: Int,
    @JsonProperty("img_icon_url")
    val imgIconUrl: String,
)