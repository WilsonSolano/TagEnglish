package com.example.tagenglish.data.repository

import com.example.tagenglish.domain.model.TestResult
import com.example.tagenglish.domain.model.Word
import kotlinx.coroutines.flow.Flow

interface WordRepository {

    // ─── Palabras del día ──────────────────────────────────────────────────────

    /** Devuelve las palabras de los IDs dados como Flow reactivo */
    fun getTodayWords(ids: List<Int>): Flow<List<Word>>

    /** Selecciona [count] palabras no asignadas, las marca y devuelve sus IDs */
    suspend fun assignNewWords(count: Int, date: Long, week: Int): List<Int>

    /** Cuántas palabras sin asignar quedan en el pool */
    suspend fun countUnassignedWords(): Int

    // ─── Aprendizaje ──────────────────────────────────────────────────────────

    suspend fun markWordAsLearned(wordId: Int, date: Long, week: Int)

    /** ¿Quedan palabras sin aprender de la lista de hoy? */
    suspend fun hasPendingWords(ids: List<Int>): Boolean

    // ─── Test semanal ─────────────────────────────────────────────────────────

    suspend fun getLearnedWordsByWeek(week: Int): List<Word>

    suspend fun saveTestResult(result: TestResult)

    suspend fun getTestResultByWeek(week: Int): TestResult?

    suspend fun hasTestResultForWeek(week: Int): Boolean

    fun getAllTestResults(): Flow<List<TestResult>>

    // ─── Ciclo completado ─────────────────────────────────────────────────────

    suspend fun resetAllWords()
}
