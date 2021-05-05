package com.example.plugins

import com.example.routes.clientRouting
import com.example.routes.customerRouting
import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.response.*


fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        customerRouting()
        clientRouting()
    }

}
