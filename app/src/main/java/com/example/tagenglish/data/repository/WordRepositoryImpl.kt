package com.example.tagenglish.data.repository

import com.example.tagenglish.data.local.dao.TestResultDao
import com.example.tagenglish.data.local.dao.WordDao
import com.example.tagenglish.data.local.entities.toDomain  // extensión suelta de Mappers.kt
import com.example.tagenglish.data.local.entities.toEntity  // extensión suelta de Mappers.kt
import com.example.tagenglish.domain.model.TestResult
import com.example.tagenglish.domain.model.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WordRepositoryImpl(
    private val wordDao: WordDao,
    private val testResultDao: TestResultDao
) : WordRepository {

    override fun getTodayWords(ids: List<Int>): Flow<List<Word>> =
        wordDao.getTodayWordsWithUsages(ids).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun assignNewWords(count: Int, date: Long, week: Int): List<Int> {
        val words = wordDao.getUnassignedWords(count)
        words.forEach { wordDao.markAsAssigned(it.id, date) }
        return words.map { it.id }
    }

    override suspend fun countUnassignedWords(): Int =
        wordDao.countUnassignedWords()

    override suspend fun markWordAsLearned(wordId: Int, date: Long, week: Int) =
        wordDao.markAsLearned(wordId, date, week)

    override suspend fun hasPendingWords(ids: List<Int>): Boolean =
        wordDao.countPendingWords(ids) > 0

    override suspend fun getLearnedWordsByWeek(week: Int): List<Word> =
        wordDao.getLearnedWordsByWeek(week).map { it.toDomain() }

    override suspend fun saveTestResult(result: TestResult) =
        testResultDao.insertResult(result.toEntity())

    override suspend fun getTestResultByWeek(week: Int): TestResult? =
        testResultDao.getResultByWeek(week)?.toDomain()

    override suspend fun hasTestResultForWeek(week: Int): Boolean =
        testResultDao.hasResultForWeek(week) > 0

    override fun getAllTestResults(): Flow<List<TestResult>> =
        testResultDao.getAllResults().map { list -> list.map { it.toDomain() } }

    override suspend fun resetAllWords() =
        wordDao.resetAllWords()
}
