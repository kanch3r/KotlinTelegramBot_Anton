package org.example

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

data class TelegramBotService(val botToken: String) {
    companion object {
        const val BASE_URL_TELEGRAM_API: String = "https://api.telegram.org/bot"
        const val GET_UPDATES_METHOD: String = "getUpdates"
        const val SEND_MESSAGE_METHOD: String = "sendMessage"
    }

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$BASE_URL_TELEGRAM_API$botToken/${GET_UPDATES_METHOD}?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
        return responseUpdates.body()
    }

    fun sendMessage(chatId: Int, textMessage: String) {
        val encodeText = URLEncoder.encode(textMessage, "UTF-8")
        val urlSendMessage = "$BASE_URL_TELEGRAM_API$botToken/$SEND_MESSAGE_METHOD?chat_id=$chatId&text=$encodeText"
        val client: HttpClient = HttpClient.newBuilder().build()
        val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
    }
}