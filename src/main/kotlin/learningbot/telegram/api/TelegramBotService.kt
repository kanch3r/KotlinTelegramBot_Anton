package org.example.learningbot.telegram.api

import kotlinx.serialization.json.Json
import org.example.learningbot.trainer.LearnWordsTrainer
import org.example.learningbot.trainer.model.Question
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val START_BUTTON: String = "/start"
const val LEARN_WORDS_BUTTON: String = "learn_words_button"
const val STATISTICS_BUTTON: String = "statistics_button"
const val CALLBACK_DATA_ANSWER_PREFIX: String = "answer_"
const val QTY_OF_WORDS_IN_A_ROW: Int = 2

data class TelegramBotService(val botToken: String) {
    companion object {
        const val BASE_URL_TELEGRAM_API: String = "https://api.telegram.org/bot"
        const val GET_UPDATES_METHOD: String = "getUpdates"
        const val SEND_MESSAGE_METHOD: String = "sendMessage"
    }

    val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$BASE_URL_TELEGRAM_API$botToken/${GET_UPDATES_METHOD}?offset=$updateId"
        val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
        return responseUpdates.body()
    }

    fun sendMessage(json: Json, chatId: Long?, textMessage: String?): String {
        val urlSendMessage = "$BASE_URL_TELEGRAM_API$botToken/$SEND_MESSAGE_METHOD"

        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = textMessage,
        )

        val requestBodyString = json.encodeToString(requestBody)

        val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
        return responseUpdates.body()
    }

    fun sendMenu(json: Json, chatId: Long?): String {
        val urlSendMessage = "$BASE_URL_TELEGRAM_API$botToken/$SEND_MESSAGE_METHOD"

        val requestMainMenuBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(
                            text = "Учить слова",
                            callbackData = LEARN_WORDS_BUTTON
                        ),
                        InlineKeyboard(
                            text = "Статистика",
                            callbackData = STATISTICS_BUTTON
                        )
                    )
                )
            )
        )

        val requestMainMenuBodyString = json.encodeToString(requestMainMenuBody)

        val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestMainMenuBodyString))
            .build()

        val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
        return responseUpdates.body()
    }

    fun sendQuestion(json: Json, chatId: Long?, question: Question): String {
        val urlSendMessage = "$BASE_URL_TELEGRAM_API$botToken/$SEND_MESSAGE_METHOD"
        val questionWord = question.correctAnswer.origin

        val requestQuestion = SendMessageRequest(
            chatId = chatId,
            text = questionWord,
            replyMarkup = ReplyMarkup(
                question.variants.mapIndexed { index, word ->
                    InlineKeyboard(
                        text = word.translate,
                        callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                    )
                }.chunked(QTY_OF_WORDS_IN_A_ROW)
            )
        )

        val requestQuestionBody = json.encodeToString(requestQuestion)

        val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestQuestionBody))
            .build()

        val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
        return responseUpdates.body()
    }

    fun checkNextQuestionAndSend(
        trainer: LearnWordsTrainer,
        chatId: Long?,
        json: Json,
    ) {
        val nextQuestion = trainer.getNextQuestion()
        trainer.questionWord = nextQuestion
        if (nextQuestion == null) {
            sendMessage(json, chatId, "Все слова выучены!")
            return
        } else {
            sendQuestion(json, chatId, nextQuestion)
        }
    }

    fun checkUserAnswerAndSendReply(
        trainer: LearnWordsTrainer,
        chatId: Long?,
        callBackData: String?,
        json: Json,
    ) {
        val correctAnswer = trainer.questionWord?.correctAnswer
        val callBackDataIndex = callBackData?.substringAfter(CALLBACK_DATA_ANSWER_PREFIX)?.toInt()
        if (trainer.checkAnswer(callBackDataIndex)) {
            sendMessage(json, chatId, "Правильно!")
        } else {
            sendMessage(json, chatId, "Неправильно! ${correctAnswer?.origin} - это ${correctAnswer?.translate}")
        }
    }
}