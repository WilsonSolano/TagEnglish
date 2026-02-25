package com.example.tagenglish.domain.usecases

import com.example.tagenglish.data.preferences.AppPreferences
import com.example.tagenglish.data.repository.WordRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Verifica si es momento de mostrar el test semanal.
 *
 * Condiciones para mostrar el test:
 * - Han pasado 7 días desde la última generación de palabras.
 * - Existen palabras aprendidas en la semana actual.
 * - El test de esta semana aún no fue completado.
 */
class CheckWeeklyTestUseCase(
    private val repository: WordRepository,
    private val preferences: AppPreferences
) {
    sealed class Result {
        /** No es momento del test todavía */
        object NotYet : Result()

        /** No hay palabras aprendidas esta semana para evaluar */
        object NoWordsToTest : Result()

        /** El test de esta semana ya fue completado */
        object AlreadyCompleted : Result()

        /** Es momento del test — devuelve la semana a evaluar */
        data class TestReady(val weekId: Int) : Result()
    }

    suspend operator fun invoke(): Result {
        var lastDate    = ""
        var currentWeek = 1

        preferences.lastGeneratedDate.collect { lastDate    = it }
        preferences.currentWeek.collect       { currentWeek = it }

        // 1. ¿Han pasado 7 días?
        if (lastDate.isNotBlank()) {
            val start    = LocalDate.parse(lastDate)
            val today    = LocalDate.now()
            val daysPast = ChronoUnit.DAYS.between(start, today)
            if (daysPast < 7) return Result.NotYet
        } else {
            return Result.NotYet
        }

        // 2. ¿Ya se completó el test esta semana?
        if (repository.hasTestResultForWeek(currentWeek)) {
            return Result.AlreadyCompleted
        }

        // 3. ¿Hay palabras aprendidas para evaluar?
        val learnedWords = repository.getLearnedWordsByWeek(currentWeek)
        if (learnedWords.isEmpty()) return Result.NoWordsToTest

        return Result.TestReady(currentWeek)
    }
}
