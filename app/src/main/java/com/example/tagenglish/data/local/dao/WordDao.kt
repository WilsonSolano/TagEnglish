package com.example.tagenglish.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tagenglish.data.local.entities.UsageEntity
import com.example.tagenglish.data.local.entities.WordEntity
import com.example.tagenglish.data.local.entities.WordWithUsages
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    // ─── Insert ───────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWord(word: WordEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUsages(usages: List<UsageEntity>)

    // ─── Update ───────────────────────────────────────────────────────────────

    @Update
    suspend fun updateWord(word: WordEntity)

    @Query("UPDATE words SET isAssigned = 1, assignedDate = :date WHERE id = :wordId")
    suspend fun markAsAssigned(wordId: Int, date: Long)

    @Query("""
        UPDATE words 
        SET isLearned = 1, learnedDate = :date, weekLearned = :week 
        WHERE id = :wordId
    """)
    suspend fun markAsLearned(wordId: Int, date: Long, week: Int)

    @Query("""
        UPDATE words 
        SET isLearned = 0, learnedDate = 0, weekLearned = 0 
        WHERE id = :wordId
    """)
    suspend fun unmarkAsLearned(wordId: Int)

    // ← nuevo: guarda la fonética obtenida de la API
    @Query("UPDATE words SET phonetic = :phonetic WHERE id = :wordId")
    suspend fun savePhonetic(wordId: Int, phonetic: String)

    // ─── Queries ──────────────────────────────────────────────────────────────

    @Query("SELECT * FROM words WHERE isAssigned = 0 LIMIT :limit")
    suspend fun getUnassignedWords(limit: Int): List<WordEntity>

    @Transaction
    @Query("SELECT * FROM words WHERE id IN (:ids)")
    fun getTodayWordsWithUsages(ids: List<Int>): Flow<List<WordWithUsages>>

    @Transaction
    @Query("SELECT * FROM words WHERE isLearned = 1 AND weekLearned = :week")
    suspend fun getLearnedWordsByWeek(week: Int): List<WordWithUsages>

    @Transaction
    @Query("SELECT * FROM words WHERE isLearned = 1 ORDER BY learnedDate DESC")
    fun getAllLearnedWords(): Flow<List<WordWithUsages>>

    @Transaction
    @Query("SELECT * FROM words ORDER BY word ASC")
    suspend fun getAllWordsWithUsages(): List<WordWithUsages>

    @Transaction
    @Query("SELECT * FROM words ORDER BY word ASC")
    fun getAllWordsWithUsagesFlow(): Flow<List<WordWithUsages>>

    @Query("SELECT COUNT(*) FROM words WHERE id IN (:ids) AND isLearned = 0")
    suspend fun countPendingWords(ids: List<Int>): Int

    @Query("SELECT COUNT(*) FROM words WHERE isAssigned = 0")
    suspend fun countUnassignedWords(): Int

    @Query("SELECT COUNT(*) FROM words")
    suspend fun countAllWords(): Int

    @Query("SELECT * FROM words")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("UPDATE words SET isAssigned = 0, isLearned = 0, assignedDate = 0, learnedDate = 0, weekLearned = 0")
    suspend fun resetAllWords()
}