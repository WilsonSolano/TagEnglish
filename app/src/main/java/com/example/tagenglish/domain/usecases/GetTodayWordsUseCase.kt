package com.example.tagenglish.domain.usecases

import com.example.tagenglish.data.repository.WordRepository
import com.example.tagenglish.domain.model.DailyProgress
import com.example.tagenglish.domain.model.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Obtiene las palabras asignadas hoy y calcula el progreso del día.
 *
 * Uso:
 *   val flow = getTodayWordsUseCase(todayIds)
 *   flow.collect { (words, progress) -> ... }
 */
class GetTodayWordsUseCase(
    private val repository: WordRepository
) {
    operator fun invoke(todayIds: List<Int>): Flow<Pair<List<Word>, DailyProgress>> =
        repository.getTodayWords(todayIds).map { words ->
            val learned  = words.count { it.isLearned }
            val progress = DailyProgress(
                totalWords   = words.size,
                learnedWords = learned
            )
            Pair(words, progress)
        }.flowOn(Dispatchers.IO)
}
