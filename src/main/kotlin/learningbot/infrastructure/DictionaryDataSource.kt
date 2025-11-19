package learningbot.infrastructure

import learningbot.trainer.DICTIONARY_SOURCE_DB
import learningbot.trainer.DICTIONARY_SOURCE_TXT
import java.io.File
import java.sql.DriverManager
import java.sql.SQLException

fun main() {
    updateDictionary(File(DICTIONARY_SOURCE_TXT))
}

fun updateDictionary(wordsFile: File) {
    try {
        DriverManager.getConnection("jdbc:sqlite:$DICTIONARY_SOURCE_DB")
            .use { connection ->
                connection.createStatement()
                    .use { statement ->

                        val sql = """
                            CREATE TABLE IF NOT EXISTS "words" (
                              "id" INTEGER PRIMARY KEY AUTOINCREMENT,
                              "origin" varchar UNIQUE,
                              "translate" varchar
                            );
                        """.trimIndent()
                        statement.executeUpdate(sql)
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
