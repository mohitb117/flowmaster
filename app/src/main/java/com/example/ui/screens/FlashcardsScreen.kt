package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FlashcardItem
import com.example.ui.theme.FlowAmber
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.FlowEmerald
import com.example.ui.theme.FlowIndigo
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.viewmodel.FlashcardQuizViewModel

@Composable
fun FlashcardsScreen(
  viewModel: FlashcardQuizViewModel,
  modifier: Modifier = Modifier
) {
  val state by viewModel.uiState.collectAsState()

  val filteredCards: List<FlashcardItem> = remember(state.selectedCategory, state.flashcards) {
    if (state.selectedCategory == "All") state.flashcards
    else state.flashcards.filter { it.category == state.selectedCategory }
  }

  val currentCard = filteredCards.getOrNull(state.currentCardIndex.coerceIn(0, (filteredCards.size - 1).coerceAtLeast(0)))
  val isFlipped = currentCard != null && state.flippedCardIds.contains(currentCard.id)
  val isMastered = currentCard != null && state.masteredIds.contains(currentCard.id)

  val rotation by animateFloatAsState(
    targetValue = if (isFlipped) 180f else 0f,
    animationSpec = tween(durationMillis = 400),
    label = "card_flip"
  )

  LazyColumn(
    modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "Interview Flashcards",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "High-yield interview recall drill",
          style = MaterialTheme.typography.bodyMedium,
          color = FlowCyan
        )
      }
    }

    // Category Tabs
    item {
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        viewModel.categories.forEach { category ->
          val isSelected = state.selectedCategory == category
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) FlowIndigo else Color(0xFF1E293B))
              .clickable { viewModel.selectCategory(category) }
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(
              text = category,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (isSelected) Color.White else Color(0xFF94A3B8)
            )
          }
        }
      }
    }

    // Progress Bar
    item {
      val masteredCount = filteredCards.count { state.masteredIds.contains(it.id) }
      val total = filteredCards.size
      val progressFraction = if (total > 0) masteredCount.toFloat() / total else 0f

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Mastery Progress", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(
              "$masteredCount / $total Mastered (${(progressFraction * 100).toInt()}%)",
              fontFamily = JetBrainsMonoFontFamily,
              fontSize = 11.sp,
              color = FlowEmerald
            )
          }
          LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = FlowEmerald,
            trackColor = Color(0xFF1E293B)
          )
        }
      }
    }

    // The Flipping Card
    if (currentCard != null) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
            .graphicsLayer {
              rotationY = rotation
              cameraDistance = 12f * density
            }
            .clickable { viewModel.toggleCardFlip(currentCard.id) }
            .testTag("interactive_flashcard")
        ) {
          if (rotation <= 90f) {
            // FRONT OF CARD
            CardFront(
              card = currentCard,
              currentIndex = state.currentCardIndex + 1,
              totalCards = filteredCards.size,
              isMastered = isMastered
            )
          } else {
            // BACK OF CARD (Flipped)
            Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
              CardBack(card = currentCard)
            }
          }
        }
      }

      // Card Controls: Previous, Mastered Toggle, Next
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { viewModel.previousCard(filteredCards.size) },
            modifier = Modifier.size(44.dp).testTag("flashcard_prev_button")
          ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Previous Card", tint = FlowCyan)
          }

          FilledTonalButton(
            onClick = { viewModel.toggleMastery(currentCard.id) },
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = if (isMastered) Color(0xFF065F46) else Color(0xFF1E293B)
            ),
            modifier = Modifier.testTag("flashcard_master_toggle")
          ) {
            Icon(
              imageVector = if (isMastered) Icons.Default.Star else Icons.Outlined.StarOutline,
              contentDescription = null,
              tint = if (isMastered) FlowAmber else Color(0xFF94A3B8),
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
              if (isMastered) "Mastered ⭐" else "Mark Mastered",
              fontSize = 12.sp,
              color = if (isMastered) Color.White else Color(0xFFCBD5E1)
            )
          }

          IconButton(
            onClick = { viewModel.nextCard(filteredCards.size) },
            modifier = Modifier.size(44.dp).testTag("flashcard_next_button")
          ) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Next Card", tint = FlowCyan)
          }
        }
      }
    }
  }
}

@Composable
private fun CardFront(
  card: FlashcardItem,
  currentIndex: Int,
  totalCards: Int,
  isMastered: Boolean
) {
  Card(
    modifier = Modifier.fillMaxSize(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    border = BorderStroke(1.5.dp, if (isMastered) FlowEmerald else FlowCyan),
    shape = RoundedCornerShape(20.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(20.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          card.category.uppercase(),
          fontFamily = JetBrainsMonoFontFamily,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = FlowCyan
        )
        Text(
          "$currentIndex of $totalCards",
          fontFamily = JetBrainsMonoFontFamily,
          fontSize = 11.sp,
          color = Color(0xFF94A3B8)
        )
      }

      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = card.question,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = Color.White,
          lineHeight = 28.sp
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.FlipCameraAndroid, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.size(6.dp))
        Text("Tap card to flip answer", fontSize = 11.sp, color = Color(0xFF64748B))
      }
    }
  }
}

@Composable
private fun CardBack(card: FlashcardItem) {
  Card(
    modifier = Modifier.fillMaxSize(),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
    border = BorderStroke(1.5.dp, FlowIndigo),
    shape = RoundedCornerShape(20.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(18.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          "IDEAL INTERVIEW ANSWER",
          fontFamily = JetBrainsMonoFontFamily,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          color = FlowEmerald
        )
        Icon(Icons.Default.FlipCameraAndroid, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
      }

      // Punchline
      Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Icon(Icons.Default.Lightbulb, contentDescription = null, tint = FlowAmber, modifier = Modifier.size(16.dp))
          Text(card.punchline, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
        }
      }

      // Bullet points
      card.keyPoints.forEach { point ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("•", color = FlowCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          Text(point, fontSize = 11.sp, color = Color(0xFFCBD5E1), lineHeight = 16.sp)
        }
      }

      // Interviewer Impression Gotcha
      Surface(
        color = Color(0xFF1E1B4B),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, FlowIndigo),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          Text("💡 IMPRESS THE INTERVIEWER:", fontFamily = JetBrainsMonoFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = FlowIndigo)
          Text(card.interviewerGotcha, fontSize = 11.sp, color = Color.White, lineHeight = 15.sp, modifier = Modifier.padding(top = 2.dp))
        }
      }
    }
  }
}
