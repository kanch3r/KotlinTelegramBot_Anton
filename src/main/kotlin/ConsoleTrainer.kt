package org.example

import java.io.File

data class Word(
    val origin: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun main() {
    val dictionary: MutableList<Word> = mutableListOf()
    val wordsFile: File = File("words.txt")
    wordsFile.forEachLine {
        val line: List<String> = it.split("|")
        val word = Word(
            origin = line[0],
            translate = line[1],
            correctAnswersCount = line[2].toIntOrNull() ?: 0
        )
        dictionary.add(word)
    }
    dictionary.forEach { println(it) }
}