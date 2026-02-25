package com.example.tagenglish.data.local.database

import android.content.Context
import com.example.tagenglish.data.local.entities.UsageEntity
import com.example.tagenglish.data.local.entities.WordEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object DatabaseSeeder {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun seed(context: Context, db: AppDatabase) {
        val wordDao = db.wordDao()

        // Leer JSON desde assets
        val jsonString = context.assets
            .open("words.json")
            .bufferedReader()
            .use { it.readText() }

        val wordJsonList = json.decodeFromString<List<WordJson>>(jsonString)

        wordJsonList.forEach { wordJson ->
            // Insertar palabra
            val wordId = wordDao.insertWord(
                WordEntity(word = wordJson.word)
            ).toInt()

            // Insertar sus usos
            if (wordId > 0) {
                val usages = wordJson.usages.map { usage ->
                    UsageEntity(
                        wordId = wordId,
                        meaning = usage.meaning,
                        example = usage.example
                    )
                }
                wordDao.insertUsages(usages)
            }
        }
    }

    // ─── Modelos internos para deserializar JSON ───────────────────────────────

    @Serializable
    private data class WordJson(
        val word: String,
        val usages: List<UsageJson>
    )

    @Serializable
    private data class UsageJson(
        val meaning: String,
        val example: String
    )
}
