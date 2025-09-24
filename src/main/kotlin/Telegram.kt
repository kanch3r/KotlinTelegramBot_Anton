package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0

    while (true) {
        Thread.sleep(2000)
        val updates = getUpdates(botToken, updateId)
        println(updates)
        val positionUpdateIdRegex = "\"update_id\":(.+?),".toRegex()
        val matchResultUpdateId = positionUpdateIdRegex.find(updates)
        val groupUpdateId = matchResultUpdateId?.groups
        val resultId = groupUpdateId?.get(1)?.value ?: continue
        updateId = resultId.toInt() + 1

        val messageTextRegex ="\"text\":\"(.+?)\"".toRegex()
        val matchResultText = messageTextRegex.find(updates)
        val groupsText = matchResultText?.groups
        val resultText = groupsText?.get(1)?.value
        println(resultText)
    }
}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())

    return responseUpdates.body()
}