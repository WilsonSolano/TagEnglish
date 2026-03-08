package com.example.tagenglish.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
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
import androidx.glance.layout.width
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
private val WidgetAccentBlue = Color(0xFF4D9EFF)
private val WidgetTextPrim   = Color(0xFFF5F5F5)
private val WidgetTextSec    = Color(0xFF8A8A9A)
private val WidgetLearned    = Color(0xFF1ECC7A)
private val WidgetBtnBg      = Color(0xFF252530)
private val WidgetDark       = Color(0xFF0D0D0F)

val wordIdKey = ActionParameters.Key<Int>("word_id")

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

    // Box externo fillMaxSize con fondo oscuro — cubre toda la celda 2x2
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
                .padding(10.dp)
        ) {

            // ── Header: título + badge ────────────────────────────────────────
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
                Row(
                    modifier = GlanceModifier
                        .background(WidgetBtnBg)
                        .cornerRadius(5.dp)
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
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

            Spacer(GlanceModifier.height(6.dp))

            // ── Contenido según estado ────────────────────────────────────────
            when {
                totalCount == 0 || word == null -> {
                    Text(
                        text  = "Abre la app 🚀",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color    = ColorProvider(WidgetTextSec)
                        )
                    )
                }

                allLearned -> {
                    Text(
                        text  = "✅ ¡Día completo!",
                        style = TextStyle(
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color      = ColorProvider(WidgetLearned)
                        )
                    )
                    Spacer(GlanceModifier.height(3.dp))
                    Text(
                        text  = "Vuelve mañana",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color    = ColorProvider(WidgetTextSec)
                        )
                    )
                }

                else -> {
                    // ── Palabra ───────────────────────────────────────────────
                    Text(
                        text  = word.word,
                        style = TextStyle(
                            fontSize   = 19.sp,       // reducido de 22 → 19
                            fontWeight = FontWeight.Bold,
                            color      = ColorProvider(WidgetTextPrim)
                        )
                    )

                    Spacer(GlanceModifier.height(5.dp))

                    // ── Solo 1 significado para que quepa en 2x2 ─────────────
                    word.usages.take(1).forEach { usage ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = GlanceModifier
                                    .width(2.dp)
                                    .height(24.dp)
                                    .background(WidgetAccentBlue)
                                    .cornerRadius(4.dp)
                            ) { }
                            Spacer(GlanceModifier.width(6.dp))
                            Column {
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
                                        fontSize = 9.sp,
                                        color    = ColorProvider(WidgetTextSec)
                                    )
                                )
                            }
                        }
                    }

                    // Si hay 2+ significados, mostrar el segundo más pequeño
                    if (word.usages.size >= 2) {
                        Spacer(GlanceModifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = GlanceModifier
                                    .width(2.dp)
                                    .height(14.dp)
                                    .background(WidgetAccentBlue)
                                    .cornerRadius(4.dp)
                            ) { }
                            Spacer(GlanceModifier.width(6.dp))
                            Text(
                                text  = word.usages[1].meaning,
                                style = TextStyle(
                                    fontSize   = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = ColorProvider(WidgetTextSec)
                                )
                            )
                        }
                    }

                    Spacer(GlanceModifier.height(6.dp))

                    // ── Botón ─────────────────────────────────────────────────
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(WidgetAccentLime)
                            .cornerRadius(8.dp)
                            .clickable(
                                actionRunCallback<MarkLearnedAction>(
                                    actionParametersOf(wordIdKey to word.id)
                                )
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment   = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = "✓ Aprendida",
                            style = TextStyle(
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color      = ColorProvider(WidgetDark)
                            )
                        )
                    }
                }
            }
        }
    }
}

// ─── ActionCallback ───────────────────────────────────────────────────────────

class MarkLearnedAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val wordId = parameters[wordIdKey] ?: return

        val db         = AppDatabase.getInstance(context)
        val prefs      = AppPreferences(context)
        val repository = WordRepositoryImpl(db.wordDao(), db.testResultDao())
        val week       = prefs.currentWeek.first()

        repository.markWordAsLearned(
            wordId = wordId,
            date   = System.currentTimeMillis(),
            week   = week
        )

        WordWidget().updateAll(context)
    }
}