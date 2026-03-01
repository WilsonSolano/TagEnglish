package com.example.tagenglish.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tagenglish.domain.model.Word
import com.example.tagenglish.ui.viewmodels.HomeViewModel

// ─── Paleta de colores ────────────────────────────────────────────────────────

private val BgDark       = Color(0xFF0D0D0F)
private val BgCard       = Color(0xFF16161A)
private val BgCardAlt    = Color(0xFF1C1C22)
private val AccentLime   = Color(0xFFCAFF4D)
private val AccentBlue   = Color(0xFF4D9EFF)
private val AccentPurple = Color(0xFFB66DFF)
private val TextPrimary  = Color(0xFFF5F5F5)
private val TextSecondary= Color(0xFF8A8A9A)
private val TextMuted    = Color(0xFF4A4A5A)
private val Learned      = Color(0xFF1ECC7A)
private val CardBorder   = Color(0xFF252530)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartTest: (weekId: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }

    LaunchedEffect(uiState.weeklyTestReady) {
        if (uiState.weeklyTestReady) onStartTest(uiState.currentWeekId)
    }

    Scaffold(
        containerColor = BgDark,
        snackbarHost   = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentLime, strokeWidth = 3.dp)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(padding),
            contentPadding      = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Header ────────────────────────────────────────────────────────
            item {
                AppHeader(
                    week     = uiState.currentWeekId,
                    learned  = uiState.progress.learnedWords,
                    total    = uiState.progress.totalWords
                )
            }

            // ── Progress Card ─────────────────────────────────────────────────
            item {
                ProgressSection(
                    learned = uiState.progress.learnedWords,
                    total   = uiState.progress.totalWords
                )
            }

            // ── Ciclo completado ──────────────────────────────────────────────
            if (uiState.isCycleCompleted) {
                item {
                    CycleCompletedBanner(onDismiss = viewModel::dismissCycleCompleted)
                }
            }

            // ── Palabras ──────────────────────────────────────────────────────
            if (uiState.words.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                itemsIndexed(uiState.words, key = { _, w -> w.id }) { index, word ->
                    AnimatedVisibility(
                        visible = true,
                        enter   = fadeIn(tween(300, delayMillis = index * 80)) +
                                slideInVertically(tween(300, delayMillis = index * 80)) { it / 3 }
                    ) {
                        WordCard(
                            word          = word,
                            index         = index,
                            onMarkLearned = { viewModel.markAsLearned(word.id) }
                        )
                    }
                }
            }
        }
    }
}

// ─── AppHeader ────────────────────────────────────────────────────────────────

@Composable
private fun AppHeader(week: Int, learned: Int, total: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF141420), BgDark)
                )
            )
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Row(
            modifier       = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text       = "TagEnglish",
                    fontSize   = 30.sp,
                    fontWeight = FontWeight.Black,
                    color      = TextPrimary,
                    letterSpacing = (-1).sp
                )
                Text(
                    text  = "Semana $week · aprende inglés",
                    fontSize  = 13.sp,
                    color     = TextSecondary,
                    letterSpacing = 0.3.sp
                )
            }

            // Badge semana
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(listOf(AccentPurple, AccentBlue))
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text       = "W$week",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
            }
        }
    }
}

// ─── ProgressSection ─────────────────────────────────────────────────────────

@Composable
private fun ProgressSection(learned: Int, total: Int) {
    val fraction = if (total == 0) 0f else learned.toFloat() / total
    val animFraction by animateFloatAsState(
        targetValue  = fraction,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label        = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(20.dp),
            colors   = CardDefaults.cardColors(containerColor = BgCard),
            border   = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier       = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "Progreso de hoy",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text       = "$learned / $total",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Black,
                        color      = if (fraction >= 1f) Learned else AccentLime
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Barra de progreso custom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF252530))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animFraction)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AccentLime, AccentBlue)
                                )
                            )
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text  = when {
                        total == 0      -> "Cargando palabras..."
                        fraction >= 1f  -> "✅ ¡Día completado! Vuelve mañana"
                        fraction >= 0.5f-> "💪 ¡Vas a la mitad!"
                        else            -> "🚀 ¡Empieza a aprender!"
                    },
                    fontSize = 12.sp,
                    color    = TextMuted
                )
            }
        }
    }
}

// ─── WordCard ─────────────────────────────────────────────────────────────────

@Composable
private fun WordCard(word: Word, index: Int, onMarkLearned: () -> Unit) {
    // Colores de acento rotando por índice
    val accentColor = when (index % 3) {
        0    -> AccentLime
        1    -> AccentBlue
        else -> AccentPurple
    }

    var expanded by remember { mutableStateOf(!word.isLearned) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(20.dp),
            colors   = CardDefaults.cardColors(
                containerColor = if (word.isLearned) Color(0xFF141A16) else BgCard
            ),
            border   = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (word.isLearned) Color(0xFF1A3024) else CardBorder
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // ── Encabezado de la tarjeta ──────────────────────────────────
                Row(
                    modifier       = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Dot de acento
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (word.isLearned) Learned else accentColor)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text       = word.word,
                            fontSize   = 26.sp,
                            fontWeight = FontWeight.Black,
                            color      = if (word.isLearned) Learned else TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    AnimatedVisibility(
                        visible = word.isLearned,
                        enter   = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1A3024))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text      = "✓ aprendida",
                                fontSize  = 11.sp,
                                fontWeight= FontWeight.Bold,
                                color     = Learned
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Significados ──────────────────────────────────────────────
                word.usages.forEachIndexed { i, usage ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (i < word.usages.size - 1) 12.dp else 0.dp)
                    ) {
                        // Línea vertical de acento
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(48.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.4f))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text       = usage.meaning,
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary
                            )
                            Text(
                                text      = usage.example,
                                fontSize  = 13.sp,
                                fontStyle = FontStyle.Italic,
                                color     = TextSecondary,
                                maxLines  = 2,
                                overflow  = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // ── Botón aprender ────────────────────────────────────────────
                if (!word.isLearned) {
                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick  = onMarkLearned,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor   = Color(0xFF0D0D0F)
                        )
                    ) {
                        Text(
                            text       = "Marcar como aprendida",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 14.sp,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── CycleCompletedBanner ─────────────────────────────────────────────────────

@Composable
private fun CycleCompletedBanner(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(20.dp),
            colors   = CardDefaults.cardColors(containerColor = Color(0xFF1A1A0D)),
            border   = androidx.compose.foundation.BorderStroke(1.dp, AccentLime.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text       = "🎉 ¡Ciclo completado!",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Black,
                    color      = AccentLime
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text  = "Has aprendido todo el vocabulario disponible. ¿Volvemos a empezar?",
                    fontSize = 13.sp,
                    color    = TextSecondary
                )
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    shape   = RoundedCornerShape(12.dp),
                    border  = androidx.compose.foundation.BorderStroke(1.5.dp, AccentLime),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = AccentLime)
                ) {
                    Text("Reiniciar ciclo", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── EmptyState ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState() {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📖", fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text       = "No hay palabras hoy",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text  = "Vuelve mañana para nuevas palabras",
                fontSize = 13.sp,
                color    = TextSecondary
            )
        }
    }
}