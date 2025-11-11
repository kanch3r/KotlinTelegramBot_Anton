package learningbot.telegram.api

import kotlinx.serialization.json.Json
import learningbot.trainer.LearnWordsTrainer
import learningbot.trainer.model.Question
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val START_BUTTON: String = "/start"
const val LEARN_WORDS_BUTTON: String = "learn_words_button"
const val STATISTICS_BUTTON: String = "statistics_button"
const val RESET_STATISTICS_BUTTON: String = "reset_statistics_button"
const val CALLBACK_DATA_ANSWER_PREFIX: String = "answer_"
const val CALLBACK_DATA_RETURN_TO_MENU: String = "return"
const val QTY_OF_WORDS_IN_A_ROW: Int = 1

data class TelegramBotService(val botToken: String) {
    companion object {
        const val BASE_URL_TELEGRAM_API: String = "https://api.telegram.org/bot"
        const val GET_UPDATES_METHOD: String = "getUpdates"
        const val SEND_MESSAGE_METHOD: String = "sendMessage"
        const val DELETE_MESSAGE_METHOD: String = "deleteMessage"
    }

    val client: HttpClient = HttpClient.newBuilder().build()
    val json = Json

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$BASE_URL_TELEGRAM_API$botToken/${GET_UPDATES_METHOD}?offset=$updateId"
        val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
        return responseUpdates.body()
    }

    fun processingUpdate(update: Update, trainers: HashMap<Long, LearnWordsTrainer>) {
        val resultChatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
        val resultText = update.message?.text
        val resultCallBackData = update.callbackQuery?.data
        val resultMessageId = update.message?.messageId ?: update.callbackQuery?.message?.messageId ?: return

        val personalTrainer = trainers.getOrPut(resultChatId) {
            LearnWordsTrainer(chatId = resultChatId)
        }

        when {
            resultText?.lowercase() == START_BUTTON -> sendMenu(resultChatId)

            resultCallBackData == LEARN_WORDS_BUTTON -> {
                checkNextQuestionAndSend(personalTrainer, resultChatId)
            }

            resultCallBackData?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
                checkUserAnswerAndSendReply(
                    personalTrainer,
                    resultChatId,
                    resultMessageId,
                    resultCallBackData
                )
                Thread.sleep(500)
                deleteMessage(resultChatId, resultMessageId.plus(1))
                checkNextQuestionAndSend(personalTrainer, resultChatId)
            }

            resultCallBackData == STATISTICS_BUTTON -> {
                sendMessage(resultChatId, "${personalTrainer.getStatistics()}")
            }

            resultCallBackData == RESET_STATISTICS_BUTTON -> {
                sendMessage(resultChatId, personalTrainer.resetStatistics())
            }

            resultCallBackData == CALLBACK_DATA_RETURN_TO_MENU -> {
                sendMenu(resultChatId)
                Thread.sleep(500)
                deleteMessage(resultChatId, resultMessageId)
            }

            else -> sendMessage(resultChatId, resultText)
        }
    }

    fun sendMessage(chatId: Long, textMessage: String?): String {
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

    fun deleteMessage(chatId: Long, messageId: Int): String {
        val urlDeleteMessage = "$BASE_URL_TELEGRAM_API$botToken/$DELETE_MESSAGE_METHOD"

        val requestBody = DeleteMessage(
            chatId = chatId,
            messageId = messageId
        )

        val requestBodyString = json.encodeToString(requestBody)

        val requestUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlDeleteMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val responseUpdates: HttpResponse<String> = client.send(requestUpdates, HttpResponse.BodyHandlers.ofString())
        return responseUpdates.body()
    }

    fun sendMenu(chatId: Long): String {
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
                    ),
                    listOf(
                        InlineKeyboard(
                            text = "Сбросить прогресс",
                            callbackData = RESET_STATISTICS_BUTTON
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

    fun sendQuestion(chatId: Long, question: Question): String {
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
                }.chunked(QTY_OF_WORDS_IN_A_ROW) +
                        listOf(
                            listOf(
                                InlineKeyboard(
                                    text = "Назад в меню",
                                    callbackData = CALLBACK_DATA_RETURN_TO_MENU
                                )
                            )
                        )
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
        chatId: Long,
    ) {
        val nextQuestion = trainer.getNextQuestion()
        trainer.questionWord = nextQuestion
        if (nextQuestion == null) {
            sendMessage(chatId, "Все слова выучены!")
            return
        } else {
            sendQuestion(chatId, nextQuestion)
        }
    }

    fun checkUserAnswerAndSendReply(
        trainer: LearnWordsTrainer,
        chatId: Long,
        messageId: Int,
        callBackData: String?,
    ) {
        val correctAnswer = trainer.questionWord?.correctAnswer
        val callBackDataIndex = callBackData?.substringAfter(CALLBACK_DATA_ANSWER_PREFIX)?.toInt()
        if (trainer.checkAnswer(callBackDataIndex)) {
            sendMessage(chatId, "Правильно!")
        } else {
            sendMessage(chatId, "Неправильно! ${correctAnswer?.origin} - это ${correctAnswer?.translate}")
        }
        Thread.sleep(500)
        deleteMessage(chatId, messageId)
    }
}