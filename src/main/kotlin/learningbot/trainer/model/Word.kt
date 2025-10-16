package org.example.learningbot.trainer.model

import kotlinx.serialization.Serializable

@Serializable
data class Word(
    val origin: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)