package com.example.tagenglish.domain.model

// ─── Word ─────────────────────────────────────────────────────────────────────

data class Word(
    val id: Int,
    val word: String,
    val isAssigned: Boolean,
    val assignedDate: Long,
    val isLearned: Boolean,
    val learnedDate: Long,
    val weekLearned: Int,
    val usages: List<Usage>
)

// ─── Usage ────────────────────────────────────────────────────────────────────

data class Usage(
    val id: Int,
    val wordId: Int,
    val meaning: String,
    val example: String
)

// ─── TestResult ───────────────────────────────────────────────────────────────

data class TestResult(
    val id: Int,
    val weekId: Int,
    val score: Int,
    val totalQuestions: Int,
    val date: Long
) {
    val percentage: Float
        get() = if (totalQuestions == 0) 0f else score.toFloat() / totalQuestions * 100f

    val passed: Boolean
        get() = percentage >= 60f
}

// ─── DailyProgress ────────────────────────────────────────────────────────────

data class DailyProgress(
    val totalWords: Int,
    val learnedWords: Int,
    val isComplete: Boolean = learnedWords >= totalWords
) {
    val summary: String
        get() = "$learnedWords/$totalWords aprendidas"
}

// ─── Question (generada dinámicamente para el test) ───────────────────────────

sealed class Question {

    data class MultipleChoiceMeaning(
        val wordId: Int,
        val word: String,
        val correctMeaning: String,
        val options: List<String>
    ) : Question()

    data class FillInBlank(
        val wordId: Int,
        val example: String,        // la palabra reemplazada por "___"
        val correctWord: String
    ) : Question()

    data class ReverseTranslation(
        val wordId: Int,
        val meaning: String,
        val correctWord: String,
        val options: List<String>
    ) : Question()
}