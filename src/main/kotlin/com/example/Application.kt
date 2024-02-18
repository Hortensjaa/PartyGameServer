package com.example

import com.example.models.Game
import com.example.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    val game = Game()
    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureRouting(game)
}
