package com.example.routes

import com.fasterxml.jackson.databind.node.ObjectNode
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*


val ChannelAccessToken =
    "4413tovpM75EOhW9E8ql+RW7h3YIxW20PjZG6aKkFkP+5ckXLeCwQIfoUAGjYIvrqTfAn1O7P5oCQiHytg9tdQzCG+0pemdLtjFz5p5bQMIKk6pa2yYktMCX8u2qxEK3/RruGSBjAkx2YfnULGrdrgdB04t89/1O/w1cDnyilFU="

data class TextMessage(val type: String = "text", val text: String)
data class ReplyData(val replyToken: String, val messages: List<TextMessage>)

suspend fun reply(reply: ReplyData) {
    try {
        val replyRes = client.post<Unit>("https://api.line.me/v2/bot/message/reply") {
            headers.append("Authorization", "Bearer ${ChannelAccessToken}")
            contentType(ContentType.Application.Json)
            println(reply)
            body = reply
        }

        println(replyRes)
    } catch (e: Exception) {
        println(e)
    }
}


fun Route.webhookRoutes() {
    route("webhook") {
        post {
            try {
                val req = call.receive<ObjectNode>()
                println(req)
                val replyToken = req.get("events")[0].get("replyToken").toString()
                println(replyToken)
                val msg = TextMessage(text = "你好")
                val replyData = ReplyData(replyToken, listOf(msg))
                reply(replyData)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                println(e)
                // handler
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}