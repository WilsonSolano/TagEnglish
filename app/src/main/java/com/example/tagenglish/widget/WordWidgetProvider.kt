package com.example.tagenglish.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.tagenglish.MainActivity
import com.example.tagenglish.R
import com.example.tagenglish.data.local.database.AppDatabase
import com.example.tagenglish.data.preferences.AppPreferences
import com.example.tagenglish.data.repository.WordRepositoryImpl
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
        // Escuchar el broadcast que manda HomeViewModel al marcar aprendida
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
                // Leer datos frescos de la DB
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

                // Construir RemoteViews
                val views = RemoteViews(context.packageName, R.layout.widget_layout)

                // Click en el widget → abrir app
                val launchIntent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)

                // Contador
                views.setTextViewText(R.id.widget_counter, "$learnedCount/$totalCount")

                // Barra de progreso
                val progress = if (totalCount > 0) (learnedCount * 100 / totalCount) else 0
                views.setProgressBar(R.id.widget_progress, 100, progress, false)

                // Contenido según estado
                val allLearned = totalCount > 0 && learnedCount >= totalCount

                when {
                    totalCount == 0 || targetWord == null -> {
                        views.setTextViewText(R.id.widget_word, "Abre la app")
                        views.setTextViewText(R.id.widget_meaning1, "")
                        views.setTextViewText(R.id.widget_meaning2, "")
                    }
                    allLearned -> {
                        views.setTextViewText(R.id.widget_word, "¡Día completo! ✅")
                        views.setTextViewText(R.id.widget_meaning1, "Vuelve mañana")
                        views.setTextViewText(R.id.widget_meaning2, "")
                    }
                    else -> {
                        views.setTextViewText(R.id.widget_word, targetWord.word)

                        val u1 = targetWord.usages.getOrNull(0)
                        val u2 = targetWord.usages.getOrNull(1)

                        views.setTextViewText(
                            R.id.widget_meaning1,
                            if (u1 != null) "${u1.meaning} · ${u1.example}" else ""
                        )
                        views.setTextViewText(
                            R.id.widget_meaning2,
                            if (u2 != null) "${u2.meaning} · ${u2.example}" else ""
                        )
                    }
                }

                // Aplicar al widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        // Llamar esto desde HomeViewModel para forzar refresh inmediato
        fun requestUpdate(context: Context) {
            val intent = Intent(context, WordWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
}
