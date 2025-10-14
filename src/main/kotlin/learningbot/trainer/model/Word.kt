package org.example.learningbot.trainer.model

data class Word(
    val origin: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)