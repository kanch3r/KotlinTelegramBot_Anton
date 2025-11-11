package learningbot.trainer

import learningbot.infrastructure.DatabaseUserDictionary
import learningbot.infrastructure.FIleUserDictionary
import learningbot.infrastructure.IUserDictionary
import learningbot.trainer.model.Question
import learningbot.trainer.model.Word
import learningbot.console.asConsoleString

const val NUMBER_OF_SUCCESS_TRIES: Int = 3
const val ONE_HUNDRED_PERCENT: Double = 100.0
const val QUANTITY_OF_ANSWERS: Int = 4
const val DICTIONARY_SOURCE_TXT: String = "words.txt"
const val DICTIONARY_SOURCE_DB: String = "wordsDataBase.db"
const val DATABASE_INFRASTRUCTURE: Boolean = true // true - use Database.db; false - use file.txt

class LearnWordsTrainer(
    val useDataBase: Boolean = DATABASE_INFRASTRUCTURE,
    val fileNameTxt: String = DICTIONARY_SOURCE_TXT,
    val chatId: Long = 0L,
) {

    private val methodDictionary: IUserDictionary = if (useDataBase) {
        DatabaseUserDictionary(chatId)
    } else {
        FIleUserDictionary(fileNameTxt)
    }

    var questionWord: Question? = null
    private val dictionary: List<Word> = methodDictionary.loadDictionary()

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
            val correctAnswer = questionWord?.correctAnswer
            if (correctAnswer != null) {
                methodDictionary.setCorrectAnswersCount(correctAnswer)
            }
            return true
        } else {
            return false
        }
    }

    fun getStatistics() = methodDictionary.getStatistics()

    fun resetStatistics() = methodDictionary.resetProgress()
}