package com.example.tagenglish.domain.usecases

import com.example.tagenglish.data.preferences.AppPreferences
import com.example.tagenglish.data.repository.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate

class AssignDailyWordsUseCase(
    private val repository: WordRepository,
    private val preferences: AppPreferences
) {
    sealed class Result {
        object AlreadyAssignedToday : Result()
        object Blocked : Result()
        object CycleCompleted : Result()
        data class NewWordsAssigned(val wordIds: List<Int>) : Result()
    }

    suspend operator fun invoke(
        today: String = LocalDate.now().toString()
    ): Result = withContext(Dispatchers.IO) {
        // ✅ .first() en lugar de .collect {}
        val lastDate    = preferences.lastGeneratedDate.first()
        val todayIds    = preferences.todayWordIds.first()
        val wordsPerDay = preferences.wordsPerDay.first()
        val currentWeek = preferences.currentWeek.first()

        if (lastDate == today) return@withContext Result.AlreadyAssignedToday

        if (todayIds.isNotEmpty() && repository.hasPendingWords(todayIds)) {
            return@withContext Result.Blocked
        }

        if (repository.countUnassignedWords() == 0) {
            return@withContext Result.CycleCompleted
        }

        val now    = System.currentTimeMillis()
        val newIds = repository.assignNewWords(wordsPerDay, now, currentWeek)

        preferences.setTodayWordIds(newIds)
        preferences.setLastGeneratedDate(today)

        return@withContext Result.NewWordsAssigned(newIds)
    }
}