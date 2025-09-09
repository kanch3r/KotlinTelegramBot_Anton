package org.example

fun main() {

    val consoleTrainer = ConsoleTrainer()

    while (true) {

        drawMainMenu()

        val userInput: Int? = readln().toIntOrNull()

        when (userInput) {
            1 -> consoleTrainer.learningWord()
            2 -> println("${consoleTrainer.displayStatistics()}\n")
            0 -> return
            else -> println("Введите число 1, 2 или 0")
        }
    }
}