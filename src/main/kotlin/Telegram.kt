package org.example

fun main(args: Array<String>) {

    val botToken = args[0]
    val telegramBotService = TelegramBotService(botToken)
    val telegramUpdatesParser = TelegramUpdatesParser()
    var updateId = 0

    while (true) {
        Thread.sleep(2000)
        val updates = telegramBotService.getUpdates(updateId)
        println(updates)

        val resultId = telegramUpdatesParser.getUpdateId(updates)
        if (resultId != null) {
            updateId = resultId + 1
        } else {
            continue
        }

        val resultChatId = telegramUpdatesParser.getChatId(updates)
        val resultText = telegramUpdatesParser.getUserChatMessage(updates)

        telegramBotService.sendMessage(resultChatId, resultText)
        println("Пользователю с ID $resultChatId было отправлено сообщение: $resultText")
    }
}