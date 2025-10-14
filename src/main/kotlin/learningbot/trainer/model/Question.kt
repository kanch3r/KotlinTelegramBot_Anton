package org.example.learningbot.trainer.model

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

fun Question.asConsoleString(): String {
    return "${correctAnswer.origin}:\n" +
            variants.mapIndexed { index, word ->
                "${index + 1} - ${word.translate}"
            }.joinToString("\n") +
            "\n__________\n" +
            "0 - Меню"
}