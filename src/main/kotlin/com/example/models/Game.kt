package com.example.models

import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import java.util.concurrent.ConcurrentHashMap

class Game {
    private val state = MutableStateFlow(GameState())
    private val devicesSockets = ConcurrentHashMap<String, WebSocketSession>()
    private val gameScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var delayGameJob: Job? = null

    init {
        state.onEach(::broadcast).launchIn(gameScope)
    }

    // socket actions
    fun connectPlayer(session: WebSocketSession) {
        // fixme: placeholder value; should delete later, but without it doesn't work - why?
        devicesSockets["anonymous"] = session
        val randomPlayer = "player${(1000..9999).random()}"
        state.update {
            it.copy(
                message = "$randomPlayer connected"
            )
        }
    }

    fun disconnectPlayer(players: List<String>) {
        val owner = players[0]
        devicesSockets.remove(owner)
        state.update {
            it.copy(
                devicesVotesLeft = it.devicesVotesLeft - owner,
                devicesVotesMemo = it.devicesVotesMemo - owner,
                players = it.players.filterNot { p -> players.contains(p) },
                message = "$owner disconnected and $players left game"
            )
        }
    }

    private suspend fun broadcast(state: GameState) {
        devicesSockets.values.forEach { socket ->
            socket.send(
                Json.encodeToString(state)
            )
        }
    }

    // game actions
    fun loginPlayer(ownerName: String, session: WebSocketSession): String {
        devicesSockets.remove("anonymous")
        devicesSockets += (ownerName to session)
        state.update {
            it.copy(
                players = it.players + ownerName,
                devicesVotesLeft = it.devicesVotesLeft + (ownerName to 1),
                devicesVotesMemo = it.devicesVotesMemo + (ownerName to 1),
                message = "$ownerName logged in"
            )
        }
        return ownerName
    }

    fun addPlayers(players: List<String>): List<String> {
        val owner = players[0]
        state.update {
            it.copy(
                devicesVotesMemo = it.devicesVotesMemo + (owner to players.size),
                players = it.players + players.drop(1),
                message = "$owner added ${players.drop(1)} to game"
            )
        }
        return players
    }

    // little workaround to not multiply data structure
    // I use state.players map to keep track of players, who are ready to game
    // if everyone is ready, game will start
    private fun startGame() {
        if (everyoneVoted()) {
            state.update {
                it.copy(
                    devicesVotesLeft = it.devicesVotesLeft.mapValues { (k, _) -> it.devicesVotesMemo[k] ?: 1},
                    question = Questions.questions.random(),
                    gameStarted = true,
                    message = "Game started"
                )
            }
        }
    }

    fun deviceReady(owner: String) {
        state.update {
            it.copy(
                devicesVotesLeft = it.devicesVotesLeft + (owner to 0),
                message = "$owner's device ready"
            )
        }
        startGame()
    }

    private fun everyoneVoted(): Boolean {
        return state.value.devicesVotesLeft.all { (_, v) -> v==0 }
    }

    fun vote(voteObject: Vote) {
        val owner = voteObject.player
        val vote = voteObject.vote
        if ((state.value.devicesVotesLeft[owner] ?: 0) > 0) {
            state.update {
                it.copy(
                    devicesVotesLeft = it.devicesVotesLeft + (owner to (it.devicesVotesLeft[owner]!!-1)),
                    votes = if (state.value.votes[vote] == null)
                        it.votes + (vote to 1)
                    else it.votes + (vote to (state.value.votes[vote]!!+1)),
                    message = "player on $owner's device voted for $vote"
                )
            }
        }
        if (everyoneVoted()) {
            state.update {
                it.copy(
                    winner = getWinningPlayer().also { startNewRoundDelayed() },
                    message = "everyone voted, winner is ${getWinningPlayer()}"
                )
            }
        }
    }

    private fun getWinningPlayer(): List<String> {
        val maxEntry = state.value.votes.maxByOrNull { it.value }
        if (maxEntry != null) {
            val (_, maxVotes) = maxEntry
            val winners = state.value.votes.filterValues { v -> v == maxVotes }.keys.toList()
            return winners
        }
        return state.value.players
    }

    fun endTurn(owner: String) {
        state.update {
            it.copy(
                devicesVotesLeft = it.devicesVotesLeft + (owner to 0),
                message = "$owner - your time is gone!"
            )
        }
        if (everyoneVoted()) {
            state.update {
                it.copy(
                    winner = getWinningPlayer().also { startNewRoundDelayed() },
                    message = "everyone voted, winner is ${getWinningPlayer()}"
                )
            }
        }
    }

    private fun startNewRoundDelayed() {
        delayGameJob?.cancel()
        delayGameJob = gameScope.launch {
            delay(2000L)
            state.update {
                it.copy(
                    devicesVotesLeft = it.devicesVotesLeft.mapValues { (k, _) -> it.devicesVotesMemo[k] ?: 1},
                    question = Questions.questions.random(),
                    votes = emptyMap(),
                    winner = emptyList(),
                    message = "new round"
                )
            }
        }
    }
}