package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FlowContentRepository
import com.example.model.BufferOverflowType
import com.example.model.CollectorState
import com.example.model.FlowKind
import com.example.model.LogCategory
import com.example.ui.theme.FlowAmber
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.FlowEmerald
import com.example.ui.theme.FlowIndigo
import com.example.ui.theme.FlowPink
import com.example.ui.theme.FlowPurple
import com.example.ui.theme.FlowRose
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.viewmodel.SimulatorUiState
import com.example.viewmodel.SimulatorViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimulatorScreen(
  viewModel: SimulatorViewModel,
  modifier: Modifier = Modifier
) {
  val state by viewModel.uiState.collectAsState()
  var showPresets by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header & Reset
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Flow Laboratory",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Real-time Coroutines & Buffer Simulator",
            style = MaterialTheme.typography.bodySmall,
            color = FlowCyan
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          FilledTonalButton(
            onClick = { showPresets = !showPresets },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.testTag("toggle_presets_button")
          ) {
            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp), tint = FlowAmber)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Presets", fontSize = 12.sp)
          }

          IconButton(
            onClick = { viewModel.resetAll() },
            modifier = Modifier.size(36.dp).testTag("reset_all_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset simulator", tint = Color(0xFF94A3B8))
          }
        }
      }
    }

    // Presets Drawer / Accordion
    if (showPresets) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Interview Scenario Presets", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            FlowContentRepository.presets.forEach { preset ->
              Surface(
                color = if (state.activePreset?.id == preset.id) Color(0xFF1E293B) else Color(0xFF0F172A),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(
                  1.dp,
                  if (state.activePreset?.id == preset.id) FlowCyan else Color(0xFF334155)
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { viewModel.applyPreset(preset) }
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(preset.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    Text(
                      preset.tag,
                      fontSize = 9.sp,
                      fontFamily = JetBrainsMonoFontFamily,
                      color = FlowAmber,
                      modifier = Modifier.background(Color(0xFF3B2900), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                  }
                  Text(preset.subtitle, fontSize = 11.sp, color = FlowCyan, modifier = Modifier.padding(vertical = 2.dp))
                  Text(preset.explanation, fontSize = 10.sp, color = Color(0xFF94A3B8), lineHeight = 14.sp)
                }
              }
            }
          }
        }
      }
    }

    // Flow Type Selector
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF0F172A))
          .padding(4.dp)
      ) {
        val isShared = state.flowKind == FlowKind.SHARED_FLOW
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isShared) FlowIndigo else Color.Transparent)
            .clickable { viewModel.setFlowKind(FlowKind.SHARED_FLOW) }
            .padding(vertical = 10.dp)
            .testTag("tab_shared_flow"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            "SharedFlow (Events)",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (isShared) Color.White else Color(0xFF94A3B8)
          )
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (!isShared) FlowCyan else Color.Transparent)
            .clickable { viewModel.setFlowKind(FlowKind.STATE_FLOW) }
            .padding(vertical = 10.dp)
            .testTag("tab_state_flow"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            "StateFlow (UI State)",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (!isShared) Color(0xFF0F172A) else Color(0xFF94A3B8)
          )
        }
      }
    }

    // Parameters Configuration (Replay, ExtraBuffer, Overflow)
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.Tune, contentDescription = null, tint = FlowCyan, modifier = Modifier.size(18.dp))
              Text("Configurations", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            if (state.flowKind == FlowKind.SHARED_FLOW) {
              Text(
                "Total Buffer: ${state.replay + state.extraBufferCapacity} slots",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                color = FlowCyan
              )
            } else {
              Text(
                "replay = 1 (Fixed)",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                color = FlowCyan
              )
            }
          }

          if (state.flowKind == FlowKind.SHARED_FLOW) {
            // Replay tuning
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("replay (${state.replay}):", fontSize = 12.sp)
              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(0, 1, 2, 3).forEach { r ->
                  ConfigChip(text = "$r", isSelected = state.replay == r) { viewModel.setReplay(r) }
                }
              }
            }

            // Extra buffer tuning
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("extraBufferCapacity (${state.extraBufferCapacity}):", fontSize = 12.sp)
              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(0, 1, 2, 4).forEach { b ->
                  ConfigChip(text = "$b", isSelected = state.extraBufferCapacity == b) { viewModel.setExtraBuffer(b) }
                }
              }
            }

            // Buffer Overflow tuning
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("onBufferOverflow:", fontSize = 12.sp)
              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                BufferOverflowType.values().forEach { ov ->
                  ConfigChip(
                    text = ov.name.replace("DROP_", ""),
                    isSelected = state.overflowType == ov
                  ) { viewModel.setOverflow(ov) }
                }
              }
            }
          } else {
            // StateFlow notice
            Text(
              "StateFlow always has replay=1, onBufferOverflow=DROP_OLDEST, and requires an initial value. It applies distinctUntilChanged().",
              fontSize = 11.sp,
              color = Color(0xFFCBD5E1),
              lineHeight = 15.sp
            )
          }
        }
      }
    }

    // Buffer Tank Visualizer Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("BUFFER STATUS", fontFamily = JetBrainsMonoFontFamily, fontSize = 10.sp, color = Color(0xFF64748B))
            if (state.flowKind == FlowKind.STATE_FLOW) {
              Text("Current value: '${state.stateFlowValue}'", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = FlowCyan)
            } else {
              Text("Queued: ${state.bufferedItems.size} items", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = FlowCyan)
            }
          }

          // Buffer items slots
          val maxSlots = if (state.flowKind == FlowKind.STATE_FLOW) 1 else (state.replay + state.extraBufferCapacity)
          if (maxSlots == 0) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                .padding(8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text("Zero buffer: Direct handover (Rendezvous)", fontSize = 11.sp, color = FlowAmber)
            }
          } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              repeat(maxSlots) { idx ->
                val itemInSlot = state.bufferedItems.getOrNull(idx)
                val isReplaySlot = idx < (if (state.flowKind == FlowKind.STATE_FLOW) 1 else state.replay)
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (itemInSlot != null) (if (isReplaySlot) Color(0xFF0369A1) else Color(0xFF4338CA)) else Color(0xFF1E293B))
                    .border(1.dp, if (isReplaySlot) FlowCyan else FlowIndigo, RoundedCornerShape(6.dp))
                    .padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                      text = itemInSlot ?: "Empty",
                      fontWeight = FontWeight.Bold,
                      fontSize = 11.sp,
                      color = if (itemInSlot != null) Color.White else Color(0xFF64748B)
                    )
                    Text(
                      text = if (isReplaySlot) "Replay" else "Extra",
                      fontSize = 8.sp,
                      color = if (isReplaySlot) FlowCyan else FlowIndigo
                    )
                  }
                }
              }
            }
          }

          // Last tryEmit feedback pill
          state.lastTryEmitResult?.let { success ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(if (success) Color(0xFF064E3B) else Color(0xFF881337))
                .padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = if (success) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (success) FlowEmerald else FlowRose,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = if (success) "tryEmit() returned TRUE (Value buffered/delivered)"
                else "tryEmit() returned FALSE! Buffer full + SUSPEND cannot suspend!",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }
    }

    // Emitter Controls
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Emitter Actions (Payload: #Item ${state.nextPayloadId})", fontWeight = FontWeight.Bold, fontSize = 13.sp)

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = { viewModel.emitSuspending() },
              modifier = Modifier.weight(1f).testTag("button_emit_suspend"),
              colors = ButtonDefaults.buttonColors(containerColor = FlowIndigo)
            ) {
              Text("emit()", fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = { viewModel.tryEmitValue() },
              modifier = Modifier.weight(1f).testTag("button_try_emit"),
              colors = ButtonDefaults.buttonColors(containerColor = FlowCyan, contentColor = Color(0xFF0F172A))
            ) {
              Text("tryEmit()", fontWeight = FontWeight.Bold)
            }
          }

          if (state.flowKind == FlowKind.STATE_FLOW) {
            OutlinedButton(
              onClick = { viewModel.emitDuplicateStateFlow() },
              modifier = Modifier.fillMaxWidth().testTag("button_duplicate_state")
            ) {
              Text("Emit Duplicate Value (Test distinctUntilChanged)", fontSize = 12.sp)
            }
          }
        }
      }
    }

    // Collectors Section
    item {
      Text("Subscribers / Collectors (3 Collectors)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }

    items(state.collectors) { collector ->
      CollectorCard(
        collector = collector,
        onToggle = { viewModel.toggleCollector(collector.id) }
      )
    }

    // Simulation Logs Console
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Simulation Log Stream", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        IconButton(
          onClick = { viewModel.clearLogs() },
          modifier = Modifier.size(32.dp).testTag("clear_logs_button")
        ) {
          Icon(Icons.Default.Delete, contentDescription = "Clear logs", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
        }
      }
    }

    if (state.logs.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF090D16))
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Text("No logs yet. Click emit() or tryEmit() above to test!", fontSize = 12.sp, color = Color(0xFF64748B))
        }
      }
    } else {
      items(state.logs) { logItem ->
        LogItemRow(logItem = logItem)
      }
    }
  }
}

@Composable
private fun ConfigChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(if (isSelected) FlowCyan else Color(0xFF1E293B))
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 4.dp)
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
private fun CollectorCard(
  collector: CollectorState,
  onToggle: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = if (collector.isSubscribed) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF111827)
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(if (collector.isSubscribed) FlowEmerald else Color(0xFF64748B))
          )
          Column {
            Text(collector.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
              if (collector.isSubscribed) "Active subscriber" else "Disconnected (Click switch to join)",
              fontSize = 10.sp,
              color = if (collector.isSubscribed) FlowCyan else Color(0xFF94A3B8)
            )
          }
        }

        Switch(
          checked = collector.isSubscribed,
          onCheckedChange = { onToggle() },
          colors = SwitchDefaults.colors(checkedThumbColor = FlowCyan, checkedTrackColor = Color(0xFF0369A1)),
          modifier = Modifier.testTag("switch_${collector.id}")
        )
      }

      if (collector.isSubscribed) {
        Text("Received stream (recent):", fontSize = 10.sp, color = Color(0xFF94A3B8))
        if (collector.receivedItems.isEmpty()) {
          Text("Waiting for emissions...", fontSize = 11.sp, fontFamily = JetBrainsMonoFontFamily, color = Color(0xFF64748B))
        } else {
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            collector.receivedItems.forEach { item ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFF1E293B))
                  .border(1.dp, FlowIndigo, RoundedCornerShape(6.dp))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(item, fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = FlowCyan)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun LogItemRow(logItem: com.example.model.SimulationLogItem) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(6.dp))
      .background(Color(0xFF090D16))
      .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      text = logItem.time,
      fontFamily = JetBrainsMonoFontFamily,
      fontSize = 9.sp,
      color = Color(0xFF64748B)
    )

    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(4.dp))
        .background(
          when (logItem.category) {
            LogCategory.EMISSION -> Color(0xFF0369A1)
            LogCategory.COLLECTION -> Color(0xFF065F46)
            LogCategory.BUFFER -> Color(0xFF4338CA)
            LogCategory.SUSPENDED -> Color(0xFF78350F)
            LogCategory.DROPPED, LogCategory.WARNING -> Color(0xFF881337)
          }
        )
        .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
      Text(
        text = logItem.category.name,
        fontFamily = JetBrainsMonoFontFamily,
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    }

    Text(
      text = logItem.text,
      fontFamily = JetBrainsMonoFontFamily,
      fontSize = 11.sp,
      color = Color(0xFFE2E8F0),
      modifier = Modifier.weight(1f)
    )
  }
}
