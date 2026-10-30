package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.ColdVsHotDiagram
import com.example.ui.components.FlowDecisionTreeDiagram
import com.example.ui.components.SharedFlowAnatomyDiagram
import com.example.ui.components.WhileSubscribedTimelineDiagram
import com.example.ui.theme.FlowCyan

@Composable
fun DiagramsScreen(modifier: Modifier = Modifier) {
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "Visual Flow Architecture",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Interactive diagrams explaining Android Coroutines mechanics",
          style = MaterialTheme.typography.bodyMedium,
          color = FlowCyan
        )
      }
    }

    // 1. Cold vs Hot Flow Diagram
    item {
      ColdVsHotDiagram()
    }

    // 2. SharedFlow Buffer Anatomy
    item {
      SharedFlowAnatomyDiagram()
    }

    // 3. WhileSubscribed(5000) Timeline
    item {
      WhileSubscribedTimelineDiagram()
    }

    // 4. Decision Tree
    item {
      FlowDecisionTreeDiagram()
    }
  }
}
