package com.example.tagenglish.domain.usecases

import com.example.tagenglish.data.preferences.AppPreferences
import com.example.tagenglish.data.repository.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MarkWordAsLearnedUseCase(
    private val repository: WordRepository,
    private val preferences: AppPreferences
) {
    suspend operator fun invoke(wordId: Int) = withContext(Dispatchers.IO) {
        val currentWeek = preferences.currentWeek.first()

        repository.markWordAsLearned(
            wordId = wordId,
            date   = System.currentTimeMillis(),
            week   = currentWeek
        )
    }
}