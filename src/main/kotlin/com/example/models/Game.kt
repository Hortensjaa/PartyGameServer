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

    // socket actions
    fun connectPlayer(session: WebSocketSession) {
        // fixme: placeholder value; should delete later, but without it doesn't work - why?
        playerSockets["anonymous"] = session
        val randomPlayer = "player${(1000..9999).random()}"
        state.update {
            it.copy(
                message = "$randomPlayer connected"
            )
        }
        println("Anonymous player connected in $session")
        println(playerSockets)
    }

    fun disconnectPlayer(player: String, session: WebSocketSession) {
        playerSockets.remove(player)
        state.update {
            it.copy(
                players = it.players - player
            )
        }
        println("user $player disconnected")
        println(playerSockets)
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
        state.update {
            it.copy(
                players = it.players + (name to true),
                message = "$name logged in"
            )
        }
        println("$name logged in")
        println(playerSockets)
        return name
    }

    // little workaround to not multiply data structure
    // I use state.players map to keep track of players, who are ready to game
    // if everyone is ready, game will start
    private fun startGame() {
        if (everyoneVoted()) {
            state.update {
                it.copy(
                    players = it.players.mapValues { true },
                    question = Questions.questions.random(),
                    gameStarted = true
                )
            }
        }
    }

    // it looks like terrible practice, but will make sense in context of
    // having multiple players on one device = one socket connection
    // (I will add this option in next commit probably)
    fun socketReady(session: WebSocketSession) {
        playerSockets.forEach { entry ->
            if (entry.value == session) {
                state.update {
                    it.copy(
                        players = it.players + (entry.key to false),
                    )
                }
            }
        }
        startGame()
    }

    private fun everyoneVoted(): Boolean {
        return state.value.players.all { (_, v) -> !v }
    }

    fun vote(voteObject: Vote) {
        val player = voteObject.player
        val vote = voteObject.vote
        if (state.value.players[player] == true) {
            state.update {
                it.copy(
                    players = it.players + (player to false),
                    votes = if (state.value.votes[vote] == null)
                        it.votes + (vote to 1)
                    else it.votes + (vote to (state.value.votes[vote]!!+1)),
                )
            }
        }
        println("player $player voted for $vote")
        println(state.value.players)
        if (everyoneVoted()) {
            state.update {
                it.copy(
                    winner = getWinningPlayer().also { startNewRoundDelayed() }
                )
            }
            println("everyone voted")
        }
    }

    private fun getWinningPlayer(): String {
        val (k, _) = (state.value.votes).maxBy { it.value }
        return k
    }

    private fun startNewRoundDelayed() {
        delayGameJob?.cancel()
        delayGameJob = gameScope.launch {
            delay(2000L)
            state.update {
                it.copy(
                    players = it.players.mapValues { true },
                    question = Questions.questions.random(),
                    votes = emptyMap(),
                    winner = null
                )
            }
        }
    }
}