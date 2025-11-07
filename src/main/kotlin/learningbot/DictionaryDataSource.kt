package learningbot

import learningbot.trainer.DICTIONARY_SOURCE_DB
import learningbot.trainer.model.Word
import java.io.File
import java.sql.DriverManager
import java.sql.SQLException


fun main() {

    val dictionary = loadDictionary()

}

fun loadDictionary(): List<Word> {
    val dictionary = mutableListOf<Word>()
    DriverManager.getConnection("jdbc:sqlite:$DICTIONARY_SOURCE_DB")
        .use { connection ->
            connection.createStatement()
                .use { statement ->
                    val result = statement.executeQuery(
                        """
                        SELECT words.origin, words.translate
                        FROM words
                    """.trimIndent()
                    )
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


fun updateDictionary(wordsFile: File) {
    try {
        DriverManager.getConnection("jdbc:sqlite:wordsDataBase.db")
            .use { connection ->
                connection.createStatement()
                    .use { statement ->
                        statement.executeUpdate(
                            """
                            CREATE TABLE IF NOT EXISTS "words" (
                              "id" INTEGER PRIMARY KEY AUTOINCREMENT,
                              "origin" varchar UNIQUE,
                              "translate" varchar
                            );
                        """.trimIndent()
                        )
                    }
                val insertSQL = """
                            INSERT OR REPLACE INTO words ("origin", "translate") VALUES (?, ?)
                        """.trimIndent()
                connection.prepareStatement(insertSQL)
                    .use { ps ->
                        wordsFile.forEachLine { row ->
                            val line = row.split("|")
                            ps.setString(1, line[0])
                            ps.setString(2, line[1])
                            ps.executeUpdate()
                        }
                    }
            }
    } catch (e: SQLException) {
        e.printStackTrace(System.err)
    }
}
