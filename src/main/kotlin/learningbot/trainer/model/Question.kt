package org.example.learningbot.trainer.model

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)