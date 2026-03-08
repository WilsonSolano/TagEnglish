package com.example.tagenglish.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tagenglish.domain.model.Word
import com.example.tagenglish.domain.usecases.UnmarkWordAsLearnedUseCase
import com.example.tagenglish.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LearnedWordsViewModel(
    private val repository: WordRepository,
    private val unmarkWordAsLearnedUseCase: UnmarkWordAsLearnedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearnedWordsUiState())
    val uiState: StateFlow<LearnedWordsUiState> = _uiState.asStateFlow()

    init {
        observeLearnedWords()
    }

    private fun observeLearnedWords() {
        viewModelScope.launch {
            repository.getAllLearnedWords().collectLatest { words ->
                // Agrupar por semana
                val grouped = words
                    .groupBy { it.weekLearned }
                    .toSortedMap(reverseOrder())
                _uiState.update {
                    it.copy(isLoading = false, groupedWords = grouped)
                }
            }
        }
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
}

data class LearnedWordsUiState(
    val isLoading: Boolean = true,
    val groupedWords: Map<Int, List<Word>> = emptyMap(),
    val snackbarMessage: String? = null
) {
    val totalLearned: Int get() = groupedWords.values.sumOf { it.size }
}
