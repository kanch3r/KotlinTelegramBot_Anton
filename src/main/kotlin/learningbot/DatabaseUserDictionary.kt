package learningbot

import learningbot.trainer.DICTIONARY_SOURCE_DB
import learningbot.trainer.model.Question
import learningbot.trainer.model.Word
import java.sql.DriverManager
import kotlin.use

class DatabaseUserDictionary(val chatId: Long) {

    init {
        createTablesIfNotExists()
    }

    private fun createTablesIfNotExists() {
        DriverManager.getConnection("jdbc:sqlite:$DICTIONARY_SOURCE_DB")
            .use { connection ->
                connection.createStatement()
                    .use { statement ->

                        val sql = """
                        CREATE TABLE IF NOT EXISTS "users" (
                            "id" INTEGER PRIMARY KEY AUTOINCREMENT,
                            "user_name" VARCHAR,
                            "created_at" TIMESTAMP,
                            "chat_id" INTEGER UNIQUE
                        );
                        CREATE TABLE IF NOT EXISTS "user_answers" (
                            "user_id" INTEGER,
                            "word_id" INTEGER,
                            "correct_answer_count" INTEGER,
                            "updated_at" TIMESTAMP,
                            PRIMARY KEY ("user_id", "word_id"),
                            FOREIGN KEY ("user_id") REFERENCES "users" ("id"),
                            FOREIGN KEY ("word_id") REFERENCES "words" ("id")
                            )
                    """.trimIndent()
                        statement.executeUpdate(sql)
                    }
            }
    }


    fun loadDictionary(): List<Word> {
        val dictionary = mutableListOf<Word>()
        DriverManager.getConnection("jdbc:sqlite:$DICTIONARY_SOURCE_DB")
            .use { connection ->
                val sql = """
                    SELECT words.origin, words.translate, user_answers.correct_answer_count
                    FROM words
                    LEFT JOIN user_answers ON words.id = user_answers.word_id
                    LEFT JOIN users ON users.id = user_answers.user_id AND users.chat_id = ?
                    """.trimIndent()
                connection.prepareStatement(sql)
                    .use { preparedStatement ->
                        preparedStatement.setLong(1, chatId)
                        val result = preparedStatement.executeQuery()
                        while (result.next()) {
                            val word = Word(
                                origin = result.getString("origin"),
                                translate = result.getString("translate"),
                                correctAnswersCount = result.getInt("correct_answer_count")
                            )
                            dictionary.add(word)
                        }
                    }
            }
        return dictionary
    }

    fun setCorrectCount(questionWord: Question) {
        DriverManager.getConnection("jdbc:sqlite:$DICTIONARY_SOURCE_DB")
            .use { connection ->
                val sql1 = """
                INSERT OR IGNORE INTO users.chat_id
                VALUES ?
            """.trimIndent()

                connection.prepareStatement(sql1).use { statement ->
                    statement.setLong(1, chatId)
                    val result1 = statement.executeUpdate()
                }

                val sql2 = """
                INSERT OR REPLACE INTO user_answers (user_id, word_id, correct_answer_count)
                VALUES (
                    (SELECT id FROM users WHERE chat_id = ?),
                    (SELECT id FROM words WHERE origin = ?),
                    ?
                )
            """.trimIndent()

                connection.prepareStatement(sql2).use { statement ->
                    statement.setLong(1, chatId)
                    statement.setString(2, questionWord.correctAnswer.origin)
                    statement.setInt(3, questionWord.correctAnswer.correctAnswersCount)
                    val result2 = statement.executeUpdate()
                }
            }
    }
}