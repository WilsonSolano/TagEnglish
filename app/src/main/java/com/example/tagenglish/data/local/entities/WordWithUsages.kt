package com.example.tagenglish.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class WordWithUsages(
    @Embedded val word: WordEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "wordId"
    )
    val usages: List<UsageEntity>
)
