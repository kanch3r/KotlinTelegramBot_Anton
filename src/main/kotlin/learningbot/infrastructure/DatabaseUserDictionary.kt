package learningbot.infrastructure

import learningbot.trainer.DICTIONARY_SOURCE_DB
import learningbot.trainer.NUMBER_OF_SUCCESS_TRIES
import learningbot.trainer.ONE_HUNDRED_PERCENT
import learningbot.trainer.model.Statistics
import learningbot.trainer.model.Word
import java.sql.DriverManager
import kotlin.use

class DatabaseUserDictionary(val chatId: Long) : IUserDictionary {

    init {
        createTablesIfNotExists()
    }

    override fun loadDictionary(): List<Word> {
        val dictionary = mutableListOf<Word>()
        DriverManager.getConnection("jdbc:sqlite:$DICTIONARY_SOURCE_DB")
            .use { connection ->
                val sql = """
                    SELECT words.origin, words.translate, user_answers.correct_answer_count
                    FROM words
                    LEFT JOIN (
                    	SELECT word_id, correct_answer_count
                    	FROM user_answers
                    	JOIN users ON users.id = user_answers.user_id
                    	WHERE chat_id = ?
                    	) user_answers ON user_answers.word_id = words.id
                """.trimIndent()

                connection.prepareStatement(sql).use { preparedStatement ->
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

    override fun setCorrectAnswersCount(word: Word) {
        DriverManager.getConnection("jdbc:sqlite:$DICTIONARY_SOURCE_DB")
            .use { connection ->
                val sql1 = """
                    INSERT OR IGNORE INTO users (chat_id)
                    VALUES (?)
                """.trimIndent()

                connection.prepareStatement(sql1).use { statement ->
                    statement.setLong(1, chatId)
                    statement.executeUpdate()
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
                    statement.setString(2, word.origin)
                    statement.setInt(3, word.correctAnswersCount)
                    statement.executeUpdate()
                }
            }
    }

    override fun getStatistics(): Statistics {
        DriverManager.getConnection("jdbc:sqlite:$DICTIONARY_SOURCE_DB")
            .use { connection ->
                val sgl = """
                    SELECT 
                        (SELECT COUNT(*) FROM words) as total_words,
                        (SELECT COUNT(*)
                            FROM user_answers
                            JOIN users ON user_answers.user_id = users.id 
                            WHERE users.chat_id = ? 
                            AND user_answers.correct_answer_count >= ?
                        ) as learned_words
                """.trimIndent()

                connection.prepareStatement(sgl).use { statement ->
                    statement.setLong(1, chatId)
                    statement.setInt(2, NUMBER_OF_SUCCESS_TRIES)
                    val result = statement.executeQuery()

                    if (result.next()) {
                        val totalCount = result.getInt("total_words")
                        val learnedCount: Int = result.getInt("learned_words")
                        val percent: Double = if (totalCount == 0) {
                            0.0
                        } else {
                            learnedCount * ONE_HUNDRED_PERCENT / totalCount
                        }
                        return Statistics(totalCount, learnedCount, percent)
                    }
                }
            }
        return Statistics(0, 0, 0.0)
    }

    override fun resetProgress(): String {
        DriverManager.getConnection("jdbc:sqlite:$DICTIONARY_SOURCE_DB")
            .use { connection ->
                val sgl = """
                    UPDATE user_answers
                    SET correct_answer_count = 0
                    WHERE user_id in (
                    	SELECT id 
                    	FROM users
                    	WHERE chat_id = ?
                    )
                """.trimIndent()

                connection.prepareStatement(sgl).use { statement ->
                    statement.setLong(1, chatId)
                    statement.executeUpdate()
                }
            }
        return "Ваш прогресс сброшен"
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
                                "correct_answer_count" INTEGER INTEGER DEFAULT 0,
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
}