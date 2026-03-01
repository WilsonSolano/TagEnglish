package com.example.tagenglish.ui.screens.test

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tagenglish.domain.model.Question
import com.example.tagenglish.ui.viewmodels.TestViewModel

// ─── Paleta (misma que HomeScreen) ───────────────────────────────────────────

private val BgDark       = Color(0xFF0D0D0F)
private val BgCard       = Color(0xFF16161A)
private val AccentLime   = Color(0xFFCAFF4D)
private val AccentBlue   = Color(0xFF4D9EFF)
private val AccentPurple = Color(0xFFB66DFF)
private val TextPrimary  = Color(0xFFF5F5F5)
private val TextSecondary= Color(0xFF8A8A9A)
private val TextMuted    = Color(0xFF4A4A5A)
private val CardBorder   = Color(0xFF252530)

@Composable
fun WeeklyTestScreen(
    viewModel: TestViewModel,
    weekId: Int,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(weekId) { viewModel.loadQuestions(weekId) }

    Scaffold(containerColor = BgDark) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color    = AccentLime,
                        strokeWidth = 3.dp
                    )
                }

                uiState.isFinished && uiState.result != null -> {
                    TestResultScreen(result = uiState.result!!, onFinish = onFinish)
                }

                uiState.allAnswered -> {
                    SubmitScreen(
                        answeredCount = uiState.answers.size,
                        totalCount    = uiState.totalQuestions,
                        onSubmit      = viewModel::submitTest
                    )
                }

                uiState.questions.isNotEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                    ) {
                        Spacer(Modifier.height(16.dp))

                        // ── Top bar ───────────────────────────────────────────
                        Row(
                            modifier       = Modifier.fillMaxWidth(),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = onFinish) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = TextSecondary
                                )
                            }
                            Text(
                                text       = "Test · Semana $weekId",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary
                            )
                            // Badge progreso
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CardBorder)
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text  = "${uiState.currentIndex + 1}/${uiState.totalQuestions}",
                                    fontSize  = 13.sp,
                                    fontWeight= FontWeight.Bold,
                                    color     = AccentLime
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // ── Barra de progreso ─────────────────────────────────
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(CardBorder)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(uiState.progress)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(listOf(AccentLime, AccentBlue))
                                    )
                            )
                        }

                        Spacer(Modifier.height(28.dp))

                        // ── Pregunta con animación ────────────────────────────
                        AnimatedContent(
                            targetState = uiState.currentIndex,
                            transitionSpec = {
                                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                            },
                            label = "question_transition"
                        ) { index ->
                            val question = uiState.questions.getOrNull(index)
                            if (question != null) {
                                QuestionContent(
                                    question = question,
                                    index    = index,
                                    onAnswer = { answer ->
                                        viewModel.answerQuestion(index, answer)
                                        viewModel.nextQuestion()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── QuestionContent ──────────────────────────────────────────────────────────

@Composable
private fun QuestionContent(
    question: Question,
    index: Int,
    onAnswer: (String) -> Unit
) {
    val accentColor = when (index % 3) {
        0    -> AccentLime
        1    -> AccentBlue
        else -> AccentPurple
    }

    when (question) {
        is Question.MultipleChoiceMeaning -> MultipleChoiceQuestion(
            word        = question.word,
            prompt      = "¿Qué significa esta palabra?",
            options     = question.options,
            accentColor = accentColor,
            onAnswer    = onAnswer
        )
        is Question.ReverseTranslation -> MultipleChoiceQuestion(
            word        = question.meaning,
            prompt      = "¿Cómo se dice en inglés?",
            options     = question.options,
            accentColor = accentColor,
            onAnswer    = onAnswer
        )
        is Question.FillInBlank -> FillInBlankQuestion(
            example     = question.example,
            accentColor = accentColor,
            onAnswer    = onAnswer
        )
    }
}

// ─── MultipleChoiceQuestion ───────────────────────────────────────────────────

@Composable
private fun MultipleChoiceQuestion(
    word: String,
    prompt: String,
    options: List<String>,
    accentColor: Color,
    onAnswer: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Chip de tipo de pregunta
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(accentColor.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text      = "SELECCIÓN MÚLTIPLE",
                fontSize  = 10.sp,
                fontWeight= FontWeight.Bold,
                color     = accentColor,
                letterSpacing = 1.sp
            )
        }

        Text(
            text      = prompt,
            fontSize  = 14.sp,
            color     = TextSecondary
        )

        // Palabra principal
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = BgCard),
            border   = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
        ) {
            Box(
                modifier         = Modifier.fillMaxWidth().padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = word,
                    fontSize   = 36.sp,
                    fontWeight = FontWeight.Black,
                    color      = accentColor,
                    textAlign  = TextAlign.Center,
                    letterSpacing = (-1).sp
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Opciones
        options.forEachIndexed { i, option ->
            Button(
                onClick  = { onAnswer(option) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = BgCard,
                    contentColor   = TextPrimary
                ),
                border   = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier       = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Box(
                        modifier         = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text      = ('A' + i).toString(),
                            fontSize  = 11.sp,
                            fontWeight= FontWeight.Bold,
                            color     = accentColor
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    Text(
                        text      = option,
                        fontSize  = 15.sp,
                        fontWeight= FontWeight.Medium,
                        color     = TextPrimary
                    )
                }
            }
        }
    }
}

// ─── FillInBlankQuestion ──────────────────────────────────────────────────────

@Composable
private fun FillInBlankQuestion(
    example: String,
    accentColor: Color,
    onAnswer: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(accentColor.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text      = "COMPLETA LA ORACIÓN",
                fontSize  = 10.sp,
                fontWeight= FontWeight.Bold,
                color     = accentColor,
                letterSpacing = 1.sp
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = BgCard),
            border   = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
        ) {
            Text(
                text      = example,
                fontSize  = 20.sp,
                fontWeight= FontWeight.SemiBold,
                modifier  = Modifier.padding(24.dp),
                textAlign = TextAlign.Center,
                color     = TextPrimary,
                fontStyle = FontStyle.Italic
            )
        }

        OutlinedTextField(
            value         = input,
            onValueChange = { input = it },
            label         = { Text("Escribe la palabra", color = TextSecondary) },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(14.dp),
            singleLine    = true,
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = accentColor,
                unfocusedBorderColor = CardBorder,
                focusedTextColor     = TextPrimary,
                unfocusedTextColor   = TextPrimary,
                cursorColor          = accentColor,
                focusedLabelColor    = accentColor
            )
        )

        Button(
            onClick  = { if (input.isNotBlank()) onAnswer(input.trim()) },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            enabled  = input.isNotBlank(),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = accentColor,
                contentColor           = Color(0xFF0D0D0F),
                disabledContainerColor = CardBorder,
                disabledContentColor   = TextMuted
            )
        ) {
            Text("Confirmar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

// ─── SubmitScreen ─────────────────────────────────────────────────────────────

@Composable
private fun SubmitScreen(
    answeredCount: Int,
    totalCount: Int,
    onSubmit: () -> Unit
) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✅", fontSize = 72.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Text(
            text       = "¡Todas respondidas!",
            fontSize   = 26.sp,
            fontWeight = FontWeight.Black,
            color      = TextPrimary,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = "$answeredCount de $totalCount preguntas contestadas",
            fontSize  = 15.sp,
            color     = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(36.dp))
        Button(
            onClick  = onSubmit,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = AccentLime,
                contentColor   = Color(0xFF0D0D0F)
            )
        ) {
            Text("Ver resultado", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// ─── TestResultScreen ─────────────────────────────────────────────────────────

@Composable
private fun TestResultScreen(
    result: com.example.tagenglish.domain.model.TestResult,
    onFinish: () -> Unit
) {
    val passed = result.passed
    val accentColor = if (passed) AccentLime else Color(0xFFFF6B6B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Emoji grande
            Text(
                text      = if (passed) "🎉" else "📚",
                fontSize  = 80.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            // Porcentaje
            Text(
                text       = "${"%.0f".format(result.percentage)}%",
                fontSize   = 72.sp,
                fontWeight = FontWeight.Black,
                color      = accentColor,
                letterSpacing = (-3).sp
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text       = if (passed) "¡Aprobaste!" else "Sigue practicando",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text      = "${result.score} de ${result.totalQuestions} correctas",
                fontSize  = 15.sp,
                color     = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick  = onFinish,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor   = Color(0xFF0D0D0F)
                )
            ) {
                Text("Volver al inicio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}