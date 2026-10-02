package dev.cristianruiz.companion.steam

import com.fasterxml.jackson.annotation.JsonProperty

// GetFriendList. Steam answers 401 instead when the friends list is private
data class FriendListResponse(
    @JsonProperty("friendslist")
    val friendsList: FriendList? = null
)

data class FriendList(
    val friends: List<SteamFriend>? = null
)

data class SteamFriend(
    @JsonProperty("steamid")
    val steamId: String,
    // Unix seconds; 0 for friendships older than Steam started recording it
    @JsonProperty("friend_since")
    val friendSince: Long = 0
)
