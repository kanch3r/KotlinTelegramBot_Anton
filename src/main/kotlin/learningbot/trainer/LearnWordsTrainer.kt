package learningbot.trainer

import learningbot.trainer.model.Question
import learningbot.trainer.model.Statistics
import learningbot.trainer.model.Word
import learningbot.console.asConsoleString
import java.io.File

const val NUMBER_OF_SUCCESS_TRIES: Int = 3
const val ONE_HUNDRED_PERCENT: Double = 100.0
const val QUANTITY_OF_ANSWERS: Int = 4
const val DICTIONARY_SOURCE: String = "words.txt"

class LearnWordsTrainer(val fileName: String = DICTIONARY_SOURCE) {
    var questionWord: Question? = null
    private val dictionary: List<Word> = loadDictionary()

    fun learningWord() {
        while (true) {
            questionWord = getNextQuestion()
            if (questionWord == null) {
                println("Все слова в словаре выучены!\n")
                return
            } else {
                println(questionWord?.asConsoleString())
                val userAnswerInput = readln().toIntOrNull()
                when (userAnswerInput) {
                    in 1..QUANTITY_OF_ANSWERS -> {
                        if (checkAnswer(userAnswerInput?.minus(1))) {
                            println("Правильно!\n")
                        } else {
                            println(
                                "Неправильно! ${questionWord?.correctAnswer?.origin} " +
                                        "- это ${questionWord?.correctAnswer?.translate}\n"
                            )
                        }
                    }

                    0 -> return
                    else -> println("Введите вариант ответа от 1 до 4, или 0 для выхода.\n")
                }
            }
        }
    }

    fun getStatistics(): Statistics {
        val totalCount: Int = dictionary.size
        val learnedCount: Int = dictionary.count { it.correctAnswersCount >= NUMBER_OF_SUCCESS_TRIES }
        val percent: Double = if (totalCount == 0) {
            0.0
        } else {
            learnedCount * ONE_HUNDRED_PERCENT / totalCount
        }
        return Statistics(totalCount, learnedCount, percent)
    }

    fun resetStatistics(): String {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
        return "Ваш прогресс сброшен"
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < NUMBER_OF_SUCCESS_TRIES }
        if (notLearnedList.isEmpty()) return null
        val questionWords = notLearnedList.shuffled().take(QUANTITY_OF_ANSWERS).toMutableList()
        if (questionWords.size < QUANTITY_OF_ANSWERS) {
            questionWords.addAll(
                dictionary
                    .filter { it !in questionWords }
                    .shuffled().take(QUANTITY_OF_ANSWERS - questionWords.size))
        }
        val correctAnswer = questionWords.random()
        return Question(
            variants = questionWords.shuffled(),
            correctAnswer = correctAnswer
        )
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        val correctAnswerId = questionWord?.variants?.indexOf(questionWord?.correctAnswer)
        if (userAnswerIndex == null) return false
        val userAnswerId: Int = when (userAnswerIndex) {
            in 0 until QUANTITY_OF_ANSWERS -> userAnswerIndex
            else -> return false
        }
        if (correctAnswerId == userAnswerId) {
            questionWord?.correctAnswer?.correctAnswersCount++
            saveDictionary()
            return true
        } else {
            return false
        }
    }

    private fun loadDictionary(): List<Word> {
        try {
            val dictionary: MutableList<Word> = mutableListOf()
            val wordsFile = File(fileName)
            if (!wordsFile.exists()) {
                File(DICTIONARY_SOURCE).copyTo(wordsFile)
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

    private fun saveDictionary() {
        File(fileName).apply {
            this.writeText("")
            dictionary.forEach {
                appendText("${it.origin}|${it.translate}|${it.correctAnswersCount}\n")
            }
        }
    }
}