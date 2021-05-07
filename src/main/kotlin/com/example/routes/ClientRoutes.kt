package com.example.routes

import com.example.Object.HTMLParser
import com.example.models.SubscriptionRequest
import com.example.models.SubscriptionResponse
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.application.*
import io.ktor.routing.*

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*


val client = HttpClient(CIO) {
    install(Logging)
    install(JsonFeature) {
        serializer = JacksonSerializer()
    }
}

suspend fun search(subscription: SubscriptionRequest): List<SubscriptionResponse> {
    val response: String = client.get("https://www.ptt.cc/bbs/${subscription.boardName.toUpperCase()}/index.html")
    val elements = HTMLParser.getElementsByClass(response, "r-ent")
        .filter {
            var isMatch = true
            if (subscription.keyword !== null) {
                val title = it.getElementsByClass("title")
                isMatch = title.text().contains(subscription.keyword)
            }
            isMatch
        }
        .filter {
            var isMatch = true
            if (subscription.author !== null) {
                val author = it.getElementsByClass("author")
                isMatch = author.text().equals(subscription.author)
            }
            isMatch
        }
        .filter {
            it.getElementsByTag("a").attr("href").isNotEmpty()
        }

    val responseData = mutableListOf<SubscriptionResponse>()
    for (element in elements) {
        val data = SubscriptionResponse(
            board = subscription.boardName,
            title = element.getElementsByClass("title").text(),
            author = element.getElementsByClass("author").text(),
            href = "https://www.ptt.cc" + element.getElementsByTag("a").attr("href"),
            date = element.getElementsByClass("date").text()
        )
        responseData.add(data)
    }
    return responseData
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TelegramChatData(val id: Int)
@JsonIgnoreProperties(ignoreUnknown = true)
data class TelegramMessage(val chat: TelegramChatData)
@JsonIgnoreProperties(ignoreUnknown = true)
data class TelegramData(val update_id: String, val message: TelegramMessage)

fun Route.clientRouting() {
    route("/client") {
        get {
            val response: String = client.get("https://www.ptt.cc/bbs/DIABLO/index.html") {
                // Configure request parameters exposed by HttpRequestBuilder
            }
            val elements = HTMLParser.getElementsByClass(response, "title")
                .filter {
                    val isMatch = it.text().contains("美西")
                    println(isMatch)
                    isMatch
                }
            for (element in elements) {
                val aTag = element.getElementsByTag("a")
                println("element")
                println(aTag)
                //取出href
                val href = aTag.attr("href")
                val text = aTag.text()
                println(href)
                println(text)
            }

            client.close()
            call.respondText("Succeeded", status = HttpStatusCode.OK)
        }

        post {
            val subscription = call.receive<SubscriptionRequest>()
            val subscriptionStorage = search(subscription)
            if (subscriptionStorage.isNotEmpty()) {
                call.respond(subscriptionStorage)
            } else {
                call.respondText("not found", status = HttpStatusCode.NotFound)
            }

        }


        post("/webhook") {
            try {
                val req = call.receive<String>()
                println(req)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                println(e)
                // handler
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}