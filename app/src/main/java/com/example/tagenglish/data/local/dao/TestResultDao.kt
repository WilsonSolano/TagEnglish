package com.example.tagenglish.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tagenglish.data.local.entities.TestResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: TestResultEntity)

    /** Historial completo de tests ordenado por fecha descendente */
    @Query("SELECT * FROM test_results ORDER BY date DESC")
    fun getAllResults(): Flow<List<TestResultEntity>>

    /** Resultado de una semana específica */
    @Query("SELECT * FROM test_results WHERE weekId = :week LIMIT 1")
    suspend fun getResultByWeek(week: Int): TestResultEntity?

    /** ¿Ya existe resultado para esta semana? (para saber si el test fue completado) */
    @Query("SELECT COUNT(*) FROM test_results WHERE weekId = :week")
    suspend fun hasResultForWeek(week: Int): Int
}
