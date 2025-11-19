package learningbot.infrastructure

import learningbot.trainer.DICTIONARY_SOURCE_TXT
import learningbot.trainer.NUMBER_OF_SUCCESS_TRIES
import learningbot.trainer.ONE_HUNDRED_PERCENT
import learningbot.trainer.model.Statistics
import learningbot.trainer.model.Word
import java.io.File

class FIleUserDictionary(val fileName: String = DICTIONARY_SOURCE_TXT) : IUserDictionary {

    val dictionary: List<Word> = loadDictionary()

    override fun loadDictionary(): List<Word> {
        try {
            val dictionary: MutableList<Word> = mutableListOf()
            val wordsFile = File(fileName)
            if (!wordsFile.exists()) {
                File(DICTIONARY_SOURCE_TXT).copyTo(wordsFile)
            }
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
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("некорректный файл")
        }
    }

    override fun getStatistics(): Statistics {
        val totalCount: Int = dictionary.size
        val learnedCount: Int = dictionary.count { it.correctAnswersCount >= NUMBER_OF_SUCCESS_TRIES }
        val percent: Double = if (totalCount == 0) {
            0.0
        } else {
            learnedCount * ONE_HUNDRED_PERCENT / totalCount
        }
        val roundedPercent ="%.0f".format(percent).toDouble()
        return Statistics(totalCount, learnedCount, roundedPercent)
    }

    override fun resetProgress(): String {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
        return "Ваш прогресс сброшен"
    }

    override fun setCorrectAnswersCount(word: Word) {
        val existingWord = dictionary.find { it.origin == word.origin }
        existingWord?.correctAnswersCount = word.correctAnswersCount
        saveDictionary()
    }

    private fun saveDictionary() {
        File(fileName).apply {
            this.writeText("")
            dictionary.forEach {
                appendText("${it.origin}|${it.translate}|${it.correctAnswersCount}\n")
            }
        }
    }
}