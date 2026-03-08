package com.example.tagenglish.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
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
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.tagenglish.MainActivity
import com.example.tagenglish.data.local.database.AppDatabase
import com.example.tagenglish.data.preferences.AppPreferences
import com.example.tagenglish.data.repository.WordRepositoryImpl
import com.example.tagenglish.domain.model.Word
import kotlinx.coroutines.flow.first

// ─── Colores ──────────────────────────────────────────────────────────────────

private val WidgetBg         = Color(0xFF16161A)
private val WidgetAccentLime = Color(0xFFCAFF4D)
private val WidgetTextPrim   = Color(0xFFF5F5F5)
private val WidgetTextSec    = Color(0xFF8A8A9A)
private val WidgetTextMuted  = Color(0xFF4A4A5A)
private val WidgetLearned    = Color(0xFF1ECC7A)
private val WidgetBtnBg      = Color(0xFF252530)

// ─── Widget ───────────────────────────────────────────────────────────────────

class WordWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
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

        provideContent {
            WidgetContent(
                word         = targetWord,
                learnedCount = learnedCount,
                totalCount   = totalCount,
                context      = context
            )
        }
    }
}

// ─── UI ───────────────────────────────────────────────────────────────────────

@Composable
private fun WidgetContent(
    word: Word?,
    learnedCount: Int,
    totalCount: Int,
    context: Context
) {
    val allLearned   = totalCount > 0 && learnedCount >= totalCount
    val launchIntent = Intent(context, MainActivity::class.java)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetBg)
            .cornerRadius(18.dp)
            .clickable(actionStartActivity(launchIntent)),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {

            // ── Header: título + badge progreso ───────────────────────────────
            Row(
                modifier          = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text  = "TagEnglish",
                    style = TextStyle(
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = ColorProvider(WidgetAccentLime)
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                Box(
                    modifier = GlanceModifier
                        .background(WidgetBtnBg)
                        .cornerRadius(5.dp)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text  = "$learnedCount/$totalCount",
                        style = TextStyle(
                            fontSize   = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color      = ColorProvider(
                                if (allLearned) WidgetLearned else WidgetAccentLime
                            )
                        )
                    )
                }
            }

            Spacer(GlanceModifier.height(3.dp))

            // ── Barra de progreso ─────────────────────────────────────────────
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(WidgetBtnBg)
                    .cornerRadius(4.dp)
            ) { }

            if (learnedCount > 0 && totalCount > 0) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(WidgetAccentLime)
                        .cornerRadius(4.dp)
                ) { }
            }

            Spacer(GlanceModifier.height(8.dp))

            // ── Contenido ─────────────────────────────────────────────────────
            when {
                totalCount == 0 || word == null -> {
                    Text(
                        text  = "Abre la app 🚀",
                        style = TextStyle(fontSize = 11.sp, color = ColorProvider(WidgetTextSec))
                    )
                }

                allLearned -> {
                    Text(
                        text  = "✅ ¡Día completo!",
                        style = TextStyle(
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color      = ColorProvider(WidgetLearned)
                        )
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text  = "Vuelve mañana",
                        style = TextStyle(fontSize = 11.sp, color = ColorProvider(WidgetTextSec))
                    )
                }

                else -> {
                    // ── Palabra ───────────────────────────────────────────────
                    Text(
                        text  = word.word,
                        style = TextStyle(
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color      = ColorProvider(WidgetTextPrim)
                        )
                    )

                    // Fonética (si ya fue cargada desde la API)
                    if (word.phonetic.isNotBlank()) {
                        Spacer(GlanceModifier.height(1.dp))
                        Text(
                            text  = word.phonetic,
                            style = TextStyle(
                                fontSize  = 10.sp,
                                fontStyle = FontStyle.Italic,
                                color     = ColorProvider(WidgetTextMuted)
                            )
                        )
                    }

                    Spacer(GlanceModifier.height(7.dp))

                    // ── Significado 1: meaning + ejemplo ─────────────────────
                    word.usages.getOrNull(0)?.let { usage ->
                        Text(
                            text  = usage.meaning,
                            style = TextStyle(
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color      = ColorProvider(WidgetTextPrim)
                            )
                        )
                        Text(
                            text  = usage.example,
                            style = TextStyle(
                                fontSize  = 9.sp,
                                fontStyle = FontStyle.Italic,
                                color     = ColorProvider(WidgetTextSec)
                            )
                        )
                    }

                    Spacer(GlanceModifier.height(5.dp))

                    // ── Significado 2: meaning + ejemplo ─────────────────────
                    word.usages.getOrNull(1)?.let { usage ->
                        Text(
                            text  = usage.meaning,
                            style = TextStyle(
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color      = ColorProvider(WidgetTextSec)
                            )
                        )
                        Text(
                            text  = usage.example,
                            style = TextStyle(
                                fontSize  = 9.sp,
                                fontStyle = FontStyle.Italic,
                                color     = ColorProvider(WidgetTextMuted)
                            )
                        )
                    }
                }
            }
        }
    }
}

// ─── ActionCallback vacío (se mantiene por si se necesita en el futuro) ───────

class MarkLearnedAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: androidx.glance.action.ActionParameters
    ) {
        WordWidget().updateAll(context)
    }
}