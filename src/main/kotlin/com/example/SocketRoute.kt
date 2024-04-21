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
                        val message = frame.readText()
                        val type = message.substringBefore("#")
                        val body = message.substringAfter("#")
                        when(type) {
                            "vote" -> game.vote(Json.decodeFromString(body))
                            "login" -> game.loginPlayer(body, this)
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

//private fun extractAction(message: String): Vote? {
//    val type = message.substringBefore("#")
//    val body = message.substringAfter("#")
//    when(type) {
//        "vote" -> return Json.decodeFromString(body)
//        "check_connection" -> {
//            println("Connection checked, everything is alright")
//            return null
//        }
//        else -> throw Exception("action not known")
//    }
//}