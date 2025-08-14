package org.example

import java.io.File

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

fun main() {
    val dictionary: List<Word> = loadDictionary("words.txt")

    while (true) {
        println(
            "Меню: \n" +
                    "1 – Учить слова\n" +
                    "2 – Статистика\n" +
                    "0 – Выход"
        )
        val userInput = readln().toIntOrNull()

        when (userInput) {
            1 -> println("Вы выбрали учить слова.")
            2 -> println("Вы выбрали просмотр статистики.")
            0 -> return
            else -> println("Введите число 1, 2 или 0")
        }
    }
}