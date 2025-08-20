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

fun learningWord(workingFile: List<Word>) {
    while (true) {

        val notLearnedList = workingFile.filter { it.correctAnswersCount < NUMBER_OF_SUCCESS_TRIES }
        if (notLearnedList.count() == 0) {
            println("Все слова в словаре выучены!")
            return
        }

        val questionWords = notLearnedList.shuffled().take(4)
        val correctAnswer = questionWords.random()

        println("${correctAnswer.origin}:")
        questionWords.forEach { word ->
            println("\t${questionWords.indexOf(word) + 1} - ${word.translate}")
        }
        println("__________")
        println("0 - Меню")

        val userAnswerInput = readln().toIntOrNull()
        if (userAnswerInput == null) {
            println("Неверный ввод\n")
            continue
        }

        val userAnswerId: Int = when (userAnswerInput) {
            in 1..4 -> userAnswerInput
            0 -> return
            else -> {
                println("Неверный ввод\n")
                continue
            }
        }

        if (correctAnswer.translate == questionWords[userAnswerId - 1].translate) {
            correctAnswer.correctAnswersCount++
            println("Правильно!\n")
            saveDictionary("words.txt", workingFile)
        } else {
            println("Неправильно! ${correctAnswer.origin} - это ${correctAnswer.translate}\n")
        }
    }
}

fun saveDictionary(fileName: String, workingFile: List<Word>) {
    File(fileName).writeText(workingFile.joinToString("\n") {
        "${it.origin}|${it.translate}|${it.correctAnswersCount}"
    })
}

fun displayStatistics(workingFile: List<Word>): String {
    val totalCount: Int = workingFile.count()
    val learnedCount: Int = workingFile.count { it.correctAnswersCount >= NUMBER_OF_SUCCESS_TRIES }
    val percent: Double = learnedCount * ONE_HUNDRED_PERCENT / totalCount
    return "Выучено $learnedCount из $totalCount слов | ${"%.0f".format(percent)}%"
}

fun main() {

    while (true) {

        val dictionary: List<Word> = loadDictionary("words.txt")
        val statistics: String = displayStatistics(dictionary)
        println(
            "Меню: \n" +
                    "1 – Учить слова\n" +
                    "2 – Статистика\n" +
                    "0 – Выход"
        )
        val userInput: Int? = readln().toIntOrNull()

        when (userInput) {
            1 -> learningWord(dictionary)
            2 -> println("$statistics\n")
            0 -> return
            else -> println("Введите число 1, 2 или 0")
        }
    }
}