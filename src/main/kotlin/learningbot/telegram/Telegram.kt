package org.example.learningbot.telegram

import kotlinx.serialization.json.Json
import org.example.learningbot.telegram.api.CALLBACK_DATA_ANSWER_PREFIX
import org.example.learningbot.telegram.api.LEARN_WORDS_BUTTON
import org.example.learningbot.telegram.api.Response
import org.example.learningbot.trainer.LearnWordsTrainer
import org.example.learningbot.telegram.api.START_BUTTON
import org.example.learningbot.telegram.api.STATISTICS_BUTTON
import org.example.learningbot.telegram.api.TelegramBotService

fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    var updateId = 0L
    val json = Json {ignoreUnknownKeys = true }
    val learnWordsTrainer = LearnWordsTrainer()

    while (true) {
        Thread.sleep(2000)
        val responseJsonString = telegramBotService.getUpdates(updateId)
        println(responseJsonString)

        val response = json.decodeFromString<Response>(responseJsonString)
        val updates = response.result
        val lastUpdate = updates.lastOrNull() ?: continue
        updateId = lastUpdate.updateId + 1

        val resultChatId = lastUpdate.message?.chat?.id ?: lastUpdate.callbackQuery?.message?.chat?.id
        val resultText = lastUpdate.message?.text
        val resultCallBackData = lastUpdate.callbackQuery?.data

        when {
            resultText?.lowercase() == START_BUTTON -> telegramBotService.sendMenu(json, resultChatId)

            resultCallBackData == LEARN_WORDS_BUTTON -> {
                telegramBotService.checkNextQuestionAndSend(learnWordsTrainer, resultChatId, json)
            }

            resultCallBackData?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
                telegramBotService.checkUserAnswerAndSendReply(
                    learnWordsTrainer,
                    resultChatId,
                    resultCallBackData,
                    json
                )
                Thread.sleep(500)
                telegramBotService.checkNextQuestionAndSend(learnWordsTrainer, resultChatId, json)
            }

            resultCallBackData == STATISTICS_BUTTON ->
                telegramBotService.sendMessage(json, resultChatId, "${learnWordsTrainer.getStatistics()}")

            else -> telegramBotService.sendMessage(json, resultChatId, resultText)
        }
    }
}