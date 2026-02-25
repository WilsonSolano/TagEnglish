package com.example.tagenglish.ui.screens.test

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tagenglish.domain.model.Question
import com.example.tagenglish.ui.viewmodels.TestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyTestScreen(
    viewModel: TestViewModel,
    weekId: Int,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(weekId) {
        viewModel.loadQuestions(weekId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test Semana $weekId", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.isFinished && uiState.result != null -> {
                    TestResultScreen(
                        result   = uiState.result!!,
                        onFinish = onFinish
                    )
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
                            .padding(16.dp)
                    ) {
                        // Barra de progreso
                        LinearProgressIndicator(
                            progress  = { uiState.progress },
                            modifier  = Modifier.fillMaxWidth().height(8.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = "Pregunta ${uiState.currentIndex + 1} de ${uiState.totalQuestions}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))

                        // Pregunta actual con animación de slide
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
                                    question    = question,
                                    onAnswer    = { answer ->
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

// ─── QuestionContent - elige el tipo de pregunta ─────────────────────────────

@Composable
private fun QuestionContent(
    question: Question,
    onAnswer: (String) -> Unit
) {
    when (question) {
        is Question.MultipleChoiceMeaning -> MultipleChoiceQuestion(
            word    = question.word,
            prompt  = "¿Qué significa esta palabra?",
            options = question.options,
            onAnswer= onAnswer
        )
        is Question.ReverseTranslation -> MultipleChoiceQuestion(
            word    = question.meaning,
            prompt  = "¿Cómo se dice esto en inglés?",
            options = question.options,
            onAnswer= onAnswer
        )
        is Question.FillInBlank -> FillInBlankQuestion(
            example  = question.example,
            onAnswer = onAnswer
        )
    }
}

// ─── MultipleChoiceQuestion ───────────────────────────────────────────────────

@Composable
private fun MultipleChoiceQuestion(
    word: String,
    prompt: String,
    options: List<String>,
    onAnswer: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text       = prompt,
            style      = MaterialTheme.typography.bodyMedium,
            color      = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text       = word,
            fontSize   = 32.sp,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary,
            modifier   = Modifier.fillMaxWidth(),
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        options.forEach { option ->
            Button(
                onClick  = { onAnswer(option) },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor   = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(text = option, fontSize = 16.sp)
            }
        }
    }
}

// ─── FillInBlankQuestion ──────────────────────────────────────────────────────

@Composable
private fun FillInBlankQuestion(
    example: String,
    onAnswer: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text  = "Completa la oración:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text      = example,
                fontSize  = 18.sp,
                modifier  = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
        OutlinedTextField(
            value         = input,
            onValueChange = { input = it },
            label         = { Text("Tu respuesta") },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            singleLine    = true
        )
        Button(
            onClick  = { if (input.isNotBlank()) onAnswer(input.trim()) },
            modifier = Modifier.fillMaxWidth(),
            enabled  = input.isNotBlank(),
            shape    = RoundedCornerShape(12.dp)
        ) {
            Text("Confirmar")
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
        modifier             = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment  = Alignment.CenterHorizontally,
        verticalArrangement  = Arrangement.Center
    ) {
        Text("✅", fontSize = 64.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(
            text       = "Respondiste $answeredCount de $totalCount preguntas",
            style      = MaterialTheme.typography.titleMedium,
            textAlign  = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick  = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(12.dp)
        ) {
            Text("Ver resultado", fontSize = 16.sp)
        }
    }
}

// ─── TestResultScreen ─────────────────────────────────────────────────────────

@Composable
private fun TestResultScreen(
    result: com.example.tagenglish.domain.model.TestResult,
    onFinish: () -> Unit
) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val emoji = if (result.passed) "🎉" else "📚"
        Text(emoji, fontSize = 72.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(
            text       = if (result.passed) "¡Aprobaste!" else "Sigue practicando",
            fontSize   = 26.sp,
            fontWeight = FontWeight.Bold,
            color      = if (result.passed) MaterialTheme.colorScheme.primary
                         else MaterialTheme.colorScheme.error,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = "${result.score} / ${result.totalQuestions} correctas",
            style     = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text      = "${"%.0f".format(result.percentage)}%",
            fontSize  = 48.sp,
            fontWeight= FontWeight.Bold,
            textAlign = TextAlign.Center,
            color     = if (result.passed) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick  = onFinish,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(12.dp)
        ) {
            Text("Volver al inicio", fontSize = 16.sp)
        }
    }
}
