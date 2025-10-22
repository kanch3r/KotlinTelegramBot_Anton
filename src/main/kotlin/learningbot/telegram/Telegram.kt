package org.example.learningbot.telegram

import kotlinx.serialization.json.Json
import org.example.learningbot.telegram.api.Response
import org.example.learningbot.trainer.LearnWordsTrainer
import org.example.learningbot.telegram.api.TelegramBotService

fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    var updateId = 0L
    val json = Json { ignoreUnknownKeys = true }
    val trainers = hashMapOf<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(2000)
        val responseJsonString = telegramBotService.getUpdates(updateId)
        println("Это сырой update - $responseJsonString")

        val response = json.decodeFromString<Response>(responseJsonString)
        if (response.result.isEmpty()) continue

        val sortedLastUpdates = response.result
            .associateBy { it.message?.chat?.id }
            .values
        sortedLastUpdates.forEach {
            telegramBotService.processingUpdate(it, json, trainers)
        }

        updateId = sortedLastUpdates.last().updateId + 1
    }
}