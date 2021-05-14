package com.example.routes

import com.example.models.FileUploader
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.ktor.application.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper


val ChannelAccessToken =
    "4413tovpM75EOhW9E8ql+RW7h3YIxW20PjZG6aKkFkP+5ckXLeCwQIfoUAGjYIvrqTfAn1O7P5oCQiHytg9tdQzCG+0pemdLtjFz5p5bQMIKk6pa2yYktMCX8u2qxEK3/RruGSBjAkx2YfnULGrdrgdB04t89/1O/w1cDnyilFU="

data class QuickReplyAction(val type: String, val label: String, val text: String? = null)
data class QuickReplyItem(val type: String, val imageUrl: String? = null, val action: QuickReplyAction)
data class QuickReply(val items: List<QuickReplyItem>)

data class Message(val type: String = "text", val text: String, val quickReply: QuickReply? = null)
data class ReplyData(val replyToken: String, val messages: List<Message>)


fun makeAQuickReply(): Message {

    val quickReplyItemList = mutableListOf<QuickReplyItem>()
    quickReplyItemList.add(QuickReplyItem("action", action = QuickReplyAction("message", "Sushi", "Sushi")))
    quickReplyItemList.add(QuickReplyItem("action", action = QuickReplyAction("message", "Tempura", "Tempura")))
    quickReplyItemList.add(QuickReplyItem("action", action = QuickReplyAction("location", "Send location")))
    val quickReply = Message(text = "你今天想去哪吃飯？", quickReply = QuickReply(quickReplyItemList))
    println(jacksonObjectMapper().writeValueAsString(quickReply))
    return quickReply

}


suspend fun reply(reply: ReplyData) {
    try {
        val replyRes = client.post<Unit>("https://api.line.me/v2/bot/message/reply") {
            headers.append("Authorization", "Bearer ${ChannelAccessToken}")
            contentType(ContentType.Application.Json)
            println(reply)
            println(jacksonObjectMapper().writeValueAsString(reply))
            body = reply
        }

        println(replyRes)
    } catch (e: Exception) {
        println(e)
    }
}

val MENU_1 = "richmenu-14a5ad4900a79d55c8246caf7591be5a"
val MENU_2 = "richmenu-0ab9bc77e1dea1d8e96a07958a0c7c7e"

enum class Action {
    Back,
    GO_MY_SUB
}

suspend fun switchRichMenu(userId: String, action: Action) {
    try {

        val richMenuId: String = when (action) {
            Action.Back -> MENU_1
            Action.GO_MY_SUB -> MENU_2
        }

        val replyRes = client.post<Unit>("https://api.line.me/v2/bot/user/${userId}/richmenu/${richMenuId}") {
            headers.append("Authorization", "Bearer ${ChannelAccessToken}")
        }

        println(replyRes)
    } catch (e: Exception) {
        println(e)
    }
}

suspend fun getContent(messageId: String = "14047809905865") {
    try {

        val res = client.get<ByteArray>("https://api-data.line.me/v2/bot/message/${messageId}/content") {
            headers.append("Authorization", "Bearer ${ChannelAccessToken}")
        }
        println("img:")
        println(res)
        FileUploader.upload(res)
    } catch (e: Exception) {

    }
}

var isQuite: Boolean = false

fun Route.webhookRoutes() {
    route("webhook") {
        post {
            try {
                val req = call.receive<ObjectNode>()
                val data = req.get("events")[0]
                val type = data.get("type").asText()
                println(data)
                if (type == "postback") {
                    // richMenu
                    val userId: String = data.get("source").get("userId").asText()
                    when (data.get("postback").get("data").asText()) {
                        "mysub" -> switchRichMenu(userId, Action.GO_MY_SUB)
                        "back" -> switchRichMenu(userId, Action.Back)
                    }
                }

                if (type == "message" && data.get("message").get("type").asText() == "image") {
                    getContent(data.get("message").get("id").asText())
                }

                val replyToken = req.get("events")[0].get("replyToken").asText()
                val textFromUser = req.get("events")[0].get("message").get("text").asText()
                val msg = Message(text = textFromUser)
                val replyData = ReplyData(replyToken, listOf(msg, makeAQuickReply()))

                println("hi")


                when (textFromUser) {
                    "安靜" -> {
                        isQuite = true
                        reply(ReplyData(replyToken, listOf(Message(text = "好哦！ 我安靜ＱＡＱ"))))
                    }
                    "講話" -> {
                        isQuite = false
                        reply(ReplyData(replyToken, listOf(Message(text = "解放！ 大吵大鬧模式啟動！"))))
                    }
                    else -> {
                        if (!isQuite) reply(replyData)
                    }
                }


//                println(req)
//                val mockingbird = req.get("events")[0].get("message").get("text").asText()
//                println(replyToken)
//                val msg = TextMessage(text = mockingbird)
//                val replyData = ReplyData(replyToken, listOf(msg))
//                reply(replyData)
//                handleWebhook(type, data)

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                println(e)
                // handler
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}