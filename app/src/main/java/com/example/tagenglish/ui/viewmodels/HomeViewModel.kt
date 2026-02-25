package com.example.tagenglish.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tagenglish.data.preferences.AppPreferences
import com.example.tagenglish.domain.model.DailyProgress
import com.example.tagenglish.domain.model.Word
import com.example.tagenglish.domain.usecases.AssignDailyWordsUseCase
import com.example.tagenglish.domain.usecases.CheckWeeklyTestUseCase
import com.example.tagenglish.domain.usecases.GetTodayWordsUseCase
import com.example.tagenglish.domain.usecases.MarkWordAsLearnedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModel(
    private val getTodayWordsUseCase: GetTodayWordsUseCase,
    private val assignDailyWordsUseCase: AssignDailyWordsUseCase,
    private val markWordAsLearnedUseCase: MarkWordAsLearnedUseCase,
    private val checkWeeklyTestUseCase: CheckWeeklyTestUseCase,
    private val preferences: AppPreferences
) : ViewModel() {

    // ─── Estado de la UI ──────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // ─── Init ─────────────────────────────────────────────────────────────────

    init {
        assignWordsIfNeeded()
        observeTodayWords()
        checkWeeklyTest()
    }

    // ─── Asignar palabras del día ─────────────────────────────────────────────

    private fun assignWordsIfNeeded() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = assignDailyWordsUseCase(LocalDate.now().toString())) {
                is AssignDailyWordsUseCase.Result.NewWordsAssigned -> {
                    _uiState.update { it.copy(message = null) }
                }
                is AssignDailyWordsUseCase.Result.Blocked -> {
                    _uiState.update {
                        it.copy(message = "Termina de aprender las palabras de hoy primero.")
                    }
                }
                is AssignDailyWordsUseCase.Result.CycleCompleted -> {
                    _uiState.update {
                        it.copy(isCycleCompleted = true)
                    }
                }
                is AssignDailyWordsUseCase.Result.AlreadyAssignedToday -> { /* no-op */ }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // ─── Observar palabras del día reactivamente ──────────────────────────────

    private fun observeTodayWords() {
        viewModelScope.launch {
            preferences.todayWordIds.collectLatest { ids ->
                if (ids.isEmpty()) {
                    _uiState.update { it.copy(words = emptyList()) }
                    return@collectLatest
                }

                getTodayWordsUseCase(ids).collectLatest { (words, progress) ->
                    _uiState.update {
                        it.copy(
                            words    = words,
                            progress = progress
                        )
                    }
                }
            }
        }
    }

    // ─── Verificar test semanal ───────────────────────────────────────────────

    private fun checkWeeklyTest() {
        viewModelScope.launch {
            when (val result = checkWeeklyTestUseCase()) {
                is CheckWeeklyTestUseCase.Result.TestReady -> {
                    _uiState.update { it.copy(weeklyTestReady = true, currentWeekId = result.weekId) }
                }
                else -> { /* no-op */ }
            }
        }
    }

    // ─── Acciones del usuario ─────────────────────────────────────────────────

    fun markAsLearned(wordId: Int) {
        viewModelScope.launch {
            markWordAsLearnedUseCase(wordId)
        }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun dismissCycleCompleted() {
        _uiState.update { it.copy(isCycleCompleted = false) }
    }
}

// ─── Estado de la UI ──────────────────────────────────────────────────────────

data class HomeUiState(
    val isLoading: Boolean       = false,
    val words: List<Word>        = emptyList(),
    val progress: DailyProgress  = DailyProgress(0, 0),
    val message: String?         = null,
    val isCycleCompleted: Boolean = false,
    val weeklyTestReady: Boolean  = false,
    val currentWeekId: Int        = 1
)
