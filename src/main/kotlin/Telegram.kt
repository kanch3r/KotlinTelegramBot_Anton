package org.example

fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    var updateId = 0
    val consoleTrainer = ConsoleTrainer()

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
            resultText.lowercase() == "/start" -> telegramBotService.sendMenu(resultChatId)
            resultCallBackData.lowercase() == "learn_words_button" -> telegramBotService.sendMessage(
                resultChatId,
                "начать учить слова"
            )
            resultCallBackData.lowercase() == "statistics_button" -> telegramBotService.sendMessage(
                resultChatId,
                "показывать статистику"
            )
            else -> telegramBotService.sendMessage(resultChatId, resultText)
        }
    }
}