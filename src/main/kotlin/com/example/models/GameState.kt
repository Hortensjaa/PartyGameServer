package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class GameState (
    // list of players' names
    val players: List<String> = emptyList(),
    // map of pairs how many votes are for every device (because max 4 players can play on 1 device)
    val devicesVotesMemo: Map<String, Int> = emptyMap(),
    // map of pairs how many votes left in this turn for every device
    val devicesVotesLeft: Map<String, Int> = emptyMap(),
    // current question
    val question: Question? = null,
    // number of votes for players in current question
    val votes: Map<String, Int> = emptyMap(),
    // player who had the most votes
    val winner: String? = null,
    // latest message from server
    val message: String? = null,
    // has game started (if not, players are in the waiting room)
    val gameStarted: Boolean = false
)