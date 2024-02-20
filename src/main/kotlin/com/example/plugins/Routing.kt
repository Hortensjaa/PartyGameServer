package com.example.plugins

import com.example.models.Game
import com.example.socket
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(game: Game) {
    routing {
        socket(game)
    }
}
