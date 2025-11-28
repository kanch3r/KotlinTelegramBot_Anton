package learningbot.trainer

import learningbot.trainer.model.Statistics
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.io.path.createTempFile

class LearnWordsTrainerTest {

    @Test
    fun `test statistics with 4 learned words of 7`() {
        val trainer = LearnWordsTrainer(fileNameTxt = "4_words_of_7.txt", useDataBase = false)

        assertEquals(
            Statistics(learned = 4, total = 7, percent = 57.0),
            trainer.getStatistics()
        )
    }

    @Test
    fun `test statistics with corrupted file`() {
        assertThrows<IllegalStateException> {
            val trainer = LearnWordsTrainer(fileNameTxt = "corrupted_file_test.txt", useDataBase = false)
            trainer.getStatistics()
        }
    }

    @Test
    fun `test getNextQuestion() with 5 unlearned words`() {
        val trainer = LearnWordsTrainer(fileNameTxt = "5_words_of_7.txt", useDataBase = false)
        val unlearnedWordsList = trainer.dictionary.filter { it.correctAnswersCount < NUMBER_OF_SUCCESS_TRIES }

        trainer.questionWord = trainer.getNextQuestion()
        val question = trainer.questionWord

        val result = if (question != null) {
            unlearnedWordsList.containsAll(question.variants)
        } else false

        assertTrue(result)
    }

    @Test
    fun `test getNextQuestion() with 1 unlearned word`() {
        val trainer = LearnWordsTrainer(fileNameTxt = "1_words_of_7.txt", useDataBase = false)
        val unlearnedWordsList = trainer.dictionary
            .filter { it.correctAnswersCount < NUMBER_OF_SUCCESS_TRIES }
        println(unlearnedWordsList)

        trainer.questionWord = trainer.getNextQuestion()
        val question = trainer.questionWord
        println(question)

        val result = if (question != null) {
            unlearnedWordsList.all { it in question.variants }
        } else false

        assertTrue(result)
    }

    @Test
    fun `test getNextQuestion() with all words learned`() {
        val trainer = LearnWordsTrainer(fileNameTxt = "0_words_of_7.txt", useDataBase = false)
        val result = trainer.getNextQuestion()
        assertNull(result)
    }

    @Test
    fun `test checkAnswer() with true`() {
        val tempFile = File.createTempFile("4_words_of_7_will be deleted after test", ".txt")
            .apply { deleteOnExit() }

        File("4_words_of_7.txt").copyTo(tempFile, overwrite = true)

        val trainer = LearnWordsTrainer(fileNameTxt = tempFile.absolutePath, useDataBase = false)

        trainer.questionWord = trainer.getNextQuestion()
        val wordsToGuess = trainer.questionWord
        val correctAnswerWordId = wordsToGuess?.variants?.indexOf(wordsToGuess.correctAnswer)

        val result = trainer.checkAnswer(correctAnswerWordId)
        assertEquals(true, result)
    }

    @Test
    fun `test checkAnswer() with false`() {
        val trainer = LearnWordsTrainer(fileNameTxt = "4_words_of_7.txt", useDataBase = false)

        trainer.questionWord = trainer.getNextQuestion()

        val wordsToGuess = trainer.questionWord
        val correctAnswerWordId = wordsToGuess?.variants?.indexOf(wordsToGuess.correctAnswer)
        val inCorrectAnswerWordId = (0 until QUANTITY_OF_VARIANTS)
            .filter { it != correctAnswerWordId }
            .random()

        val result = trainer.checkAnswer(inCorrectAnswerWordId)
        assertEquals(false, result)
    }

    @Test
    fun `test resetStatistics() with 2 words in dictionary`() {
        val trainer = LearnWordsTrainer(fileNameTxt = "2_words_in_dictionary.txt", useDataBase = false)
        val result = trainer.resetStatistics()
        assertEquals("Ваш прогресс сброшен", result)
    }

}