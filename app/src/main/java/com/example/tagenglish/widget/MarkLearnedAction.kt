package com.example.tagenglish.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.example.tagenglish.data.local.database.AppDatabase
import com.example.tagenglish.data.preferences.AppPreferences
import com.example.tagenglish.data.repository.WordRepositoryImpl
import kotlinx.coroutines.flow.first

class MarkLearnedAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val wordId = parameters[wordIdKey] ?: return

        // Marcar como aprendida en Room
        val db          = AppDatabase.getInstance(context)
        val preferences = AppPreferences(context)
        val repository  = WordRepositoryImpl(db.wordDao(), db.testResultDao())

        val currentWeek = preferences.currentWeek.first()

        repository.markWordAsLearned(
            wordId = wordId,
            date   = System.currentTimeMillis(),
            week   = currentWeek
        )

        // Refrescar el widget
        WordWidget().updateAll(context)
    }

    companion object {
        private val wordIdKey = ActionParameters.Key<Int>("word_id")

        fun provideParameters(wordId: Int): ActionParameters =
            actionParametersOf(wordIdKey to wordId)
    }
}
