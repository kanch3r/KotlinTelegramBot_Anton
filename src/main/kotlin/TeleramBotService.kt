package org.example

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val POSITION_UPDATE_ID_REGEX: String = "\"update_id\":(.+?),"
const val USER_CHAT_ID_REGEX: String = "\"chat\":\\{\"id\":(.+?),\""
const val MESSAGE_TEXT_REGEX: String = "\"text\":\"(.+?)\""

data class TelegramBotService(val botToken: String) {

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
        return responseUpdates.body()
    }

    fun sendMessage(chatId: Int, textMessage: String) {
        val encodeText = URLEncoder.encode(textMessage, "UTF-8")
        val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodeText"
        val client: HttpClient = HttpClient.newBuilder().build()
        val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
    }
}

class TelegramUpdatesParser {

    fun getUpdateId(updates: String): Int? {
        val positionUpdateIdRegex = POSITION_UPDATE_ID_REGEX.toRegex()
        val allUpdateIds = positionUpdateIdRegex.findAll(updates)
        val lastUpdateIdMatch = allUpdateIds.lastOrNull()
        val groupUpdateId = lastUpdateIdMatch?.groups
        val resultId = groupUpdateId?.get(1)?.value?.toInt()
        return resultId
    }

    fun getChatId(updates: String): Int {
        val userChatIdRegex = USER_CHAT_ID_REGEX.toRegex()
        val matchChatId = userChatIdRegex.find(updates)
        val groupsChatId = matchChatId?.groups
        val resultChatId = groupsChatId?.get(1)?.value?.toInt()
            ?: throw IllegalArgumentException("Chat ID not found in updates")
        return resultChatId
    }

    fun getUserChatMessage(updates: String): String {
        val messageTextRegex = MESSAGE_TEXT_REGEX.toRegex()
        val matchResultText = messageTextRegex.find(updates)
        val groupsText = matchResultText?.groups
        val resultText = groupsText?.get(1)?.value ?: "Message not found in updates"
        return resultText
    }
}