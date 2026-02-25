package com.example.tagenglish.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tagenglish.domain.model.Question
import com.example.tagenglish.domain.model.TestResult
import com.example.tagenglish.domain.usecases.GenerateTestQuestionsUseCase
import com.example.tagenglish.domain.usecases.SaveTestResultUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TestViewModel(
    private val generateTestQuestionsUseCase: GenerateTestQuestionsUseCase,
    private val saveTestResultUseCase: SaveTestResultUseCase
) : ViewModel() {

    // ─── Estado de la UI ──────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(TestUiState())
    val uiState: StateFlow<TestUiState> = _uiState.asStateFlow()

    // ─── Cargar preguntas ─────────────────────────────────────────────────────

    fun loadQuestions(weekId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, weekId = weekId) }

            val questions = generateTestQuestionsUseCase(weekId)

            _uiState.update {
                it.copy(
                    isLoading       = false,
                    questions       = questions,
                    currentIndex    = 0,
                    answers         = emptyMap(),
                    isFinished      = false,
                    result          = null
                )
            }
        }
    }

    // ─── Responder pregunta ───────────────────────────────────────────────────

    fun answerQuestion(questionIndex: Int, answer: String) {
        _uiState.update { state ->
            val updatedAnswers = state.answers + (questionIndex to answer)
            state.copy(answers = updatedAnswers)
        }
    }

    // ─── Siguiente pregunta ───────────────────────────────────────────────────

    fun nextQuestion() {
        _uiState.update { state ->
            val next = state.currentIndex + 1
            if (next >= state.questions.size) {
                state.copy(allAnswered = true)
            } else {
                state.copy(currentIndex = next)
            }
        }
    }

    // ─── Finalizar test ───────────────────────────────────────────────────────

    fun submitTest() {
        viewModelScope.launch {
            val state = _uiState.value

            // Calcular puntaje
            val score = state.questions.mapIndexed { index, question ->
                val userAnswer = state.answers[index] ?: ""
                val correct    = getCorrectAnswer(question)
                if (userAnswer.equals(correct, ignoreCase = true)) 1 else 0
            }.sum()

            val result = saveTestResultUseCase(
                weekId         = state.weekId,
                score          = score,
                totalQuestions = state.questions.size
            )

            _uiState.update {
                it.copy(
                    isFinished = true,
                    result     = result
                )
            }
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun getCorrectAnswer(question: Question): String = when (question) {
        is Question.MultipleChoiceMeaning -> question.correctMeaning
        is Question.FillInBlank           -> question.correctWord
        is Question.ReverseTranslation    -> question.correctWord
    }

    fun getCurrentQuestion(): Question? {
        val state = _uiState.value
        return state.questions.getOrNull(state.currentIndex)
    }
}

// ─── Estado de la UI ──────────────────────────────────────────────────────────

data class TestUiState(
    val isLoading: Boolean              = false,
    val weekId: Int                     = 0,
    val questions: List<Question>       = emptyList(),
    val currentIndex: Int               = 0,
    val answers: Map<Int, String>       = emptyMap(),
    val allAnswered: Boolean            = false,
    val isFinished: Boolean             = false,
    val result: TestResult?             = null
) {
    val totalQuestions: Int get() = questions.size
    val progress: Float     get() = if (totalQuestions == 0) 0f
                                    else (currentIndex + 1f) / totalQuestions
}
