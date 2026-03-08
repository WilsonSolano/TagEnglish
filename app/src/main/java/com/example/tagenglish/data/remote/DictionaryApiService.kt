package com.example.tagenglish.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cliente simple para la Free Dictionary API.
 * https://api.dictionaryapi.dev/api/v2/entries/en/{word}
 *
 * No usa Retrofit — solo HttpURLConnection para no añadir dependencias.
 */
object DictionaryApiService {

    private val json = Json { ignoreUnknownKeys = true }
    private const val BASE_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/"

    data class DictionaryResult(
        val phonetic: String,       // ej. "/lɪt/"
        val audioUrl: String        // URL del .mp3, puede estar vacía
    )

    /**
     * Busca la fonética y el audio de una palabra en inglés.
     * Devuelve null si la palabra no existe en el diccionario o hay error de red.
     */
    suspend fun lookup(word: String): DictionaryResult? = withContext(Dispatchers.IO) {
        try {
            val url        = URL("$BASE_URL${word.lowercase().trim()}")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod   = "GET"
                connectTimeout  = 5000
                readTimeout     = 5000
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode != 200) return@withContext null

            val body     = connection.inputStream.bufferedReader().use { it.readText() }
            val entries  = json.decodeFromString<List<DictionaryEntry>>(body)
            val entry    = entries.firstOrNull() ?: return@withContext null

            // Fonética: primero el campo top-level, luego busca en la lista
            val phonetic = entry.phonetic
                ?: entry.phonetics.firstOrNull { it.text?.isNotBlank() == true }?.text
                ?: ""

            // Audio: primer .mp3 disponible en la lista de phonetics
            val audioUrl = entry.phonetics
                .firstOrNull { it.audio?.isNotBlank() == true }
                ?.audio ?: ""

            DictionaryResult(phonetic = phonetic, audioUrl = audioUrl)

        } catch (e: Exception) {
            null    // sin internet o palabra no encontrada → null
        }
    }

    // ─── Modelos internos de deserialización ──────────────────────────────────

    @Serializable
    private data class DictionaryEntry(
        val phonetic: String? = null,
        val phonetics: List<Phonetic> = emptyList()
    )

    @Serializable
    private data class Phonetic(
        val text: String? = null,
        val audio: String? = null
    )
}
