package org.example.learningbot.telegram.api

import org.example.learningbot.trainer.LearnWordsTrainer
import org.example.learningbot.trainer.model.Question
import java.net.URI
import java.net.URLEncoder
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

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$BASE_URL_TELEGRAM_API$botToken/${GET_UPDATES_METHOD}?offset=$updateId"
        val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
        return responseUpdates.body()
    }

    fun sendMessage(chatId: Int, textMessage: String): String {
        val encodeText = URLEncoder.encode(textMessage, "UTF-8")
        val urlSendMessage = "$BASE_URL_TELEGRAM_API$botToken/$SEND_MESSAGE_METHOD?chat_id=$chatId&text=$encodeText"
        val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
        return responseUpdates.body()
    }

    fun sendMenu(chatId: Int): String {
        val urlSendMessage = "$BASE_URL_TELEGRAM_API$botToken/$SEND_MESSAGE_METHOD"
        val mainMenuBody = """
            {
            	"chat_id": $chatId,
            	"text": "Main menu",
            	"reply_markup": {
            		"inline_keyboard": [
            			[
            				{
            					"text": "learn words",
            					"callback_data": "$LEARN_WORDS_BUTTON"
            				},
            				{
            					"text": "statistics",
            					"callback_data": "$STATISTICS_BUTTON"
            				}
            			]
            		]
            	}
            }
        """.trimIndent()

        val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(mainMenuBody))
            .build()

        val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
        return responseUpdates.body()
    }

    fun sendQuestion(chatId: Int, question: Question?): String {
        val urlSendMessage = "$BASE_URL_TELEGRAM_API$botToken/$SEND_MESSAGE_METHOD"
        val questionWord = question?.correctAnswer?.origin
        val buttons = question?.variants?.mapIndexed { index, word ->
            """
            {
            	"text": "${word.translate}",
            	"callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${index}"
            }
            """.trimIndent()
        }

        val questionBody = """
            {
            	"chat_id": $chatId,
            	"text": "$questionWord",
            	"reply_markup": {
            		"inline_keyboard":
                    [
            			${buttons?.chunked(QTY_OF_WORDS_IN_A_ROW)?.joinToString(",\n")}
            		]
            	}
            }
        """.trimIndent()

        val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlSendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(questionBody))
            .build()

        val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
        return responseUpdates.body()
    }

    fun checkNextQuestionAndSend(
        trainer: LearnWordsTrainer,
        chatId: Int,
    ) {
        trainer.questionWord = trainer.getNextQuestion()
        if (trainer.questionWord == null) {
            sendMessage(chatId, "Все слова выучены!")
            return
        } else {
            sendQuestion(chatId, trainer.questionWord)
        }
    }

    fun checkUserAnswerAndSendReply(
        trainer: LearnWordsTrainer,
        chatId: Int,
        callBackData: String,
    ) {
        val correctAnswer = trainer.questionWord?.correctAnswer
        val callBackDataIndex = callBackData.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
        if (trainer.checkAnswer(callBackDataIndex)) {
            sendMessage(chatId, "Правильно!")
        } else {
            sendMessage(chatId, "Неправильно! ${correctAnswer?.origin} - это ${correctAnswer?.translate}")
        }
    }
}