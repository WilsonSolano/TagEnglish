package com.example.tagenglish.domain.usecases

import com.example.tagenglish.data.preferences.AppPreferences
import com.example.tagenglish.data.repository.WordRepository
import com.example.tagenglish.domain.model.TestResult

/**
 * Guarda el resultado del test semanal e incrementa la semana actual.
 * Se llama cuando el usuario termina el test.
 */
class SaveTestResultUseCase(
    private val repository: WordRepository,
    private val preferences: AppPreferences
) {
    suspend operator fun invoke(
        weekId: Int,
        score: Int,
        totalQuestions: Int
    ): TestResult {
        val result = TestResult(
            id             = 0,
            weekId         = weekId,
            score          = score,
            totalQuestions = totalQuestions,
            date           = System.currentTimeMillis()
        )

        // Guardar en Room
        repository.saveTestResult(result)

        // Avanzar a la siguiente semana
        preferences.incrementCurrentWeek()

        return result
    }
}
