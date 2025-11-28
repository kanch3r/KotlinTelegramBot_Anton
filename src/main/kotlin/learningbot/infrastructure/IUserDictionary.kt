package learningbot.infrastructure

import learningbot.trainer.model.Statistics
import learningbot.trainer.model.Word

interface IUserDictionary {
    fun loadDictionary(): List<Word>
    fun getStatistics(): Statistics
    fun setCorrectAnswersCount(word: Word)
    fun resetProgress(): Boolean
}