package com.example.tagenglish.domain.usecases

import com.example.tagenglish.data.preferences.AppPreferences
import com.example.tagenglish.data.repository.WordRepository
import java.time.LocalDate

/**
 * Decide si hay que asignar palabras nuevas hoy y lo hace si corresponde.
 *
 * Reglas que aplica:
 * 1. Si ya se generaron palabras hoy → no hace nada.
 * 2. Si hay palabras pendientes del día anterior → bloquea (devuelve BLOCKED).
 * 3. Si no quedan palabras sin asignar → devuelve CYCLE_COMPLETED.
 * 4. Si todo está bien → asigna nuevas palabras y devuelve NEW_WORDS_ASSIGNED.
 */
class AssignDailyWordsUseCase(
    private val repository: WordRepository,
    private val preferences: AppPreferences
) {
    sealed class Result {
        /** Ya se asignaron palabras hoy, no hay nada que hacer */
        object AlreadyAssignedToday : Result()

        /** Hay palabras del día anterior sin aprender, no se desbloquean nuevas */
        object Blocked : Result()

        /** Se agotaron todas las palabras del pool */
        object CycleCompleted : Result()

        /** Se asignaron nuevas palabras correctamente */
        data class NewWordsAssigned(val wordIds: List<Int>) : Result()
    }

    suspend operator fun invoke(
        today: String = LocalDate.now().toString()  // "2026-02-24"
    ): Result {
        // 1. Leer estado actual del DataStore
        var lastDate    = ""
        var todayIds    = emptyList<Int>()
        var wordsPerDay = AppPreferences.DEFAULT_WORDS_PER_DAY
        var currentWeek = 1

        // Recolectar valores actuales (una sola emisión)
        preferences.lastGeneratedDate.collect { lastDate    = it }
        preferences.todayWordIds.collect      { todayIds    = it }
        preferences.wordsPerDay.collect       { wordsPerDay = it }
        preferences.currentWeek.collect       { currentWeek = it }

        // 2. ¿Ya se generaron palabras hoy?
        if (lastDate == today) return Result.AlreadyAssignedToday

        // 3. ¿Quedan palabras pendientes de ayer?
        if (todayIds.isNotEmpty() && repository.hasPendingWords(todayIds)) {
            return Result.Blocked
        }

        // 4. ¿Quedan palabras disponibles en el pool?
        if (repository.countUnassignedWords() == 0) {
            return Result.CycleCompleted
        }

        // 5. Asignar nuevas palabras
        val now     = System.currentTimeMillis()
        val newIds  = repository.assignNewWords(wordsPerDay, now, currentWeek)

        // 6. Guardar en DataStore
        preferences.setTodayWordIds(newIds)
        preferences.setLastGeneratedDate(today)

        return Result.NewWordsAssigned(newIds)
    }
}
