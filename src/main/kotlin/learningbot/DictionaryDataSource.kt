package learningbot

import learningbot.trainer.DICTIONARY_SOURCE
import java.io.File
import java.sql.DriverManager
import java.sql.SQLException


fun main() {

    updateDictionary(File(DICTIONARY_SOURCE))

}

fun updateDictionary(wordsFile: File) {
    try {
        DriverManager.getConnection("jdbc:sqlite:data3.db")
            .use { connection ->
                connection.createStatement()
                    .use { statement ->
                        statement.executeUpdate(
                            """
                            CREATE TABLE IF NOT EXISTS "words" (
                              "id" integer PRIMARY KEY,
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
