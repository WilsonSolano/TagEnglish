package com.example.tagenglish.domain.usecases

import com.example.tagenglish.data.preferences.AppPreferences
import com.example.tagenglish.data.repository.WordRepository

/**
 * Marca una palabra como aprendida.
 * Asocia automáticamente la semana actual y la fecha actual.
 */
class MarkWordAsLearnedUseCase(
    private val repository: WordRepository,
    private val preferences: AppPreferences
) {
    suspend operator fun invoke(wordId: Int) {
        var currentWeek = 1
        preferences.currentWeek.collect { currentWeek = it }

        repository.markWordAsLearned(
            wordId = wordId,
            date   = System.currentTimeMillis(),
            week   = currentWeek
        )
    }
}
