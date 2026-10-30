package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BufferOverflowType
import com.example.ui.theme.FlowAmber
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.FlowEmerald
import com.example.ui.theme.FlowIndigo
import com.example.ui.theme.FlowPink
import com.example.ui.theme.FlowPurple
import com.example.ui.theme.FlowRose
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun ColdVsHotDiagram(modifier: Modifier = Modifier) {
  var isHotMode by remember { mutableStateOf(false) }

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Cold Flow vs Hot Flow",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = if (isHotMode) "Hot: Broadcaster (SharedFlow / StateFlow)" else "Cold: On-Demand Stream (`flow { }`)",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isHotMode) FlowPurple else FlowCyan
          )
        }

        // Toggle Switch
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0F172A))
            .padding(4.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(if (!isHotMode) FlowCyan else Color.Transparent)
              .clickable { isHotMode = false }
              .padding(horizontal = 12.dp, vertical = 6.dp)
              .testTag("tab_cold_flow")
          ) {
            Text(
              "Cold",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = if (!isHotMode) Color(0xFF0F172A) else Color(0xFF94A3B8)
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(if (isHotMode) FlowPurple else Color.Transparent)
              .clickable { isHotMode = true }
              .padding(horizontal = 12.dp, vertical = 6.dp)
              .testTag("tab_hot_flow")
          ) {
            Text(
              "Hot",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = if (isHotMode) Color.White else Color(0xFF94A3B8)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Diagram Canvas Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF090D16))
          .padding(14.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          // Upstream Producer Node
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              color = if (isHotMode) Color(0xFF3B0764) else Color(0xFF082F49),
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, if (isHotMode) FlowPurple else FlowCyan)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = null,
                  tint = if (isHotMode) FlowPurple else FlowCyan,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = if (isHotMode) "Hot Producer (Active 24/7)" else "Cold Producer (Idle)",
                  fontFamily = JetBrainsMonoFontFamily,
                  fontSize = 11.sp,
                  color = Color.White
                )
              }
            }

            Text(
              text = if (isHotMode) "Broadcasting 🟢" else "Waiting for collect() ⏸️",
              fontFamily = JetBrainsMonoFontFamily,
              fontSize = 10.sp,
              color = if (isHotMode) FlowEmerald else FlowAmber
            )
          }

          // Visual Stream Lines
          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
          ) {
            val startY = 6f
            val endY = size.height - 6f
            val midX = size.width / 2

            if (isHotMode) {
              // Hot: Single pipe splitting into 2 collectors (Multicast)
              drawLine(
                color = Color(0xFFA855F7),
                start = Offset(midX, startY),
                end = Offset(midX, size.height / 2),
                strokeWidth = 4f
              )
              // Branch left to Collector 1
              drawLine(
                color = Color(0xFFA855F7),
                start = Offset(midX, size.height / 2),
                end = Offset(size.width * 0.25f, endY),
                strokeWidth = 4f
              )
              // Branch right to Collector 2
              drawLine(
                color = Color(0xFFA855F7),
                start = Offset(midX, size.height / 2),
                end = Offset(size.width * 0.75f, endY),
                strokeWidth = 4f
              )
            } else {
              // Cold: Independent separate pipelines (Unicast)
              drawLine(
                color = Color(0xFF38BDF8),
                start = Offset(size.width * 0.25f, startY),
                end = Offset(size.width * 0.25f, endY),
                strokeWidth = 4f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
              )
              drawLine(
                color = Color(0xFF38BDF8),
                start = Offset(size.width * 0.75f, startY),
                end = Offset(size.width * 0.75f, endY),
                strokeWidth = 4f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
              )
            }
          }

          // Downstream Collectors
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            CollectorBox(name = "Collector #1", isHot = isHotMode)
            CollectorBox(name = "Collector #2", isHot = isHotMode)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Key Takeaway box
      Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          Text(
            text = if (isHotMode) "🔑 Hot Flow Characteristics:" else "🔑 Cold Flow Characteristics:",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (isHotMode) FlowPurple else FlowCyan
          )
          Text(
            text = if (isHotMode)
              "• Multicast: Shared among all collectors simultaneously.\n• Independent lifecycle: Emits even with 0 collectors.\n• State retention: Can buffer or hold latest value (replay)."
            else
              "• Unicast: Every call to collect() restarts the producer block independently from scratch.\n• Lazy: Does NO work until a collector arrives.\n• Closes when producer block finishes.",
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = Color(0xFFCBD5E1),
            modifier = Modifier.padding(top = 4.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun CollectorBox(name: String, isHot: Boolean) {
  Surface(
    color = Color(0xFF1E293B),
    shape = RoundedCornerShape(8.dp),
    border = BorderStroke(1.dp, Color(0xFF334155))
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(name, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color.White)
      Text(
        if (isHot) "Shares same stream" else "Separate execution",
        fontSize = 9.sp,
        fontFamily = JetBrainsMonoFontFamily,
        color = if (isHot) FlowIndigo else FlowCyan
      )
    }
  }
}

@Composable
fun SharedFlowAnatomyDiagram(modifier: Modifier = Modifier) {
  var replayCount by remember { mutableIntStateOf(1) }
  var extraBuffer by remember { mutableIntStateOf(2) }
  var selectedOverflow by remember { mutableStateOf(BufferOverflowType.SUSPEND) }

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = "SharedFlow Buffer Anatomy",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Total Buffer = replay ($replayCount) + extraBufferCapacity ($extraBuffer) = ${replayCount + extraBuffer} slots",
        style = MaterialTheme.typography.bodyMedium,
        color = FlowCyan
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Interactive Buffer Tank Graphic
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF090D16))
          .padding(14.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "BUFFER TANK LAYOUT",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B)
          )

          // Slots row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            // Replay slots
            repeat(replayCount) { idx ->
              SlotBox(
                label = "Replay #${idx + 1}",
                sub = "Cached for new collectors",
                color = FlowCyan,
                modifier = Modifier.weight(1f)
              )
            }

            // Extra buffer slots
            repeat(extraBuffer) { idx ->
              SlotBox(
                label = "Extra #${idx + 1}",
                sub = "Active consumer cushion",
                color = FlowIndigo,
                modifier = Modifier.weight(1f)
              )
            }

            if (replayCount + extraBuffer == 0) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                  .padding(12.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  "Zero Buffer (Direct Rendezvous) — Emitter must suspend if collector busy!",
                  fontSize = 11.sp,
                  color = FlowRose,
                  fontFamily = JetBrainsMonoFontFamily
                )
              }
            }
          }

          // Overflow behavior alert
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(
                when (selectedOverflow) {
                  BufferOverflowType.SUSPEND -> Color(0xFF1E293B)
                  BufferOverflowType.DROP_OLDEST -> Color(0xFF3B1E08)
                  BufferOverflowType.DROP_LATEST -> Color(0xFF3B0818)
                }
              )
              .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = when (selectedOverflow) {
                BufferOverflowType.SUSPEND -> FlowCyan
                BufferOverflowType.DROP_OLDEST -> FlowAmber
                BufferOverflowType.DROP_LATEST -> FlowRose
              },
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = when (selectedOverflow) {
                BufferOverflowType.SUSPEND -> "When full: SUSPEND pauses the emitter until space is freed."
                BufferOverflowType.DROP_OLDEST -> "When full: DROP_OLDEST kicks out the oldest item in buffer."
                BufferOverflowType.DROP_LATEST -> "When full: DROP_LATEST rejects the newest incoming item."
              },
              fontSize = 11.sp,
              fontFamily = JetBrainsMonoFontFamily,
              color = Color.White
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Interactive controls for buffer configuration
      Text("Tune Parameters:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Replay ($replayCount):", fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          listOf(0, 1, 2).forEach { num ->
            ChipButton(text = "$num", isSelected = replayCount == num) { replayCount = num }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Extra Buffer ($extraBuffer):", fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          listOf(0, 1, 2, 4).forEach { num ->
            ChipButton(text = "$num", isSelected = extraBuffer == num) { extraBuffer = num }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Overflow:", fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          BufferOverflowType.values().forEach { ov ->
            ChipButton(text = ov.name.replace("DROP_", ""), isSelected = selectedOverflow == ov) {
              selectedOverflow = ov
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SlotBox(label: String, sub: String, color: Color, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0xFF1E293B))
      .border(1.dp, color, RoundedCornerShape(8.dp))
      .padding(8.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = color)
      Text(sub, fontSize = 8.sp, textAlign = TextAlign.Center, color = Color(0xFF94A3B8), lineHeight = 10.sp)
    }
  }
}

@Composable
private fun ChipButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(if (isSelected) FlowCyan else Color(0xFF1E293B))
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 4.dp)
  ) {
    Text(
      text = text,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = if (isSelected) Color(0xFF0F172A) else Color.White
    )
  }
}

@Composable
fun WhileSubscribedTimelineDiagram(modifier: Modifier = Modifier) {
  var currentStep by remember { mutableIntStateOf(1) }

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(imageVector = Icons.Default.ScreenRotation, contentDescription = null, tint = FlowCyan)
        Column {
          Text(
            text = "SharingStarted.WhileSubscribed(5000)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "The 5-second Android rotation lifecycle safety net",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Interactive timeline steps
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        TimelineStepButton(step = 1, title = "1. Active UI", isActive = currentStep == 1) { currentStep = 1 }
        TimelineStepButton(step = 2, title = "2. Rotation / Stop", isActive = currentStep == 2) { currentStep = 2 }
        TimelineStepButton(step = 3, title = "3a. Return (<5s)", isActive = currentStep == 3) { currentStep = 3 }
        TimelineStepButton(step = 4, title = "3b. Timeout (>5s)", isActive = currentStep == 4) { currentStep = 4 }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Step Detail Card
      Surface(
        color = Color(0xFF090D16),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          when (currentStep) {
            1 -> {
              Text("STEP 1: Activity is Visible (State = STARTED)", fontWeight = FontWeight.Bold, color = FlowEmerald, fontSize = 12.sp)
              Spacer(modifier = Modifier.height(6.dp))
              Text("• Subscriber count = 1 (Activity collecting via collectAsStateWithLifecycle)\n• Upstream flow is RUNNING and emitting real-time data.\n• Battery and network are active.", fontSize = 11.sp, color = Color(0xFFCBD5E1), lineHeight = 16.sp)
            }
            2 -> {
              Text("STEP 2: User Rotates Device (Configuration Change)", fontWeight = FontWeight.Bold, color = FlowAmber, fontSize = 12.sp)
              Spacer(modifier = Modifier.height(6.dp))
              Text("• Activity is destroyed and recreated (takes ~150-300ms).\n• Subscriber count momentarily drops to ZERO.\n• ⏰ WhileSubscribed starts 5000ms countdown timer.\n• Upstream flow STAYS ALIVE — no wasted network reconnection!", fontSize = 11.sp, color = Color(0xFFCBD5E1), lineHeight = 16.sp)
            }
            3 -> {
              Text("SCENARIO 3A: Activity Reconnects Within 5 Seconds", fontWeight = FontWeight.Bold, color = FlowCyan, fontSize = 12.sp)
              Spacer(modifier = Modifier.height(6.dp))
              Text("• New Activity instance calls collect() in 200ms.\n• Subscriber count goes from 0 back to 1.\n• 5000ms timer is CANCELLED.\n• Zero flicker, zero redundant network queries!", fontSize = 11.sp, color = Color(0xFFCBD5E1), lineHeight = 16.sp)
            }
            4 -> {
              Text("SCENARIO 3B: User Went to Home Screen (>5 Seconds)", fontWeight = FontWeight.Bold, color = FlowRose, fontSize = 12.sp)
              Spacer(modifier = Modifier.height(6.dp))
              Text("• 5000ms expires without any subscriber reconnecting.\n• WhileSubscribed automatically CANCELS upstream cold flow.\n• Location sensors/WebSocket disconnect safely.\n• Saves user battery and background data!", fontSize = 11.sp, color = Color(0xFFCBD5E1), lineHeight = 16.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TimelineStepButton(step: Int, title: String, isActive: Boolean, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isActive) FlowCyan else Color(0xFF1E293B))
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Text(
      text = title,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      color = if (isActive) Color(0xFF0F172A) else Color(0xFFCBD5E1)
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowDecisionTreeDiagram(modifier: Modifier = Modifier) {
  var selectedChoice by remember { mutableStateOf("state") }

  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = "Which Flow Should I Use?",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Interactive Decision Matrix for Android",
        style = MaterialTheme.typography.bodySmall,
        color = Color(0xFF94A3B8)
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Flow Question Chips
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        DecisionChip(text = "📱 Screen UI State", isSelected = selectedChoice == "state") { selectedChoice = "state" }
        DecisionChip(text = "⚡ One-Off Events", isSelected = selectedChoice == "events") { selectedChoice = "events" }
        DecisionChip(text = "📡 Sensor / Network Stream", isSelected = selectedChoice == "stream") { selectedChoice = "stream" }
        DecisionChip(text = "⚙️ Work Queue (1 worker)", isSelected = selectedChoice == "channel") { selectedChoice = "channel" }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Surface(
        color = Color(0xFF090D16),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          when (selectedChoice) {
            "state" -> {
              DecisionResult(
                winner = "StateFlow<UiState>",
                winnerColor = FlowCyan,
                reason = "Holds the current screen state (loading, content, error). Requires an initial value, replays exactly 1 item to new/rotated screens, and eliminates unnecessary re-renders via distinctUntilChanged.",
                code = "val uiState: StateFlow<UiState> = _uiState.asStateFlow()"
              )
            }
            "events" -> {
              DecisionResult(
                winner = "SharedFlow<UiEvent>",
                winnerColor = FlowPurple,
                reason = "For navigation, toasts, and dialog triggers that should only be handled ONCE and NOT re-triggered when rotating the screen. Use replay=0, extraBufferCapacity=1, and DROP_OLDEST.",
                code = "val event = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)"
              )
            }
            "stream" -> {
              DecisionResult(
                winner = "Cold Flow (or callbackFlow)",
                winnerColor = FlowEmerald,
                reason = "For continuous streams produced on-demand (e.g., GPS updates, database queries, network polling). It stops running as soon as the subscriber cancels.",
                code = "fun observeLocation(): Flow<Location> = callbackFlow { ... awaitClose { } }"
              )
            }
            "channel" -> {
              DecisionResult(
                winner = "Channel<Task>",
                winnerColor = FlowAmber,
                reason = "When each task should be processed by exactly ONE consumer, not broadcast to everyone (like a queue of background jobs).",
                code = "val taskChannel = Channel<Task>(Channel.BUFFERED)"
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DecisionChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) FlowIndigo else Color(0xFF1E293B))
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Text(
      text = text,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
      color = if (isSelected) Color.White else Color(0xFFCBD5E1)
    )
  }
}

@Composable
private fun DecisionResult(winner: String, winnerColor: Color, reason: String, code: String) {
  Column {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = winnerColor, modifier = Modifier.size(18.dp))
      Text("Recommended: $winner", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = winnerColor)
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(reason, fontSize = 12.sp, color = Color(0xFFE2E8F0), lineHeight = 16.sp)
    Spacer(modifier = Modifier.height(8.dp))
    CodeSnippetCard(code = code, title = "Recommended Configuration")
  }
}
