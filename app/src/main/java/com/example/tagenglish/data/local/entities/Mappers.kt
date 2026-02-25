package com.example.tagenglish.data.local.entities

import com.example.tagenglish.domain.model.TestResult
import com.example.tagenglish.domain.model.Usage
import com.example.tagenglish.domain.model.Word

// ─── WordWithUsages → Word ────────────────────────────────────────────────────

fun WordWithUsages.toDomain(): Word = Word(
    id           = word.id,
    word         = word.word,
    isAssigned   = word.isAssigned,
    assignedDate = word.assignedDate,
    isLearned    = word.isLearned,
    learnedDate  = word.learnedDate,
    weekLearned  = word.weekLearned,
    usages       = usages.map { it.toDomain() }
)

// ─── UsageEntity → Usage ──────────────────────────────────────────────────────

fun UsageEntity.toDomain(): Usage = Usage(
    id      = id,
    wordId  = wordId,
    meaning = meaning,
    example = example
)

// ─── TestResultEntity → TestResult ───────────────────────────────────────────

fun TestResultEntity.toDomain(): TestResult = TestResult(
    id             = id,
    weekId         = weekId,
    score          = score,
    totalQuestions = totalQuestions,
    date           = date
)

// ─── TestResult → TestResultEntity ───────────────────────────────────────────

fun TestResult.toEntity(): TestResultEntity = TestResultEntity(
    id             = id,
    weekId         = weekId,
    score          = score,
    totalQuestions = totalQuestions,
    date           = date
)
