package learningbot

import learningbot.trainer.DICTIONARY_SOURCE_DB
import learningbot.trainer.model.Word
import java.io.File
import java.sql.DriverManager
import kotlin.use

class DatabaseUserDictionary(val chatId: Long) {

    init {
        DriverManager.getConnection("jdbc:sqlite:wordsDataBase.db")
            .use { connection ->
                connection.createStatement()
                    .use { statement ->
                        statement.executeUpdate(
                            """
                        CREATE TABLE IF NOT EXISTS "users" (
                            "id" INTEGER PRIMARY KEY AUTOINCREMENT,
                            "user_name" VARCHAR,
                            "created_at" TIMESTAMP,
                            "chat_id" INTEGER
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
                        )
                    }
            }
    }

    val dictionary: List<Word> = loadDictionary()

    fun loadDictionary(): List<Word> {
        val dictionary = mutableListOf<Word>()
        DriverManager.getConnection("jdbc:sqlite:$DICTIONARY_SOURCE_DB")
            .use { connection ->
                connection.createStatement()
                    .use { statement ->
                        val sql = """
                        SELECT words.origin, words.translate
                        FROM words
                    """.trimIndent()
                        val result = statement.executeQuery(sql)
                        while (result.next()) {
                            val word = Word(
                                origin = result.getString("origin"),
                                translate = result.getString("translate")
                            )
                            dictionary.add(word)
                        }
                    }
            }
        return dictionary
    }

    fun setCorrectCount() {
        DriverManager.getConnection("jdbc:sqlite:$DICTIONARY_SOURCE_DB")
            .use { connection ->
                connection.createStatement()
                    .use { statement ->
                        val sql = """
                            
                        """.trimIndent()

                    }
            }
    }

}