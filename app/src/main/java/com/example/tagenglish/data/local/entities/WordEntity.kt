package com.example.tagenglish.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val word: String,
    val isAssigned: Boolean = false,
    val assignedDate: Long = 0L,
    val isLearned: Boolean = false,
    val learnedDate: Long = 0L,
    val weekLearned: Int = 0
)