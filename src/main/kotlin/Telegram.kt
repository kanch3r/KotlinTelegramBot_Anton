package org.example

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val POSITION_UPDATE_ID_REGEX: String = "\"update_id\":(.+?),"
const val USER_CHAT_ID_REGEX: String = "\"chat\":\\{\"id\":(.+?),\""
const val MESSAGE_TEXT_REGEX: String = "\"text\":\"(.+?)\""

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0
    val positionUpdateIdRegex = POSITION_UPDATE_ID_REGEX.toRegex()
    val userChatIdRegex = USER_CHAT_ID_REGEX.toRegex()
    val messageTextRegex = MESSAGE_TEXT_REGEX.toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates = getUpdates(botToken, updateId)
        println(updates)

        val resultId = getUpdateId(updates, positionUpdateIdRegex)
        if (resultId != null) {
            updateId = resultId.toInt() + 1
        } else {
            continue
        }

        val resultChatId = getChatId(updates, userChatIdRegex)
        println(resultChatId)

        val resultText = getUserChatMessage(updates, messageTextRegex)
        println(resultText)

        sendMessage(botToken, resultChatId, resultText)
        println("Пользователю с ID $resultChatId было отправлено сообщение: $resultText")
    }
}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())

    return responseUpdates.body()
}

fun getUpdateId(updates: String, positionUpdateIdRegex: Regex): String? {
    val allUpdateIds = positionUpdateIdRegex.findAll(updates)
    val lastUpdateIdMatch = allUpdateIds.lastOrNull()
    val groupUpdateId = lastUpdateIdMatch?.groups
    val resultId = groupUpdateId?.get(1)?.value
    return resultId
}

fun getChatId(updates: String, userChatIdRegex: Regex): Int {
    val matchChatId = userChatIdRegex.find(updates)
    val groupsChatId = matchChatId?.groups
    val resultChatId = groupsChatId?.get(1)?.value?.toInt()
        ?: throw IllegalArgumentException("Chat ID not found in updates")
    return resultChatId
}

fun getUserChatMessage(updates: String, messageTextRegex: Regex): String {
    val matchResultText = messageTextRegex.find(updates)
    val groupsText = matchResultText?.groups
    val resultText = groupsText?.get(1)?.value ?: "Message not found in updates"
    return resultText
}

fun sendMessage(botToken: String, chatId: Int, textMessage: String) {
    val encodeText = URLEncoder.encode(textMessage, "UTF-8")
    val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodeText"
    val client: HttpClient = HttpClient.newBuilder().build()
    val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
    val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
}