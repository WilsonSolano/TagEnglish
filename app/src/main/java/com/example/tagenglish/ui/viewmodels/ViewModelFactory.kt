package com.example.tagenglish.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tagenglish.data.local.database.AppDatabase
import com.example.tagenglish.data.preferences.AppPreferences
import com.example.tagenglish.data.repository.WordRepositoryImpl
import com.example.tagenglish.domain.usecases.AssignDailyWordsUseCase
import com.example.tagenglish.domain.usecases.CheckWeeklyTestUseCase
import com.example.tagenglish.domain.usecases.GenerateTestQuestionsUseCase
import com.example.tagenglish.domain.usecases.GetTodayWordsUseCase
import com.example.tagenglish.domain.usecases.MarkWordAsLearnedUseCase
import com.example.tagenglish.domain.usecases.SaveTestResultUseCase

class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

    // ─── Construir dependencias ───────────────────────────────────────────────

    private val db          = AppDatabase.getInstance(context)
    private val preferences = AppPreferences(context)

    private val repository  = WordRepositoryImpl(
        wordDao        = db.wordDao(),
        testResultDao  = db.testResultDao()
    )

    // ─── UseCases ─────────────────────────────────────────────────────────────

    private val getTodayWordsUseCase       = GetTodayWordsUseCase(repository)
    private val assignDailyWordsUseCase    = AssignDailyWordsUseCase(repository, preferences)
    private val markWordAsLearnedUseCase   = MarkWordAsLearnedUseCase(repository, preferences)
    private val checkWeeklyTestUseCase     = CheckWeeklyTestUseCase(repository, preferences)
    private val generateQuestionsUseCase   = GenerateTestQuestionsUseCase(repository)
    private val saveTestResultUseCase      = SaveTestResultUseCase(repository, preferences)

    // ─── Factory ──────────────────────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(
            getTodayWordsUseCase    = getTodayWordsUseCase,
            assignDailyWordsUseCase = assignDailyWordsUseCase,
            markWordAsLearnedUseCase= markWordAsLearnedUseCase,
            checkWeeklyTestUseCase  = checkWeeklyTestUseCase,
            preferences             = preferences
        ) as T

        modelClass.isAssignableFrom(TestViewModel::class.java) -> TestViewModel(
            generateTestQuestionsUseCase = generateQuestionsUseCase,
            saveTestResultUseCase        = saveTestResultUseCase
        ) as T

        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
