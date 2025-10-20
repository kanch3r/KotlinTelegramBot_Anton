package org.example.learningbot.console

import org.example.learningbot.trainer.LearnWordsTrainer
import org.example.learningbot.trainer.model.Question

fun Question.asConsoleString(): String {
    return "${correctAnswer.origin}:\n" +
            variants.mapIndexed { index, word ->
                "${index + 1} - ${word.translate}"
            }.joinToString("\n") +
            "\n__________\n" +
            "0 - Меню"
}

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
            3 -> println("${consoleTrainer.resetStatistics()}\n")
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
                "3 – Сбросить прогресс\n" +
                "0 – Выход"
    )
}