package com.example.tagenglish.ui.screens.vocabulary

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tagenglish.ui.viewmodels.Message
import com.example.tagenglish.ui.viewmodels.VocabularyManagerViewModel
import kotlinx.coroutines.launch

// ─── Paleta ───────────────────────────────────────────────────────────────────

private val BgDark        = Color(0xFF0D0D0F)
private val BgCard        = Color(0xFF16161A)
private val BgCardAlt     = Color(0xFF1C1C22)
private val AccentLime    = Color(0xFFCAFF4D)
private val AccentBlue    = Color(0xFF4D9EFF)
private val AccentPurple  = Color(0xFFB66DFF)
private val AccentOrange  = Color(0xFFFF9A3C)
private val TextPrimary   = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFF8A8A9A)
private val TextMuted     = Color(0xFF4A4A5A)
private val Learned       = Color(0xFF1ECC7A)
private val CardBorder    = Color(0xFF252530)
private val ErrorRed      = Color(0xFFFF6B6B)

@Composable
fun VocabularyManagerScreen(
    viewModel: VocabularyManagerViewModel,
    onBack: () -> Unit
) {
    val uiState      by viewModel.uiState.collectAsState()
    val context      = LocalContext.current
    val scope        = rememberCoroutineScope()

    // Estado de feedback persistente en pantalla (no snackbar)
    var feedbackMsg  by remember { mutableStateOf<Message?>(null) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            feedbackMsg = it
            viewModel.dismissMessage()
        }
    }

    // Launcher para abrir el selector de archivo JSON
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromUri(context, it) }
    }

    Scaffold(containerColor = BgDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Top bar ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF141420), BgDark)))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextSecondary
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text(
                            text       = "Vocabulario",
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Black,
                            color      = TextPrimary
                        )
                        Text(
                            text     = "Gestiona tus palabras",
                            fontSize = 12.sp,
                            color    = TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Stats cards ───────────────────────────────────────────────────
            if (!uiState.isLoading) {
                Row(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatChip(
                        modifier = Modifier.weight(1f),
                        value    = "${uiState.totalWords}",
                        label    = "Total",
                        color    = AccentBlue
                    )
                    StatChip(
                        modifier = Modifier.weight(1f),
                        value    = "${uiState.learnedCount}",
                        label    = "Aprendidas",
                        color    = Learned
                    )
                    StatChip(
                        modifier = Modifier.weight(1f),
                        value    = "${uiState.pendingCount}",
                        label    = "Pendientes",
                        color    = AccentOrange
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Banner de feedback ────────────────────────────────────────────
            AnimatedVisibility(
                visible = feedbackMsg != null,
                enter   = fadeIn(tween(200)) + expandVertically(),
                exit    = fadeOut(tween(200)) + shrinkVertically()
            ) {
                feedbackMsg?.let { msg ->
                    FeedbackBanner(
                        message   = msg,
                        onDismiss = { feedbackMsg = null }
                    )
                }
            }

            // ── Sección 1: Importar JSON ──────────────────────────────────────
            SectionCard(
                emoji    = "📥",
                title    = "Importar palabras",
                subtitle = "Carga un archivo JSON con nuevas palabras"
            ) {
                Text(
                    text     = "Selecciona un archivo .json desde tu dispositivo. Las palabras que ya existen en el sistema serán ignoradas automáticamente.",
                    fontSize = 13.sp,
                    color    = TextSecondary,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick  = { filePicker.launch("application/json") },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    enabled  = !uiState.isProcessing,
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = AccentLime,
                        contentColor           = Color(0xFF0D0D0F),
                        disabledContainerColor = CardBorder,
                        disabledContentColor   = TextMuted
                    )
                ) {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            color       = TextMuted,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Procesando...", fontWeight = FontWeight.Bold)
                    } else {
                        Text(
                            text       = "Seleccionar archivo JSON",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 14.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Sección 2: Exportar palabras del sistema ──────────────────────
            SectionCard(
                emoji    = "📤",
                title    = "Exportar palabras del sistema",
                subtitle = "Descarga todas las palabras guardadas"
            ) {
                Text(
                    text     = "Genera un archivo JSON con todas las palabras actualmente en el sistema (${uiState.totalWords} palabras). Solo se exportan la palabra y sus significados, sin datos de progreso.",
                    fontSize = 13.sp,
                    color    = TextSecondary,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick  = {
                        scope.launch {
                            val jsonStr = viewModel.getExportJson()
                            saveJsonToDownloads(context, jsonStr, "tagenglish_words.json")
                            feedbackMsg = Message.Success("✅ Archivo guardado en Descargas como tagenglish_words.json")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    enabled  = !uiState.isProcessing && uiState.totalWords > 0,
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = AccentBlue,
                        contentColor           = Color.White,
                        disabledContainerColor = CardBorder,
                        disabledContentColor   = TextMuted
                    )
                ) {
                    Text(
                        text       = "Descargar words.json",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Sección 3: JSON de ejemplo ────────────────────────────────────
            SectionCard(
                emoji    = "📄",
                title    = "Archivo de ejemplo",
                subtitle = "Descarga la plantilla para añadir palabras"
            ) {
                Text(
                    text     = "¿No sabes cómo estructurar tu JSON? Descarga este archivo de ejemplo con 3 palabras para usarlo como plantilla.",
                    fontSize = 13.sp,
                    color    = TextSecondary,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(14.dp))

                // Preview del formato
                JsonPreviewBox()

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick  = {
                        val sampleJson = viewModel.getSampleJson()
                        saveJsonToDownloads(context, sampleJson, "tagenglish_example.json")
                        feedbackMsg = Message.Success("✅ Archivo guardado en Descargas como tagenglish_example.json")
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = AccentPurple,
                        contentColor   = Color.White
                    )
                ) {
                    Text(
                        text       = "Descargar ejemplo JSON",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── StatChip ─────────────────────────────────────────────────────────────────

@Composable
private fun StatChip(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = BgCard),
        border   = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = value,
                fontSize   = 26.sp,
                fontWeight = FontWeight.Black,
                color      = color
            )
            Text(
                text      = label,
                fontSize  = 11.sp,
                color     = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── SectionCard ─────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    emoji: String,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    val arrowRotation by animateFloatAsState(
        targetValue   = if (expanded) 0f else -90f,
        animationSpec = tween(200),
        label         = "arrow"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = BgCard),
        border   = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Header clicable para colapsar
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji en círculo
                Box(
                    modifier         = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BgCardAlt),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 18.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = title,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary
                    )
                    Text(
                        text     = subtitle,
                        fontSize = 12.sp,
                        color    = TextSecondary
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector        = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Colapsar" else "Expandir",
                        tint               = TextSecondary,
                        modifier           = Modifier.rotate(arrowRotation)
                    )
                }
            }

            // Contenido colapsable
            AnimatedVisibility(
                visible = expanded,
                enter   = fadeIn(tween(150)) + expandVertically(),
                exit    = fadeOut(tween(150)) + shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(CardBorder)
                    )
                    Spacer(Modifier.height(16.dp))
                    content()
                }
            }
        }
    }
}

// ─── FeedbackBanner ───────────────────────────────────────────────────────────

@Composable
private fun FeedbackBanner(message: Message, onDismiss: () -> Unit) {
    val isSuccess = message is Message.Success
    val text      = when (message) {
        is Message.Success -> message.text
        is Message.Error   -> message.text
    }
    val bgColor     = if (isSuccess) Color(0xFF141A16) else Color(0xFF1A1010)
    val borderColor = if (isSuccess) Color(0xFF1A3024) else Color(0xFF3A1A1A)
    val iconColor   = if (isSuccess) Learned else ErrorRed

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint               = iconColor,
            modifier           = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text       = text,
            fontSize   = 13.sp,
            color      = TextPrimary,
            modifier   = Modifier.weight(1f),
            lineHeight = 18.sp
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick  = onDismiss,
            modifier = Modifier.size(24.dp)
        ) {
            Text("✕", fontSize = 12.sp, color = TextSecondary)
        }
    }

    Spacer(Modifier.height(8.dp))
}

// ─── JsonPreviewBox ───────────────────────────────────────────────────────────

@Composable
private fun JsonPreviewBox() {
    val preview = """
[
  {
    "word": "challenge",
    "usages": [
      {
        "meaning": "desafío",
        "example": "Learning a language
                   is a great challenge."
      },
      {
        "meaning": "retar",
        "example": "I challenge you to
                   read one book a week."
      }
    ]
  }
]""".trimIndent()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0D0D0F))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text          = "FORMATO REQUERIDO",
                    fontSize      = 9.sp,
                    color         = TextMuted,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    DotColor(Color(0xFFFF5F56))
                    DotColor(Color(0xFFFFBD2E))
                    DotColor(Color(0xFF27C93F))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text       = preview,
                fontSize   = 11.sp,
                color      = Color(0xFFABD5FF),
                fontFamily = FontFamily.Monospace,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun DotColor(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}

// ─── Guardar JSON en Descargas ────────────────────────────────────────────────

private fun saveJsonToDownloads(context: Context, content: String, fileName: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val values   = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            resolver.openOutputStream(it)?.use { out ->
                out.write(content.toByteArray())
            }
        }
    } else {
        val dir  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = java.io.File(dir, fileName)
        file.writeText(content)
    }
}
