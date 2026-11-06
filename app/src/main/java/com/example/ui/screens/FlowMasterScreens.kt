package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FlowAmber
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.FlowDarkSurface
import com.example.ui.theme.FlowDarkSurfaceVariant
import com.example.ui.theme.FlowEmerald
import com.example.ui.theme.FlowIndigo
import com.example.ui.theme.FlowPurple
import com.example.ui.theme.FlowRose
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.WatchTextPrimary
import com.example.ui.theme.WatchTextSecondary

@Composable
fun FlowSimulatorScreen() {
  var selectedFlowType by remember { mutableStateOf("StateFlow") }
  var replayCount by remember { mutableIntStateOf(1) }
  var bufferCapacity by remember { mutableIntStateOf(2) }
  var emittedCounter by remember { mutableIntStateOf(3) }
  val emissionLog = remember {
    mutableStateListOf(
      "Emit #1 -> SPX 5,988.20 (Delivered)",
      "Emit #2 -> NDX 21,085.40 (Delivered)",
      "Emit #3 -> SPX 5,995.54 (Conflated Latest)"
    )
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Card(
      colors = CardDefaults.cardColors(containerColor = FlowDarkSurface),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "Hot Flow Ticker Stream Simulator",
          fontFamily = OutfitFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = FlowCyan
        )
        Text(
          text = "Simulate how StateFlow & SharedFlow emit real-time S&P 500 / Nasdaq 100 ticks to a Pixel Watch collector.",
          fontFamily = OutfitFontFamily,
          fontSize = 13.sp,
          color = WatchTextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("StateFlow", "SharedFlow").forEach { kind ->
            val selected = selectedFlowType == kind
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (selected) FlowCyan.copy(alpha = 0.2f) else FlowDarkSurfaceVariant)
                .border(1.dp, if (selected) FlowCyan else Color.Transparent, RoundedCornerShape(10.dp))
                .clickable {
                  selectedFlowType = kind
                  if (kind == "StateFlow") replayCount = 1
                }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = kind,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (selected) FlowCyan else WatchTextSecondary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "replay = $replayCount | extraBufferCapacity = $bufferCapacity",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 12.sp,
            color = FlowIndigo
          )
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
              onClick = {
                emittedCounter++
                val ticker = if (emittedCounter % 2 == 0) "SPX" else "NDX"
                val behavior = if (selectedFlowType == "StateFlow") "State Updated" else "Buffered (replay=$replayCount)"
                emissionLog.add(0, "Emit #$emittedCounter -> $ticker Tick ($behavior)")
              },
              colors = ButtonDefaults.buttonColors(containerColor = FlowCyan),
              modifier = Modifier.testTag("emit_flow_button")
            ) {
              Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "Emit",
                tint = Color(0xFF090D16),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Emit Tick", color = Color(0xFF090D16), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    }

    Card(
      colors = CardDefaults.cardColors(containerColor = FlowDarkSurface),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "Collector Buffer Log",
          fontFamily = JetBrainsMonoFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = FlowEmerald
        )
        emissionLog.take(6).forEach { entry ->
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(FlowDarkSurfaceVariant)
              .padding(horizontal = 10.dp, vertical = 8.dp)
          ) {
            Text(
              text = entry,
              fontFamily = JetBrainsMonoFontFamily,
              fontSize = 11.5.sp,
              color = WatchTextPrimary
            )
          }
        }
      }
    }
  }
}

@Composable
fun FlowGuidesAndQuizScreen() {
  val flashcards = remember {
    listOf(
      Triple(
        "StateFlow vs SharedFlow",
        "StateFlow requires an initial value, conflates equal values (distinctUntilChanged), and always has replay = 1. SharedFlow has configurable replay & buffer overflow.",
        "stateIn(scope, SharingStarted.WhileSubscribed(5000), initialState)"
      ),
      Triple(
        "SharingStarted.WhileSubscribed(5000)",
        "Keeps the upstream S&P 500 / Nasdaq 100 market polling flow alive for 5 seconds after the last UI collector disappears (surviving configuration changes).",
        "SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000)"
      ),
      Triple(
        "collectAsStateWithLifecycle()",
        "Recommended Jetpack Compose API on Phone & Wear OS that automatically stops collecting when the watch screen enters background or ambient state.",
        "val uiState by viewModel.uiState.collectAsStateWithLifecycle()"
      )
    )
  }

  var activeCardIndex by remember { mutableIntStateOf(0) }
  var showAnswer by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text(
      text = "Kotlin Flows Architecture & Flashcards",
      fontFamily = OutfitFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 18.sp,
      color = FlowCyan
    )

    flashcards.forEachIndexed { index, (title, explanation, snippet) ->
      val expanded = activeCardIndex == index
      Card(
        colors = CardDefaults.cardColors(containerColor = FlowDarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            activeCardIndex = index
            showAnswer = !showAnswer
          }
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = title,
              fontFamily = JetBrainsMonoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = if (expanded) FlowCyan else WatchTextPrimary
            )
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Concept",
              tint = if (expanded) FlowEmerald else WatchTextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = explanation,
            fontFamily = OutfitFontFamily,
            fontSize = 13.sp,
            color = WatchTextSecondary
          )
          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(FlowDarkSurfaceVariant)
              .padding(10.dp)
          ) {
            Text(
              text = snippet,
              fontFamily = JetBrainsMonoFontFamily,
              fontSize = 11.sp,
              color = FlowIndigo
            )
          }
        }
      }
    }
  }
}
