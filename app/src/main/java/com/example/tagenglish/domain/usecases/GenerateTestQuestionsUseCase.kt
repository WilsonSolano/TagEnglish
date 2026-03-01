package com.example.tagenglish.domain.usecases

import com.example.tagenglish.data.repository.WordRepository
import com.example.tagenglish.domain.model.Question
import com.example.tagenglish.domain.model.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GenerateTestQuestionsUseCase(
    private val repository: WordRepository
) {
    suspend operator fun invoke(weekId: Int): List<Question> = withContext(Dispatchers.IO) {
        val words = repository.getLearnedWordsByWeek(weekId)
        if (words.isEmpty()) return@withContext emptyList()

        val questions = mutableListOf<Question>()

        words.shuffled().forEach { word ->
            val usage = word.usages.randomOrNull() ?: return@forEach

            when ((0..2).random()) {
                0 -> questions.add(buildMultipleChoice(word, words))
                1 -> questions.add(buildFillInBlank(word, usage.example))
                2 -> questions.add(buildReverseTranslation(word, words))
            }
        }

        return@withContext questions.shuffled()
    }

    private fun buildMultipleChoice(
        correct: Word,
        allWords: List<Word>
    ): Question.MultipleChoiceMeaning {
        val correctMeaning = correct.usages.randomOrNull()?.meaning ?: ""

        val distractors = allWords
            .filter { it.id != correct.id }
            .shuffled()
            .take(3)
            .mapNotNull { it.usages.randomOrNull()?.meaning }

        val options = (distractors + correctMeaning).shuffled()

        return Question.MultipleChoiceMeaning(
            wordId         = correct.id,
            word           = correct.word,
            correctMeaning = correctMeaning,
            options        = options
        )
    }

    private fun buildFillInBlank(
        word: Word,
        example: String
    ): Question.FillInBlank {
        // Fix: usar replace(oldValue, newValue, ignoreCase) sin parámetros nombrados
        val blanked = example.replace(word.word, "___", ignoreCase = true)

        return Question.FillInBlank(
            wordId      = word.id,
            example     = blanked,
            correctWord = word.word
        )
    }

    private fun buildReverseTranslation(
        correct: Word,
        allWords: List<Word>
    ): Question.ReverseTranslation {
        val meaning = correct.usages.randomOrNull()?.meaning ?: ""

        val distractors = allWords
            .filter { it.id != correct.id }
            .shuffled()
            .take(3)
            .map { it.word }

        val options = (distractors + correct.word).shuffled()

        return Question.ReverseTranslation(
            wordId      = correct.id,
            meaning     = meaning,
            correctWord = correct.word,
            options     = options
        )
    }
}
