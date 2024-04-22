package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class GameState (
    // map of pairs (player_name: has_voted_in_this_turn) e.g. {Anna: true, Bella: false, Cindy: true}
    val players: Map<String, Boolean> = emptyMap(),
    // current question
    val question: Question? = null,
    // number of votes in current question
    val votes: Map<String, Int> = emptyMap(),
    // player who had the most votes
    val winner: String? = null,
    // latest message from server
    val message: String? = null
)