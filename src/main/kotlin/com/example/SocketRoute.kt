package com.example

import com.example.models.Game
import com.example.models.Vote
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json

fun Route.socket(game: Game) {
    route("/play") {
        webSocket {
            var player = "anonymous"
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
                                player = game.loginPlayer(body, this)
                            }
                            "ready" -> game.socketReady(this)
                            "check_connection" -> println("Connection checked, everything is alright")
                            else -> throw Exception("action not known")
                        }
                    }
                }
            } catch(e: Exception) {
                e.printStackTrace()
            } finally {
                game.disconnectPlayer(player, this)
            }
        }
    }
}
