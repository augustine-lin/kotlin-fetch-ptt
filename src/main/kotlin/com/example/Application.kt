package com.example

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*


fun main() {

    embeddedServer(Netty, port = 8088, host = "0.0.0.0") {
        configureRouting()

        install(ContentNegotiation) {
            jackson()
        }
    }.start(wait = true)
}
