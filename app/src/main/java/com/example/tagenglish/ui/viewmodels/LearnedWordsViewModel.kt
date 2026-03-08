package com.example.tagenglish.ui.viewmodels

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tagenglish.data.repository.WordRepository
import com.example.tagenglish.domain.model.Word
import com.example.tagenglish.domain.usecases.UnmarkWordAsLearnedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class LearnedWordsViewModel(
    private val repository: WordRepository,
    private val unmarkWordAsLearnedUseCase: UnmarkWordAsLearnedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearnedWordsUiState())
    val uiState: StateFlow<LearnedWordsUiState> = _uiState.asStateFlow()

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        observeLearnedWords()
    }

    fun initTts(context: Context) {
        if (tts != null) return
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                ttsReady = true
            }
        }
    }

    private fun observeLearnedWords() {
        viewModelScope.launch {
            repository.getAllLearnedWords().collectLatest { words ->
                val grouped = words
                    .groupBy { it.weekLearned }
                    .toSortedMap(reverseOrder())
                _uiState.update { it.copy(isLoading = false, groupedWords = grouped) }
            }
        }
    }

    fun playPronunciation(context: Context, wordId: Int, wordText: String) {
        if (_uiState.value.loadingAudioWordId == wordId) return

        _uiState.update { it.copy(loadingAudioWordId = wordId) }

        viewModelScope.launch {
            try {
                val audioUrl = repository.fetchAndSavePhonetic(wordId, wordText)

                if (!audioUrl.isNullOrBlank()) {
                    playMp3(audioUrl)
                } else {
                    initTts(context)
                    if (ttsReady) tts?.speak(wordText, TextToSpeech.QUEUE_FLUSH, null, "word_$wordId")
                }
            } catch (e: Exception) {
                initTts(context)
                if (ttsReady) tts?.speak(wordText, TextToSpeech.QUEUE_FLUSH, null, "word_$wordId")
            } finally {
                _uiState.update { it.copy(loadingAudioWordId = null) }
            }
        }
    }

    private fun playMp3(url: String) {
        try {
            MediaPlayer().apply {
                setDataSource(url)
                setOnPreparedListener { start() }
                setOnCompletionListener { release() }
                setOnErrorListener { mp, _, _ -> mp.release(); true }
                prepareAsync()
            }
        } catch (e: Exception) { }
    }

    fun unmarkAsLearned(wordId: Int) {
        viewModelScope.launch {
            unmarkWordAsLearnedUseCase(wordId)
            _uiState.update { it.copy(snackbarMessage = "Palabra marcada como pendiente") }
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
    }
}

data class LearnedWordsUiState(
    val isLoading: Boolean = true,
    val groupedWords: Map<Int, List<Word>> = emptyMap(),
    val snackbarMessage: String? = null,
    val loadingAudioWordId: Int? = null
) {
    val totalLearned: Int get() = groupedWords.values.sumOf { it.size }
}