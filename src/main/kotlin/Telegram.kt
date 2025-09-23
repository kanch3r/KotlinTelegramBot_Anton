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
        val positionUpdateId = updates.lastIndexOf("update_id")
        val startUpdateId = updates.indexOf(":", positionUpdateId)
        val endUpdateId = updates.indexOf(",\n\"message\"", startUpdateId)
        if (startUpdateId == -1 || endUpdateId == -1) continue
        val resultUpdateId = updates.substring(startUpdateId + 1, endUpdateId)
        updateId = resultUpdateId.toInt() + 1
    }
}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())

    return responseUpdates.body()
}