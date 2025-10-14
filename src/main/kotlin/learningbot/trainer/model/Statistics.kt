package org.example.learningbot.trainer.model

data class Statistics(
    val total: Int,
    val learned: Int,
    val percent: Double,
) {
    override fun toString() =
        "Выучено $learned из $total слов | ${"%.0f".format(percent)}%"
}