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
    // todo: here will be big changes because I want let many people play on the same device
    //  so for now it is okay, but i will change it later
    private val state = MutableStateFlow(GameState())
    private val playerSockets = ConcurrentHashMap<String, WebSocketSession>()
    private val gameScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var delayGameJob: Job? = null

    init {
        state.onEach(::broadcast).launchIn(gameScope)
    }

    fun connectPlayer(session: WebSocketSession) {
        println("Anonymous player connected in $session")
        // placeholder value; should delete later, but without it doesn't work - why?
        playerSockets["anonymous"] = session
        state.update {
            it.copy(message = "someone logged in")
        }
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
    fun loginPlayer(name: String, session: WebSocketSession): String {
        playerSockets.remove("anonymous")
        playerSockets += (name to session)
        println(playerSockets)
        return name
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