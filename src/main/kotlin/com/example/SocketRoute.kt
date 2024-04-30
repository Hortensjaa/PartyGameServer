package com.example

import com.example.models.Game
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json

fun Route.socket(game: Game) {
    route("/play") {
        webSocket {
            var owner = "anonymous"
            var players = listOf<String>()
            game.connectPlayer(this)

            try {
                incoming.consumeEach { frame ->
                    if(frame is Frame.Text) {
                        val message = frame.readText()
                        val type = message.substringBefore("#")
                        val body = message.substringAfter("#")
                        when(type) {
                            "vote" -> game.vote(Json.decodeFromString(body))
                            "login" -> {
                                owner = game.loginPlayer(body, this)
                                players = listOf(owner)
                            }
                            "add" -> {
                                players = game.addPlayers(Json.decodeFromString<List<String>>(body))
                            }
                            "ready" -> game.deviceReady(body)
                            "check_connection" -> println("Connection checked, everything is alright")
                            else -> throw Exception("action not known")
                        }
                    }
                }
            } catch(e: Exception) {
                e.printStackTrace()
            } finally {
                game.disconnectPlayer(players)
            }
        }
    }
}
