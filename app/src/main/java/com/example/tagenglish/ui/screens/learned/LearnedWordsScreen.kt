package com.example.tagenglish.ui.screens.learned

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tagenglish.domain.model.Word
import com.example.tagenglish.ui.viewmodels.LearnedWordsViewModel

// ─── Paleta ───────────────────────────────────────────────────────────────────

private val BgDark        = Color(0xFF0D0D0F)
private val BgCard        = Color(0xFF16161A)
private val AccentLime    = Color(0xFFCAFF4D)
private val AccentBlue    = Color(0xFF4D9EFF)
private val AccentPurple  = Color(0xFFB66DFF)
private val TextPrimary   = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFF8A8A9A)
private val TextMuted     = Color(0xFF4A4A5A)
private val Learned       = Color(0xFF1ECC7A)
private val CardBorder    = Color(0xFF252530)
private val LearnedBorder = Color(0xFF1A3024)
private val LearnedBg     = Color(0xFF141A16)

@Composable
fun LearnedWordsScreen(
    viewModel: LearnedWordsViewModel,
    onBack: () -> Unit
) {
    val uiState           by viewModel.uiState.collectAsState()
    val snackbarHostState =  remember { SnackbarHostState() }
    val context           = LocalContext.current

    LaunchedEffect(Unit) { viewModel.initTts(context) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        containerColor = BgDark,
        snackbarHost   = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Top bar ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF141420), BgDark)))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Aprendidas", fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                        Text("${uiState.totalLearned} palabras en total", fontSize = 12.sp, color = TextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1A3024))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("${uiState.totalLearned}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Learned)
                    }
                }
            }

            // ── Contenido ─────────────────────────────────────────────────────
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentLime, strokeWidth = 3.dp)
                    }
                }

                uiState.groupedWords.isEmpty() -> EmptyLearnedState()

                else -> {
                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        uiState.groupedWords.forEach { (week, words) ->
                            item(key = "week_$week") {
                                WeekHeader(week = week, count = words.size)
                            }
                            itemsIndexed(items = words, key = { _, w -> w.id }) { index, word ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter   = fadeIn(tween(250, delayMillis = index * 50)) +
                                            slideInVertically(tween(250, delayMillis = index * 50)) { it / 4 }
                                ) {
                                    LearnedWordCard(
                                        word           = word,
                                        index          = index,
                                        isLoadingAudio = uiState.loadingAudioWordId == word.id,
                                        onRevert       = { viewModel.unmarkAsLearned(word.id) },
                                        onPlayAudio    = { viewModel.playPronunciation(context, word.id, word.word) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── WeekHeader ──────────────────────────────────────────────────────────────

@Composable
private fun WeekHeader(week: Int, count: Int) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(4.dp).height(20.dp).clip(CircleShape)
                .background(Brush.verticalGradient(listOf(AccentLime, AccentBlue)))
        )
        Spacer(Modifier.width(10.dp))
        Text("Semana $week", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(CardBorder)
                .padding(horizontal = 7.dp, vertical = 2.dp)
        ) {
            Text("$count palabras", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.weight(1f))
        Box(modifier = Modifier.weight(1f).height(1.dp).background(CardBorder))
    }
}

// ─── LearnedWordCard ──────────────────────────────────────────────────────────

@Composable
private fun LearnedWordCard(
    word: Word,
    index: Int,
    isLoadingAudio: Boolean,
    onRevert: () -> Unit,
    onPlayAudio: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    val accentColor = when (index % 3) {
        0    -> AccentLime
        1    -> AccentBlue
        else -> AccentPurple
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest  = { showConfirm = false },
            containerColor    = Color(0xFF1C1C22),
            titleContentColor = TextPrimary,
            textContentColor  = TextSecondary,
            title = {
                Text("¿Revertir \"${word.word}\"?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text("La palabra volverá a estar pendiente en tu lista de hoy.", fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = { showConfirm = false; onRevert() },
                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B), contentColor = Color.White),
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Revertir", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(18.dp),
            colors   = CardDefaults.cardColors(containerColor = LearnedBg),
            border   = androidx.compose.foundation.BorderStroke(1.dp, LearnedBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {

                // ── Encabezado ────────────────────────────────────────────────
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Dot + palabra + fonética
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Learned))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text          = word.word,
                                fontSize      = 24.sp,
                                fontWeight    = FontWeight.Black,
                                color         = Learned,
                                letterSpacing = (-0.5).sp
                            )
                            if (word.phonetic.isNotBlank()) {
                                Text(
                                    text      = word.phonetic,
                                    fontSize  = 12.sp,
                                    color     = TextSecondary,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Badge aprendida
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(7.dp))
                                .background(Color(0xFF1A3024))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("✓ aprendida", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Learned)
                        }

                        Spacer(Modifier.width(6.dp))

                        // Botón audio 🔊
                        Box(
                            modifier         = Modifier.size(32.dp).clip(CircleShape).background(CardBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoadingAudio) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = accentColor, strokeWidth = 2.dp)
                            } else {
                                IconButton(onClick = onPlayAudio, modifier = Modifier.size(32.dp)) {
                                    Text("🔊", fontSize = 14.sp)
                                }
                            }
                        }

                        Spacer(Modifier.width(6.dp))

                        // Botón revertir
                        IconButton(
                            onClick  = { showConfirm = true },
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF2A1A1A))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Revertir", tint = Color(0xFFFF8080), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ── Significados ──────────────────────────────────────────────
                word.usages.forEachIndexed { i, usage ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(bottom = if (i < word.usages.size - 1) 10.dp else 0.dp)
                    ) {
                        Box(
                            modifier = Modifier.width(3.dp).height(44.dp).clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.35f))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(usage.meaning, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                usage.example, fontSize = 12.sp, fontStyle = FontStyle.Italic,
                                color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── EmptyLearnedState ────────────────────────────────────────────────────────

@Composable
private fun EmptyLearnedState() {
    Box(modifier = Modifier.fillMaxSize().padding(top = 80.dp), contentAlignment = Alignment.TopCenter) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🌱", fontSize = 56.sp)
            Spacer(Modifier.height(20.dp))
            Text("Aún no has aprendido palabras", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Text(
                "Marca palabras como aprendidas\ndesde la pantalla principal",
                fontSize = 13.sp, color = TextSecondary
            )
        }
    }
}