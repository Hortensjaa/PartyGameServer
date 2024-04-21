package com.example.models

import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class Game {
    private val state = MutableStateFlow(GameState())
    private val playerSockets = ConcurrentHashMap<String, WebSocketSession>()
    private val gameScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var delayGameJob: Job? = null

    init {
        state.onEach(::broadcast).launchIn(gameScope)
    }

    fun connectPlayer(player: String, session: WebSocketSession): String {
        println("user $player connected")
        state.update {
            if(!playerSockets.containsKey(player)) {
                playerSockets[player] = session
            }
            it.copy(
                players = it.players + (player to false)
            )
        }
        return player
    }

    // socket actions
    fun disconnectPlayer(player: String, session: WebSocketSession) {
        println("user $player disconnected")
        playerSockets.remove(player)
        state.update {
            it.copy(
                players = it.players - player
            )
        }
    }

    private suspend fun broadcast(state: GameState) {
        playerSockets.values.forEach { socket ->
            socket.send(
                Json.encodeToString(state)
            )
        }
    }

    // game actions
    fun loginPlayer(name: String, session: WebSocketSession) {
        var toDelete: String? = null
        playerSockets.forEach { (t, u) ->
            if (u == session) {
                toDelete = t
            }
        }
        if (toDelete != null) {
            playerSockets.remove(toDelete)
            playerSockets += (name to session)
        }
        println("player added B)")
        println(playerSockets)
    }

    private fun everyoneVoted(): Boolean {
        return state.value.players.all { (_, v) -> v }
    }

    fun vote(voteObject: Vote) {
        val player = voteObject.player
        val vote = voteObject.vote
        if (state.value.players[player] == false) {
            state.update {
                it.copy(
                    players = it.players + (player to true),
                    votes = if (state.value.votes[vote] == null)
                        it.votes + (vote to 1)
                    else it.votes + (vote to (state.value.votes[vote]!!+1)),
                )
            }
        }
        if (everyoneVoted()) {
            state.update {
                it.copy(
                    winner = getWinningPlayer().also { startNewRoundDelayed() }
                )
            }
        }
    }

    private fun getWinningPlayer(): String {
        val (k, _) = (state.value.votes).maxBy { it.value }
        return k
    }

    private fun startNewRoundDelayed() {
        delayGameJob?.cancel()
        delayGameJob = gameScope.launch {
            delay(5000L)
            state.update {
                it.copy(
                    players = it.players.mapValues { false },
                    question = Questions.questions.random(),
                    votes = emptyMap(),
                    winner = null
                )
            }
        }
    }
}