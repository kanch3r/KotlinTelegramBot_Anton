package org.example.learningbot.console

import org.example.learningbot.trainer.LearnWordsTrainer

fun main() {

    val consoleTrainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    while (true) {

        drawMainMenu()

        val userInput: Int? = readln().toIntOrNull()

        when (userInput) {
            1 -> consoleTrainer.learningWord()
            2 -> println("${consoleTrainer.getStatistics()}\n")
            0 -> return
            else -> println("Введите число 1, 2 или 0")
        }
    }
}

fun drawMainMenu() {
    println(
        "Меню: \n" +
                "1 – Учить слова\n" +
                "2 – Статистика\n" +
                "0 – Выход"
    )
}