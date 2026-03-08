package com.example.tagenglish.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.TextView
import com.example.tagenglish.MainActivity
import com.example.tagenglish.R
import com.example.tagenglish.data.local.database.AppDatabase
import com.example.tagenglish.data.preferences.AppPreferences
import com.example.tagenglish.data.repository.WordRepositoryImpl
import com.example.tagenglish.domain.model.Word
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WordWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, WordWidgetProvider::class.java)
            )
            ids.forEach { id -> updateWidget(context, manager, id) }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.example.tagenglish.WIDGET_REFRESH"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val db         = AppDatabase.getInstance(context)
                val prefs      = AppPreferences(context)
                val repository = WordRepositoryImpl(db.wordDao(), db.testResultDao())

                val todayIds   = prefs.todayWordIds.first()
                val todayWords = if (todayIds.isEmpty()) emptyList()
                else repository.getTodayWords(todayIds).first()

                val learnedCount = todayWords.count { it.isLearned }
                val totalCount   = todayWords.size
                val targetWord   = todayWords.firstOrNull { !it.isLearned }
                    ?: todayWords.firstOrNull()

                val views = RemoteViews(context.packageName, R.layout.widget_layout)

                // Click → abrir app
                val pending = PendingIntent.getActivity(
                    context, 0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_title, pending)

                // Contador + progreso
                views.setTextViewText(R.id.widget_counter, "$learnedCount/$totalCount")
                val progress = if (totalCount > 0) learnedCount * 100 / totalCount else 0
                views.setProgressBar(R.id.widget_progress, 100, progress, false)

                val allLearned = totalCount > 0 && learnedCount >= totalCount

                when {
                    totalCount == 0 || targetWord == null -> {
                        views.setTextViewText(R.id.widget_word, "Abre la app 🚀")
                        views.setViewVisibility(R.id.widget_phonetic, View.GONE)
                        views.removeAllViews(R.id.widget_usages_container)
                    }

                    allLearned -> {
                        views.setTextViewText(R.id.widget_word, "✅ ¡Día completo!")
                        views.setViewVisibility(R.id.widget_phonetic, View.GONE)
                        views.removeAllViews(R.id.widget_usages_container)

                        // Añadir "Vuelve mañana" como item
                        val row = buildUsageRow(context, "Vuelve mañana", "")
                        views.addView(R.id.widget_usages_container, row)
                    }

                    else -> {
                        // Palabra
                        views.setTextViewText(R.id.widget_word, targetWord.word)

                        // Fonética
                        if (targetWord.phonetic.isNotBlank()) {
                            views.setTextViewText(R.id.widget_phonetic, targetWord.phonetic)
                            views.setViewVisibility(R.id.widget_phonetic, View.VISIBLE)
                        } else {
                            views.setViewVisibility(R.id.widget_phonetic, View.GONE)
                        }

                        // Significados — todos, con scroll
                        views.removeAllViews(R.id.widget_usages_container)
                        targetWord.usages.forEach { usage ->
                            val row = buildUsageRow(context, usage.meaning, usage.example)
                            views.addView(R.id.widget_usages_container, row)
                        }
                    }
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        /**
         * Construye una fila RemoteViews para un significado.
         * RemoteViews solo acepta layouts XML — inflamos widget_usage_item.xml.
         */
        private fun buildUsageRow(
            context: Context,
            meaning: String,
            example: String
        ): RemoteViews {
            val row = RemoteViews(context.packageName, R.layout.widget_usage_item)
            row.setTextViewText(R.id.usage_meaning, meaning)
            if (example.isNotBlank()) {
                row.setTextViewText(R.id.usage_example, example)
                row.setViewVisibility(R.id.usage_example, View.VISIBLE)
            } else {
                row.setViewVisibility(R.id.usage_example, View.GONE)
            }
            return row
        }

        fun requestUpdate(context: Context) {
            val intent = Intent(context, WordWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
}