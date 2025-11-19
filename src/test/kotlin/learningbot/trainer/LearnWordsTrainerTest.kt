package learningbot.trainer

import learningbot.trainer.model.Statistics
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LearnWordsTrainerTest {

    @Test
    fun `test statistics with 4 words of 7`() {
        val trainer = LearnWordsTrainer(fileNameTxt = "4_words_of_7.txt", useDataBase = false)

        assertEquals(
            Statistics(learned = 4, total = 7, percent = 57.0),
            trainer.getStatistics()
        )
    }

    fun `test statistics with corrupted file`() {
        val trainer = LearnWordsTrainer(fileNameTxt = "4_words_of_7.txt", useDataBase = false)
        // подумать

    }

    @Test
    fun `test getNextQuestion() with 5 unlearned words`() {
        val trainer = LearnWordsTrainer(fileNameTxt = "4_words_of_7.txt", useDataBase = false)

        // поправить
        assertEquals(
            5,
            trainer.getNextQuestion()?.variants?.size
        )
    }

    fun `test getNextQuestion() with all words learned`() {
        val trainer = LearnWordsTrainer(fileNameTxt = "4_words_of_7.txt", useDataBase = false)

        // сделать

    }


}