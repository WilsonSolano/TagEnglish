package com.example.tagenglish.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_results")
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val weekId: Int,
    val score: Int,
    val totalQuestions: Int,
    val date: Long
)
