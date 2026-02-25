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

    /** Marca una palabra como asignada */
    @Query("UPDATE words SET isAssigned = 1, assignedDate = :date WHERE id = :wordId")
    suspend fun markAsAssigned(wordId: Int, date: Long)

    /** Marca una palabra como aprendida */
    @Query("""
        UPDATE words 
        SET isLearned = 1, learnedDate = :date, weekLearned = :week 
        WHERE id = :wordId
    """)
    suspend fun markAsLearned(wordId: Int, date: Long, week: Int)

    // ─── Query palabras disponibles ───────────────────────────────────────────

    /** Palabras que aún no han sido asignadas */
    @Query("SELECT * FROM words WHERE isAssigned = 0 LIMIT :limit")
    suspend fun getUnassignedWords(limit: Int): List<WordEntity>

    /** Palabras asignadas hoy (por sus IDs) */
    @Transaction
    @Query("SELECT * FROM words WHERE id IN (:ids)")
    fun getTodayWordsWithUsages(ids: List<Int>): Flow<List<WordWithUsages>>

    /** Palabras aprendidas en una semana concreta */
    @Transaction
    @Query("SELECT * FROM words WHERE isLearned = 1 AND weekLearned = :week")
    suspend fun getLearnedWordsByWeek(week: Int): List<WordWithUsages>

    /** Verifica si todas las palabras de la lista están aprendidas */
    @Query("SELECT COUNT(*) FROM words WHERE id IN (:ids) AND isLearned = 0")
    suspend fun countPendingWords(ids: List<Int>): Int

    /** Total de palabras no asignadas */
    @Query("SELECT COUNT(*) FROM words WHERE isAssigned = 0")
    suspend fun countUnassignedWords(): Int

    /** Todas las palabras (para debug / ciclo completado) */
    @Query("SELECT * FROM words")
    fun getAllWords(): Flow<List<WordEntity>>

    /** Resetea el ciclo: desmarca isAssigned e isLearned en todas */
    @Query("UPDATE words SET isAssigned = 0, isLearned = 0, assignedDate = 0, learnedDate = 0, weekLearned = 0")
    suspend fun resetAllWords()
}
