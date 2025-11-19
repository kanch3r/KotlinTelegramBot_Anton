package learningbot.console

import learningbot.trainer.LearnWordsTrainer
import learningbot.trainer.model.Question

fun Question.asConsoleString(): String {
    val variantsWords = variants
        .mapIndexed { index, word -> "${index + 1} - ${word.translate}" }
        .joinToString("\n")

    val questionWithVariants = "${correctAnswer.origin}:\n${variantsWords}\n"
    val bottomBorderWithMenu = "__________\n0 - Меню"
    return questionWithVariants + bottomBorderWithMenu
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
            else -> println("Введите число 1, 2, 3 или 0")
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