package com.example.tagenglish.domain.usecases

import com.example.tagenglish.data.repository.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UnmarkWordAsLearnedUseCase(
    private val repository: WordRepository
) {
    suspend operator fun invoke(wordId: Int) = withContext(Dispatchers.IO) {
        repository.unmarkWordAsLearned(wordId)
    }
}
