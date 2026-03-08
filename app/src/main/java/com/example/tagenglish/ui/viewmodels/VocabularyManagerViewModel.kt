package com.example.tagenglish.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tagenglish.data.repository.WordRepository
import com.example.tagenglish.domain.model.Usage
import com.example.tagenglish.domain.model.Word
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class VocabularyManagerViewModel(
    private val repository: WordRepository
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private val _uiState = MutableStateFlow(VocabularyManagerUiState())
    val uiState: StateFlow<VocabularyManagerUiState> = _uiState.asStateFlow()

    init {
        observeWords()
    }

    private fun observeWords() {
        viewModelScope.launch {
            repository.getAllWordsFlow().collectLatest { words ->
                _uiState.update {
                    it.copy(
                        isLoading  = false,
                        totalWords = words.size,
                        learnedCount = words.count { w -> w.isLearned },
                        pendingCount = words.count { w -> !w.isLearned }
                    )
                }
            }
        }
    }

    // ── Importar JSON desde URI ───────────────────────────────────────────────

    fun importFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, message = null) }
            try {
                val text = context.contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: throw Exception("No se pudo leer el archivo")

                val parsed = json.decodeFromString<List<WordJson>>(text)

                if (parsed.isEmpty()) {
                    _uiState.update {
                        it.copy(isProcessing = false, message = Message.Error("El archivo está vacío"))
                    }
                    return@launch
                }

                // Validar estructura mínima
                val invalid = parsed.firstOrNull { it.word.isBlank() || it.usages.isEmpty() }
                if (invalid != null) {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            message = Message.Error("Estructura inválida. Revisa que cada palabra tenga 'word' y al menos un 'usages'.")
                        )
                    }
                    return@launch
                }

                val domainWords = parsed.map { w ->
                    Word(
                        id           = 0,
                        word         = w.word.trim().lowercase(),
                        isAssigned   = false,
                        assignedDate = 0L,
                        isLearned    = false,
                        learnedDate  = 0L,
                        weekLearned  = 0,
                        usages       = w.usages.map { u ->
                            Usage(id = 0, wordId = 0, meaning = u.meaning.trim(), example = u.example.trim())
                        }
                    )
                }

                val imported = repository.importWords(domainWords)
                val skipped  = parsed.size - imported

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        message = Message.Success(
                            buildString {
                                append("✅ $imported palabra${if (imported != 1) "s" else ""} importada${if (imported != 1) "s" else ""}")
                                if (skipped > 0) append(" · $skipped duplicada${if (skipped != 1) "s" else ""} omitida${if (skipped != 1) "s" else ""}")
                            }
                        )
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        message = Message.Error("Error al leer el archivo: ${e.message}")
                    )
                }
            }
        }
    }

    // ── Exportar todas las palabras del sistema ───────────────────────────────

    fun buildExportJson(): String {
        // Se llama suspendida desde la UI con LaunchedEffect
        return ""
    }

    suspend fun getExportJson(): String {
        val words = repository.getAllWords()
        val export = words.map { w ->
            WordJson(
                word   = w.word,
                usages = w.usages.map { u ->
                    UsageJson(meaning = u.meaning, example = u.example)
                }
            )
        }
        return json.encodeToString(kotlinx.serialization.builtins.ListSerializer(WordJson.serializer()), export)
    }

    // ── JSON de ejemplo para descargar ────────────────────────────────────────

    fun getSampleJson(): String {
        val sample = listOf(
            WordJson(
                word = "challenge",
                usages = listOf(
                    UsageJson("desafío",  "Learning a language is a great challenge."),
                    UsageJson("retar",    "I challenge you to read one book a week.")
                )
            ),
            WordJson(
                word = "wonder",
                usages = listOf(
                    UsageJson("maravilla", "The Grand Canyon is a natural wonder."),
                    UsageJson("preguntarse", "I wonder what tomorrow will bring.")
                )
            ),
            WordJson(
                word = "sharp",
                usages = listOf(
                    UsageJson("afilado",     "Be careful, the knife is very sharp."),
                    UsageJson("inteligente", "She has a sharp mind for business.")
                )
            )
        )
        return json.encodeToString(
            kotlinx.serialization.builtins.ListSerializer(WordJson.serializer()),
            sample
        )
    }

    fun dismissMessage() {
        _uiState.update { it.copy(message = null) }
    }

    // ── Modelos internos de serialización ────────────────────────────────────

    @Serializable
    data class WordJson(
        val word: String,
        val usages: List<UsageJson>
    )

    @Serializable
    data class UsageJson(
        val meaning: String,
        val example: String
    )
}

// ── Estado de la UI ───────────────────────────────────────────────────────────

data class VocabularyManagerUiState(
    val isLoading: Boolean    = true,
    val isProcessing: Boolean = false,
    val totalWords: Int       = 0,
    val learnedCount: Int     = 0,
    val pendingCount: Int     = 0,
    val message: Message?     = null
)

sealed class Message {
    data class Success(val text: String) : Message()
    data class Error(val text: String)   : Message()
}
