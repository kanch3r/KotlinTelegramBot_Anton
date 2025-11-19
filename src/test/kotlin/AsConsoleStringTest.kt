import learningbot.console.asConsoleString
import learningbot.trainer.model.Question
import learningbot.trainer.model.Word
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AsConsoleStringTest {

    @Test
    fun `base functional`() {
        val question = Question(
            variants = listOf(
                Word("cat", "кошка"),
                Word("dog", "собака"),
                Word("hello", "привет"),
                Word("thank you", "спасибо"),
            ),
            correctAnswer = Word("dog", "собака")
        )

        val result = question.asConsoleString()

        assertTrue(result.startsWith("dog:"))
        assertTrue(result.contains("кошка"))
        assertTrue(result.contains("собака"))
        assertTrue(result.contains("привет"))
        assertTrue(result.contains("спасибо"))
        assertTrue(result.contains("__________"))
        assertTrue(result.endsWith("0 - Меню"))
    }

    @Test
    fun `should work with any variants words order`() {
        val question = Question(
            variants = listOf(
                Word("thank you", "спасибо"),
                Word("dog", "собака"),
                Word("cat", "кошка"),
                Word("hello", "привет"),
            ),
            correctAnswer = Word("dog", "собака")
        )

        val result = question.asConsoleString()

        assertTrue(result.contains("кошка"))
        assertTrue(result.contains("собака"))
        assertTrue(result.contains("привет"))
        assertTrue(result.contains("спасибо"))
    }

    @Test
    fun `variants should start from 1`() {
        val question = Question(
            variants = listOf(Word("cat", "кошка")),
            correctAnswer = Word("dog", "собака")
        )

        val result = question.asConsoleString()

        assertTrue(result.contains("1 - кошка"))
    }

    @Test
    fun `should handle with 10 words`() {
        val question = Question(
            variants = List(10) { Word("origin", "translate") },
            correctAnswer = Word("dog", "собака")
        )

        val result = question.asConsoleString()

        assertEquals(question.variants.size + 3, result.lines().size)
    }

    @Test
    fun `should handle with empty variants`() {
        val question = Question(
            variants = emptyList(),
            correctAnswer = Word("dog", "собака")
        )

        val result = question.asConsoleString()

        assertTrue(result.startsWith("dog:"))
        assertTrue(result.contains("__________"))
        assertTrue(result.endsWith("0 - Меню"))
    }

    @Test
    fun `should work with words with special symbols`() {
        val question = Question(
            variants = listOf(
                Word("cat-!", "-кошка@#"),
                Word("hello...", "привет..."),
            ),
            correctAnswer = Word("*()$@", "собака")
        )

        val result = question.asConsoleString()

        assertTrue(result.startsWith("*()$@"))
        assertTrue(result.contains("-кошка@#"))
        assertTrue(result.contains("привет..."))
    }

}