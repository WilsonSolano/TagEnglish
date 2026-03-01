package com.example.tagenglish.domain.usecases

import com.example.tagenglish.data.preferences.AppPreferences
import com.example.tagenglish.data.repository.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CheckWeeklyTestUseCase(
    private val repository: WordRepository,
    private val preferences: AppPreferences
) {
    sealed class Result {
        object NotYet : Result()
        object NoWordsToTest : Result()
        object AlreadyCompleted : Result()
        data class TestReady(val weekId: Int) : Result()
    }

    suspend operator fun invoke(): Result = withContext(Dispatchers.IO) {
        // ✅ .first() en lugar de .collect {}
        val lastDate    = preferences.lastGeneratedDate.first()
        val currentWeek = preferences.currentWeek.first()

        if (lastDate.isNotBlank()) {
            val start    = LocalDate.parse(lastDate)
            val today    = LocalDate.now()
            val daysPast = ChronoUnit.DAYS.between(start, today)
            if (daysPast < 7) return@withContext Result.NotYet
        } else {
            return@withContext Result.NotYet
        }

        if (repository.hasTestResultForWeek(currentWeek)) {
            return@withContext Result.AlreadyCompleted
        }

        val learnedWords = repository.getLearnedWordsByWeek(currentWeek)
        if (learnedWords.isEmpty()) return@withContext Result.NoWordsToTest

        return@withContext Result.TestReady(currentWeek)
    }
}