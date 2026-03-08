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
import com.example.tagenglish.domain.usecases.UnmarkWordAsLearnedUseCase

class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val db          = AppDatabase.getInstance(context)
    private val preferences = AppPreferences(context)

    private val repository  = WordRepositoryImpl(
        wordDao       = db.wordDao(),
        testResultDao = db.testResultDao()
    )

    private val getTodayWordsUseCase       = GetTodayWordsUseCase(repository)
    private val assignDailyWordsUseCase    = AssignDailyWordsUseCase(repository, preferences)
    private val markWordAsLearnedUseCase   = MarkWordAsLearnedUseCase(repository, preferences)
    private val unmarkWordAsLearnedUseCase = UnmarkWordAsLearnedUseCase(repository)
    private val checkWeeklyTestUseCase     = CheckWeeklyTestUseCase(repository, preferences)
    private val generateQuestionsUseCase   = GenerateTestQuestionsUseCase(repository)
    private val saveTestResultUseCase      = SaveTestResultUseCase(repository, preferences)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {

        modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(
            getTodayWordsUseCase     = getTodayWordsUseCase,
            assignDailyWordsUseCase  = assignDailyWordsUseCase,
            markWordAsLearnedUseCase = markWordAsLearnedUseCase,
            checkWeeklyTestUseCase   = checkWeeklyTestUseCase,
            preferences              = preferences
        ) as T

        modelClass.isAssignableFrom(TestViewModel::class.java) -> TestViewModel(
            generateTestQuestionsUseCase = generateQuestionsUseCase,
            saveTestResultUseCase        = saveTestResultUseCase
        ) as T

        modelClass.isAssignableFrom(LearnedWordsViewModel::class.java) -> LearnedWordsViewModel(
            repository                 = repository,
            unmarkWordAsLearnedUseCase = unmarkWordAsLearnedUseCase
        ) as T

        modelClass.isAssignableFrom(VocabularyManagerViewModel::class.java) -> VocabularyManagerViewModel(
            repository = repository
        ) as T

        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}