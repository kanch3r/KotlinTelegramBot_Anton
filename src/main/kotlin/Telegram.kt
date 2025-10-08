package org.example

fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    var updateId = 0
    val learnWordsTrainer = LearnWordsTrainer()

    while (true) {
        Thread.sleep(2000)
        val updates = telegramBotService.getUpdates(updateId)
        println(updates)

        val resultId = TelegramUpdatesParser.getUpdateId(updates)
        if (resultId != null) {
            updateId = resultId + 1
        } else {
            continue
        }

        val resultChatId = TelegramUpdatesParser.getChatId(updates)
        val resultText = TelegramUpdatesParser.getUserChatMessage(updates)
        val resultCallBackData = TelegramUpdatesParser.getUserCallBackData(updates)

        when {
            resultText.lowercase() == START_BUTTON -> telegramBotService.sendMenu(resultChatId)

            resultCallBackData == LEARN_WORDS_BUTTON -> {
                telegramBotService.checkNextQuestionAndSend(learnWordsTrainer, resultChatId)
            }

            resultCallBackData.startsWith(CALLBACK_DATA_ANSWER_PREFIX) -> {
                telegramBotService.checkUserAnswerAndSendReply(learnWordsTrainer, resultChatId, resultCallBackData)
                Thread.sleep(500)
                telegramBotService.checkNextQuestionAndSend(learnWordsTrainer, resultChatId)
            }

            resultCallBackData == STATISTICS_BUTTON ->
                telegramBotService.sendMessage(resultChatId, "${learnWordsTrainer.getStatistics()}")

            else -> telegramBotService.sendMessage(resultChatId, resultText)
        }
    }
}