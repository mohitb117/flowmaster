package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CodeSnippetCard
import com.example.ui.theme.FlowAmber
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.FlowEmerald
import com.example.ui.theme.FlowIndigo
import com.example.ui.theme.FlowRose
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.viewmodel.FlashcardQuizViewModel

@Composable
fun QuizScreen(
  viewModel: FlashcardQuizViewModel,
  modifier: Modifier = Modifier
) {
  val state by viewModel.uiState.collectAsState()

  LazyColumn(
    modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Interview Drill Quiz",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Test your Flow mastery under pressure",
            style = MaterialTheme.typography.bodyMedium,
            color = FlowCyan
          )
        }

        if (state.isQuizCompleted || state.selectedAnswers.isNotEmpty()) {
          FilledTonalButton(
            onClick = { viewModel.resetQuiz() },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.testTag("reset_quiz_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Restart", fontSize = 12.sp)
          }
        }
      }
    }

    if (state.isQuizCompleted) {
      // Results Card
      item {
        val total = state.quizQuestions.size
        val score = state.quizScore
        val percentage = (score.toFloat() / total * 100).toInt()

        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          border = BorderStroke(1.5.dp, if (percentage >= 70) FlowEmerald else FlowAmber),
          shape = RoundedCornerShape(20.dp)
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.EmojiEvents,
              contentDescription = null,
              tint = if (percentage >= 70) FlowAmber else FlowCyan,
              modifier = Modifier.size(54.dp)
            )

            Text(
              text = if (percentage >= 80) "Interview Ready! 🚀" else if (percentage >= 60) "Good Progress! 👍" else "Brush Up Needed 📚",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold
            )

            Text(
              text = "$score / $total Correct ($percentage%)",
              fontFamily = JetBrainsMonoFontFamily,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = if (percentage >= 70) FlowEmerald else FlowRose
            )

            Text(
              text = if (percentage >= 80)
                "Outstanding! You understand the tricky details of StateFlow, SharedFlow, buffers, and Android lifecycle collection."
              else
                "Review the Sound-Alikes and Interactive Diagrams tab to cement the exact differences between tryEmit, whileSubscribed, and buffer overflow rules.",
              textAlign = TextAlign.Center,
              fontSize = 13.sp,
              color = Color(0xFFCBD5E1),
              lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
              onClick = { viewModel.resetQuiz() },
              colors = ButtonDefaults.buttonColors(containerColor = FlowCyan, contentColor = Color(0xFF0F172A)),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Take Quiz Again", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    } else {
      val questionIndex = state.currentQuizIndex
      val question = state.quizQuestions.getOrNull(questionIndex)
      val selectedOption = state.selectedAnswers[questionIndex]
      val hasAnswered = selectedOption != null

      if (question != null) {
        // Progress header
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "QUESTION ${questionIndex + 1} OF ${state.quizQuestions.size}",
              fontFamily = JetBrainsMonoFontFamily,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = FlowCyan
            )
            Text(
              text = "Answered: ${state.selectedAnswers.size} / ${state.quizQuestions.size}",
              fontFamily = JetBrainsMonoFontFamily,
              fontSize = 11.sp,
              color = Color(0xFF94A3B8)
            )
          }
        }

        // Question Statement
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 22.sp
              )

              question.codeSnippet?.let { snippet ->
                CodeSnippetCard(code = snippet)
              }
            }
          }
        }

        // Multiple choice options
        item {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            question.options.forEachIndexed { optIdx, optText ->
              val isChosen = selectedOption == optIdx
              val isCorrect = optIdx == question.correctIndex

              val (bg, border, textColor) = when {
                !hasAnswered -> {
                  if (isChosen) Triple(Color(0xFF1E293B), FlowCyan, Color.White)
                  else Triple(Color(0xFF0F172A), Color(0xFF334155), Color(0xFFE2E8F0))
                }
                isCorrect -> Triple(Color(0xFF064E3B), FlowEmerald, Color.White)
                isChosen && !isCorrect -> Triple(Color(0xFF881337), FlowRose, Color.White)
                else -> Triple(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF64748B))
              }

              Surface(
                color = bg,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, border),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable(enabled = !hasAnswered) { viewModel.answerQuiz(questionIndex, optIdx) }
                  .testTag("quiz_option_$optIdx")
              ) {
                Row(
                  modifier = Modifier.padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(24.dp)
                      .clip(CircleShape)
                      .background(if (hasAnswered && isCorrect) FlowEmerald else if (hasAnswered && isChosen) FlowRose else Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                  ) {
                    if (hasAnswered && isCorrect) {
                      Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else if (hasAnswered && isChosen) {
                      Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                      Text(
                        "${('A' + optIdx)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                      )
                    }
                  }

                  Text(
                    text = optText,
                    fontSize = 13.sp,
                    color = textColor,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f)
                  )
                }
              }
            }
          }
        }

        // Explanation (Shown after answering)
        if (hasAnswered) {
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
              border = BorderStroke(1.dp, if (selectedOption == question.correctIndex) FlowEmerald else FlowRose),
              shape = RoundedCornerShape(14.dp)
            ) {
              Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = if (selectedOption == question.correctIndex) FlowEmerald else FlowRose,
                    modifier = Modifier.size(18.dp)
                  )
                  Text(
                    if (selectedOption == question.correctIndex) "CORRECT EXPLANATION" else "INCORRECT — HERE'S WHY",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (selectedOption == question.correctIndex) FlowEmerald else FlowRose
                  )
                }

                Text(
                  text = question.explanation,
                  fontSize = 12.sp,
                  color = Color.White,
                  lineHeight = 17.sp
                )

                Surface(
                  color = Color(0xFF1E293B),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("💡", fontSize = 12.sp)
                    Text(
                      text = "Interview Tip: ${question.interviewTip}",
                      fontSize = 11.sp,
                      color = FlowCyan,
                      lineHeight = 15.sp
                    )
                  }
                }
              }
            }
          }
        }

        // Navigation buttons
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedButton(
              onClick = { viewModel.previousQuizQuestion() },
              enabled = questionIndex > 0,
              modifier = Modifier.testTag("quiz_prev_button")
            ) {
              Text("Previous")
            }

            Button(
              onClick = { viewModel.nextQuizQuestion() },
              enabled = hasAnswered,
              colors = ButtonDefaults.buttonColors(containerColor = FlowCyan, contentColor = Color(0xFF0F172A)),
              modifier = Modifier.testTag("quiz_next_button")
            ) {
              Text(
                if (questionIndex + 1 == state.quizQuestions.size) "See Results" else "Next Question",
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}
