package com.example.tagenglish.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.color.ColorProviders
import com.example.tagenglish.MainActivity
import com.example.tagenglish.data.local.database.AppDatabase
import com.example.tagenglish.data.preferences.AppPreferences
import com.example.tagenglish.data.repository.WordRepositoryImpl
import com.example.tagenglish.domain.model.Word
import kotlinx.coroutines.flow.first

class WordWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // ── Cargar datos directamente desde Room y DataStore ──────────────────
        val db          = AppDatabase.getInstance(context)
        val preferences = AppPreferences(context)
        val repository  = WordRepositoryImpl(db.wordDao(), db.testResultDao())

        val todayIds    = preferences.todayWordIds.first()
        val wordsPerDay = preferences.wordsPerDay.first()

        val todayWords  = if (todayIds.isEmpty()) emptyList()
                          else repository.getTodayWords(todayIds).first()

        val learnedCount = todayWords.count { it.isLearned }
        val firstWord    = todayWords.firstOrNull()

        provideContent {
            GlanceTheme {
                WidgetContent(
                    firstWord    = firstWord,
                    learnedCount = learnedCount,
                    totalCount   = wordsPerDay,
                    context      = context
                )
            }
        }
    }
}

// ─── UI del Widget ────────────────────────────────────────────────────────────

@Composable
private fun WidgetContent(
    firstWord: Word?,
    learnedCount: Int,
    totalCount: Int,
    context: Context
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.Top
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier          = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text  = "TagEnglish",
                style = TextStyle(
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = GlanceTheme.colors.primary
                )
            )
        }

        Spacer(GlanceModifier.height(8.dp))

        // ── Progreso del día ──────────────────────────────────────────────────
        Text(
            text  = "📚 $learnedCount / $totalCount aprendidas",
            style = TextStyle(
                fontSize = 13.sp,
                color    = GlanceTheme.colors.onBackground
            )
        )

        Spacer(GlanceModifier.height(12.dp))

        // ── Palabra del día ───────────────────────────────────────────────────
        if (firstWord != null) {
            Text(
                text  = firstWord.word,
                style = TextStyle(
                    fontSize   = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color      = GlanceTheme.colors.primary
                )
            )

            Spacer(GlanceModifier.height(4.dp))

            // Primer significado
            val firstMeaning = firstWord.usages.firstOrNull()
            if (firstMeaning != null) {
                Text(
                    text  = firstMeaning.meaning,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color    = GlanceTheme.colors.onBackground
                    )
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text  = firstMeaning.example,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color    = GlanceTheme.colors.onBackground
                    )
                )
            }

            Spacer(GlanceModifier.height(8.dp))

            // Estado de la palabra
            val statusText = if (firstWord.isLearned) "✅ Aprendida" else "⏳ Pendiente"
            Text(
                text  = statusText,
                style = TextStyle(
                    fontSize = 12.sp,
                    color    = GlanceTheme.colors.onBackground
                )
            )
        } else {
            Text(
                text  = "Abre la app para comenzar 🚀",
                style = TextStyle(
                    fontSize = 14.sp,
                    color    = GlanceTheme.colors.onBackground
                )
            )
        }
    }
}
