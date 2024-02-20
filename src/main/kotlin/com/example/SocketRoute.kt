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
            val player = game.connectPlayer("player${(1000..9999).random()}",this)


            try {
                incoming.consumeEach { frame ->
                    if(frame is Frame.Text) {
                        val action = extractAction(frame.readText())
                        game.vote(action)
                    }
                }
            } catch(e: Exception) {
                e.printStackTrace()
            } finally {
                game.disconnectPlayer(player)
            }
        }
    }
}

private fun extractAction(message: String): Vote {
    // make_turn#{...}
    val type = message.substringBefore("#")
    val body = message.substringAfter("#")
    if(type == "make_turn") {
        return Json.decodeFromString(body)
    } else throw Exception("")
}