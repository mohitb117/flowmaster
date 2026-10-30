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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.ComparisonRow
import com.example.model.SoundAlikeItem
import com.example.ui.components.CodeSnippetCard
import com.example.ui.theme.FlowAmber
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.FlowEmerald
import com.example.ui.theme.FlowIndigo
import com.example.ui.theme.FlowPurple
import com.example.ui.theme.FlowRose
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun SoundAlikesScreen(modifier: Modifier = Modifier) {
  var searchQuery by remember { mutableStateOf("") }
  var expandedIds by remember { mutableStateOf(setOf<String>("stateflow_vs_sharedflow", "statein_vs_sharein", "emit_vs_tryemit")) }

  val filteredItems = remember(searchQuery) {
    if (searchQuery.isBlank()) FlowContentRepository.soundAlikes
    else FlowContentRepository.soundAlikes.filter {
      it.termA.contains(searchQuery, ignoreCase = true) ||
        it.termB.contains(searchQuery, ignoreCase = true) ||
        it.mnemonic.contains(searchQuery, ignoreCase = true) ||
        it.quickRule.contains(searchQuery, ignoreCase = true)
    }
  }

  LazyColumn(
    modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "Sound-Alikes & Mnemonics",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Never mix up similar names in your interview",
          style = MaterialTheme.typography.bodyMedium,
          color = FlowCyan
        )
      }
    }

    // Search Box
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search (e.g. shareIn, tryEmit, collectLatest)...", fontSize = 13.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = FlowCyan) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("search_sound_alikes_input"),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = FlowCyan,
          unfocusedBorderColor = Color(0xFF334155),
          focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
      )
    }

    if (filteredItems.isEmpty()) {
      item {
        Box(
          modifier = Modifier.fillMaxWidth().padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Text("No matching terms found. Try 'StateFlow' or 'emit'.", color = Color(0xFF94A3B8))
        }
      }
    } else {
      items(filteredItems, key = { it.id }) { item ->
        val isExpanded = expandedIds.contains(item.id)
        SoundAlikeCard(
          item = item,
          isExpanded = isExpanded,
          onToggleExpand = {
            expandedIds = if (isExpanded) expandedIds - item.id else expandedIds + item.id
          }
        )
      }
    }
  }
}

@Composable
fun SoundAlikeCard(
  item: SoundAlikeItem,
  isExpanded: Boolean,
  onToggleExpand: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      // Header: Term A vs Term B
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onToggleExpand),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = item.termA,
              fontFamily = JetBrainsMonoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = FlowCyan
            )
            Text("vs", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = FlowAmber)
            Text(
              text = item.termB,
              fontFamily = JetBrainsMonoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = FlowPurple
            )
          }
          Text(
            text = item.quickRule,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFCBD5E1),
            modifier = Modifier.padding(top = 4.dp),
            maxLines = if (isExpanded) Int.MAX_VALUE else 2
          )
        }

        Icon(
          imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
          contentDescription = "Expand comparison",
          tint = FlowCyan,
          modifier = Modifier.size(24.dp)
        )
      }

      // 5-Second Memory Hook (Always visible)
      Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(10.dp),
          verticalAlignment = Alignment.Top,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = null,
            tint = FlowAmber,
            modifier = Modifier.size(18.dp)
          )
          Column {
            Text(
              "5-SECOND MEMORY TRICK",
              fontFamily = JetBrainsMonoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 9.sp,
              color = FlowAmber
            )
            Text(
              text = item.mnemonic,
              fontSize = 12.sp,
              color = Color.White,
              lineHeight = 16.sp,
              modifier = Modifier.padding(top = 2.dp)
            )
          }
        }
      }

      // Expandable section with comparison table, snippets, and interview gotcha
      AnimatedVisibility(visible = isExpanded) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          // Comparison Table
          Surface(
            color = Color(0xFF090D16),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              // Table Header
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                  .padding(horizontal = 8.dp, vertical = 6.dp)
              ) {
                Text("Aspect", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF94A3B8), modifier = Modifier.weight(1f))
                Text(item.termA, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = FlowCyan, modifier = Modifier.weight(1.3f))
                Text(item.termB, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = FlowPurple, modifier = Modifier.weight(1.3f))
              }

              // Table Rows
              item.comparisonRows.forEach { row ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(row.aspect, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFCBD5E1), modifier = Modifier.weight(1f))
                  Text(row.valA, fontSize = 10.sp, color = Color.White, modifier = Modifier.weight(1.3f))
                  Text(row.valB, fontSize = 10.sp, color = Color.White, modifier = Modifier.weight(1.3f))
                }
              }
            }
          }

          // Code Snippets Comparison
          Text("Code Comparison", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          CodeSnippetCard(code = item.snippetA, title = item.termA)
          CodeSnippetCard(code = item.snippetB, title = item.termB)

          // Interview Trap / Gotcha Box
          Surface(
            color = Color(0xFF3B0818),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, FlowRose),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.Top,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = FlowRose,
                modifier = Modifier.size(18.dp)
              )
              Column {
                Text(
                  "INTERVIEW GOTCHA TO WATCH OUT FOR",
                  fontFamily = JetBrainsMonoFontFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 9.sp,
                  color = FlowRose
                )
                Text(
                  text = item.interviewGotcha,
                  fontSize = 11.sp,
                  color = Color.White,
                  lineHeight = 15.sp,
                  modifier = Modifier.padding(top = 2.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
