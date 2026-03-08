package com.example.tagenglish.data.repository

import com.example.tagenglish.domain.model.TestResult
import com.example.tagenglish.domain.model.Word
import kotlinx.coroutines.flow.Flow

interface WordRepository {

    fun getTodayWords(ids: List<Int>): Flow<List<Word>>

    suspend fun assignNewWords(count: Int, date: Long, week: Int): List<Int>

    suspend fun countUnassignedWords(): Int

    suspend fun markWordAsLearned(wordId: Int, date: Long, week: Int)

    suspend fun unmarkWordAsLearned(wordId: Int)

    suspend fun hasPendingWords(ids: List<Int>): Boolean

    suspend fun getLearnedWordsByWeek(week: Int): List<Word>

    fun getAllLearnedWords(): Flow<List<Word>>

    /** Todas las palabras del sistema (para exportar / pantalla de vocab) */
    suspend fun getAllWords(): List<Word>

    /** Todas las palabras reactivo */
    fun getAllWordsFlow(): Flow<List<Word>>

    /** Importar palabras nuevas desde JSON (ignora duplicados) */
    suspend fun importWords(words: List<Word>): Int

    suspend fun saveTestResult(result: TestResult)

    suspend fun getTestResultByWeek(week: Int): TestResult?

    suspend fun hasTestResultForWeek(week: Int): Boolean

    fun getAllTestResults(): Flow<List<TestResult>>

    suspend fun resetAllWords()
}