package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.DiagramsScreen
import com.example.ui.screens.FlashcardsScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.SimulatorScreen
import com.example.ui.screens.SoundAlikesScreen
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.FlowIndigo
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FlashcardQuizViewModel
import com.example.viewmodel.SimulatorViewModel

enum class AppDestination(val title: String, val icon: ImageVector, val tag: String) {
  SIMULATOR("Simulator", Icons.Default.Science, "tab_simulator"),
  SOUND_ALIKES("Mnemonics", Icons.AutoMirrored.Filled.CompareArrows, "tab_sound_alikes"),
  DIAGRAMS("Diagrams", Icons.Default.Hub, "tab_diagrams"),
  FLASHCARDS("Flashcards", Icons.Default.CollectionsBookmark, "tab_flashcards"),
  QUIZ("Quiz", Icons.Default.Quiz, "tab_quiz")
}

class MainActivity : ComponentActivity() {

  private val simulatorViewModel: SimulatorViewModel by viewModels()
  private val flashcardQuizViewModel: FlashcardQuizViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        FlowMasterApp(
          simulatorViewModel = simulatorViewModel,
          flashcardQuizViewModel = flashcardQuizViewModel
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowMasterApp(
  simulatorViewModel: SimulatorViewModel,
  flashcardQuizViewModel: FlashcardQuizViewModel
) {
  var currentDestination by remember { mutableStateOf(AppDestination.SIMULATOR) }

  // BackHandler: Return to SIMULATOR tab if pressed from sub-tabs
  BackHandler(enabled = currentDestination != AppDestination.SIMULATOR) {
    currentDestination = AppDestination.SIMULATOR
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "FlowMaster",
            fontWeight = FontWeight.Bold,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 18.sp,
            color = FlowCyan
          )
        },
        actions = {
          Box(
            modifier = Modifier
              .background(Color(0xFF1E293B), shape = MaterialTheme.shapes.small)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "Android Interview Prep",
              fontFamily = JetBrainsMonoFontFamily,
              fontSize = 10.sp,
              color = Color(0xFF94A3B8)
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background,
          titleContentColor = FlowCyan
        )
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
      ) {
        AppDestination.values().forEach { destination ->
          val isSelected = currentDestination == destination
          NavigationBarItem(
            selected = isSelected,
            onClick = { currentDestination = destination },
            icon = {
              Icon(
                imageVector = destination.icon,
                contentDescription = destination.title,
                modifier = Modifier.size(20.dp)
              )
            },
            label = {
              Text(
                text = destination.title,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color(0xFF0F172A),
              selectedTextColor = FlowCyan,
              indicatorColor = FlowCyan,
              unselectedIconColor = Color(0xFF64748B),
              unselectedTextColor = Color(0xFF64748B)
            ),
            modifier = Modifier.testTag(destination.tag)
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      AnimatedContent(
        targetState = currentDestination,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
      ) { destination ->
        when (destination) {
          AppDestination.SIMULATOR -> SimulatorScreen(viewModel = simulatorViewModel)
          AppDestination.SOUND_ALIKES -> SoundAlikesScreen()
          AppDestination.DIAGRAMS -> DiagramsScreen()
          AppDestination.FLASHCARDS -> FlashcardsScreen(viewModel = flashcardQuizViewModel)
          AppDestination.QUIZ -> QuizScreen(viewModel = flashcardQuizViewModel)
        }
      }
    }
  }
}
