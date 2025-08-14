package org.example

import java.io.File

const val NUMBER_OF_SUCCESS_TRIES: Int = 3
const val ONE_HUNDRED_PERCENT: Double = 100.0

data class Word(
    val origin: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun loadDictionary(fileName: String): List<Word> {
    val dictionary: MutableList<Word> = mutableListOf()
    val wordsFile: File = File(fileName)
    wordsFile.forEachLine {
        val line: List<String> = it.split("|")
        val word = Word(
            origin = line[0],
            translate = line[1],
            correctAnswersCount = line[2].toIntOrNull() ?: 0
        )
        dictionary.add(word)
    }
    return dictionary
}

fun displayStatistics(workingFile: List<Word>): String {
    val totalCount: Int = workingFile.count()
    val learnedCount: Int = workingFile.filter { it.correctAnswersCount >= NUMBER_OF_SUCCESS_TRIES }.count()
    val percent: Double = learnedCount * ONE_HUNDRED_PERCENT / totalCount
    return "Выучено $learnedCount из $totalCount слов | ${"%.0f".format(percent)}%"
}

fun main() {
    val dictionary: List<Word> = loadDictionary("words.txt")
    val statistics: String = displayStatistics(dictionary)

    while (true) {
        println(
            "Меню: \n" +
                    "1 – Учить слова\n" +
                    "2 – Статистика\n" +
                    "0 – Выход"
        )
        val userInput: Int? = readln().toIntOrNull()

        when (userInput) {
            1 -> println("Вы выбрали учить слова.")
            2 -> println("$statistics\n")
            0 -> return
            else -> println("Введите число 1, 2 или 0")
        }
    }
}