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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ChartRange
import com.example.model.WatchScreenDestination
import com.example.ui.components.WatchCurvedPositionIndicator
import com.example.ui.components.WatchTopStatusHeader
import com.example.ui.screens.DualIndexWatchScreen
import com.example.ui.screens.SingleIndexDetailWatchScreen
import com.example.ui.screens.WatchSettingsScreen
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MarketBearish
import com.example.ui.theme.MarketBullish
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NasdaqIndigo
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.Sp500Cyan
import com.example.ui.theme.WatchOledBlack
import com.example.ui.theme.WatchSurfaceCard
import com.example.ui.theme.WatchSurfaceDark
import com.example.ui.theme.WatchSurfaceElevated
import com.example.ui.theme.WatchTextMuted
import com.example.ui.theme.WatchTextPrimary
import com.example.ui.theme.WatchTextSecondary
import com.example.viewmodel.MarketWatchUiState
import com.example.viewmodel.MarketWatchViewModel
import kotlin.math.abs

class MainActivity : ComponentActivity() {

  private val viewModel: MarketWatchViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        PulseIndexWatchApp(
          uiState = uiState,
          viewModel = viewModel
        )
      }
    }
  }
}

@Composable
fun PulseIndexWatchApp(
  uiState: MarketWatchUiState,
  viewModel: MarketWatchViewModel
) {
  BackHandler(enabled = uiState.currentDestination != WatchScreenDestination.DUAL_HOME) {
    viewModel.navigateTo(WatchScreenDestination.DUAL_HOME)
  }

  val configuration = LocalConfiguration.current
  val isRoundHardware = configuration.isScreenRound

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = if (isRoundHardware || !uiState.showWatchBezelInPreview) {
      WatchOledBlack
    } else {
      Color(0xFF05080F)
    }
  ) {
    BoxWithConstraints(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      val containerWidth = maxWidth
      val containerHeight = maxHeight
      val minSide = min(containerWidth, containerHeight)
      val isNativeWatchSize = isRoundHardware || minSide < 290.dp

      if (isNativeWatchSize || !uiState.showWatchBezelInPreview) {
        // Direct edge-to-edge circular Wear OS rendering on Pixel Watch hardware (or full-bleed mode)
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(WatchOledBlack),
          contentAlignment = Alignment.Center
        ) {
          PixelWatchCircularFace(
            uiState = uiState,
            viewModel = viewModel,
            modifier = Modifier.size(minSide)
          )

          if (!isNativeWatchSize) {
            // Subtle floating button to return to Pixel Watch Bezel frame when testing on phone emulator
            Box(
              modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(bottom = 12.dp)
                .clip(CircleShape)
                .background(WatchSurfaceElevated)
                .border(1.dp, Color(0xFF2B3A55), CircleShape)
                .clickable { viewModel.toggleWatchBezelInPreview() }
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("restore_watch_bezel_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Watch,
                  contentDescription = "Show Pixel Watch Hardware Frame",
                  tint = Sp500Cyan,
                  modifier = Modifier.size(15.dp)
                )
                Text(
                  text = "Show Pixel Watch Bezel",
                  fontFamily = OutfitFontFamily,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 12.sp,
                  color = WatchTextPrimary
                )
              }
            }
          }
        }
      } else {
        // Streaming Emulator Companion View: Renders the true circular Pixel Watch 3 hardware bezel + rotary crown
        PixelWatchInteractiveShowcase(
          uiState = uiState,
          viewModel = viewModel,
          availableWidth = containerWidth,
          availableHeight = containerHeight
        )
      }
    }
  }
}

@Composable
private fun PixelWatchInteractiveShowcase(
  uiState: MarketWatchUiState,
  viewModel: MarketWatchViewModel,
  availableWidth: Dp,
  availableHeight: Dp
) {
  val watchOuterSize = min(availableWidth - 44.dp, availableHeight * 0.56f).coerceIn(230.dp, 320.dp)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .padding(horizontal = 16.dp, vertical = 10.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Top Header Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 520.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Sp500Cyan.copy(alpha = 0.14f))
            .border(1.dp, Sp500Cyan.copy(alpha = 0.45f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Watch,
            contentDescription = "Pixel Watch App",
            tint = Sp500Cyan,
            modifier = Modifier.size(17.dp)
          )
        }
        Column {
          Text(
            text = "PulseIndex • Pixel Watch",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = WatchTextPrimary
          )
          Text(
            text = "Wear OS Standalone • S&P 500 & Nasdaq 100",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            color = WatchTextSecondary
          )
        }
      }

      // Full-bleed vs Watch Bezel toggle
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(WatchSurfaceCard)
          .border(1.dp, Color(0xFF253248), RoundedCornerShape(12.dp))
          .clickable { viewModel.toggleWatchBezelInPreview() }
          .padding(horizontal = 10.dp, vertical = 6.dp)
          .testTag("toggle_bezel_mode_button")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Fullscreen,
            contentDescription = "Full Screen Watch Face",
            tint = Sp500Cyan,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "Full Face",
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = WatchTextPrimary
          )
        }
      }
    }

    // Center: Pixel Watch 3 Circular Case + Interactive Haptic Crown
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.padding(vertical = 4.dp)
    ) {
      // Top & Bottom Watch Band Lugs
      Box(
        modifier = Modifier
          .width(watchOuterSize * 0.46f)
          .height(watchOuterSize + 34.dp)
          .clip(RoundedCornerShape(28.dp))
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color(0xFF161D29),
                Color(0xFF0D121B),
                Color(0xFF161D29)
              )
            )
          )
          .border(1.dp, Color(0xFF253042), RoundedCornerShape(28.dp))
      )

      // Right Side Tactile Rotating Crown (Tap or Drag Vertically to Scroll Watch Screens)
      var crownDragAccumulator by remember { mutableFloatStateOf(0f) }
      Box(
        modifier = Modifier
          .offset(x = (watchOuterSize / 2) + 6.dp, y = (-6).dp)
          .width(16.dp)
          .height(44.dp)
          .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp, topStart = 3.dp, bottomStart = 3.dp))
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color(0xFF475569),
                Color(0xFF1E293B),
                Color(0xFF64748B),
                Color(0xFF0F172A)
              )
            )
          )
          .border(
            1.dp,
            Sp500Cyan.copy(alpha = 0.5f),
            RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp, topStart = 3.dp, bottomStart = 3.dp)
          )
          .clickable { viewModel.cycleDestinationForward() }
          .pointerInput(Unit) {
            detectVerticalDragGestures { change, dragAmount ->
              change.consume()
              crownDragAccumulator += dragAmount
              if (abs(crownDragAccumulator) > 28f) {
                if (crownDragAccumulator > 0) {
                  viewModel.cycleDestinationForward()
                } else {
                  viewModel.cycleDestinationBackward()
                }
                crownDragAccumulator = 0f
              }
            }
          }
          .testTag("pixel_watch_crown_button"),
        contentAlignment = Alignment.Center
      ) {
        // Crown Ridges
        Column(
          verticalArrangement = Arrangement.spacedBy(3.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          repeat(6) {
            Box(
              modifier = Modifier
                .width(8.dp)
                .height(1.5.dp)
                .background(Color(0xFF94A3B8))
            )
          }
        }
      }

      // Secondary Side Button above Crown (Returns to Dual Home)
      Box(
        modifier = Modifier
          .offset(x = (watchOuterSize / 2) + 2.dp, y = (-42).dp)
          .width(8.dp)
          .height(22.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(Color(0xFF334155))
          .clickable { viewModel.navigateTo(WatchScreenDestination.DUAL_HOME) }
          .testTag("pixel_watch_side_button")
      )

      // Domed Glass Pixel Watch Circular Bezel
      Box(
        modifier = Modifier
          .size(watchOuterSize)
          .shadow(elevation = 20.dp, shape = CircleShape, spotColor = Sp500Cyan)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              colors = listOf(
                Color(0xFF1E293B),
                Color(0xFF0F172A),
                Color(0xFF05080E)
              )
            )
          )
          .border(
            width = 3.dp,
            brush = Brush.linearGradient(
              colors = listOf(
                Color(0xFF475569),
                Color(0xFF1E293B),
                Color(0xFF334155)
              )
            ),
            shape = CircleShape
          )
          .padding(10.dp),
        contentAlignment = Alignment.Center
      ) {
        PixelWatchCircularFace(
          uiState = uiState,
          viewModel = viewModel,
          modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
        )
      }
    }

    // Bottom Quick Hardware & Screen Controls Strip
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 520.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(WatchSurfaceDark)
        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(18.dp))
        .padding(horizontal = 12.dp, vertical = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Screen Selector Tabs (Dual Watch / S&P 500 Dial / Nasdaq 100 Dial / Config)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        val tabs = listOf(
          Triple(WatchScreenDestination.DUAL_HOME, "Dual Watch", "nav_dual_home"),
          Triple(WatchScreenDestination.SP500_DETAIL, "S&P 500", "nav_sp500_detail"),
          Triple(WatchScreenDestination.NASDAQ100_DETAIL, "Nasdaq 100", "nav_nasdaq100_detail"),
          Triple(WatchScreenDestination.WATCH_SETTINGS, "Config", "nav_watch_settings")
        )
        tabs.forEach { (dest, label, tag) ->
          val selected = uiState.currentDestination == dest
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (selected) Sp500Cyan.copy(alpha = 0.2f) else WatchSurfaceCard)
              .border(
                1.dp,
                if (selected) Sp500Cyan else Color(0xFF243146),
                RoundedCornerShape(10.dp)
              )
              .clickable { viewModel.navigateTo(dest) }
              .padding(vertical = 7.dp)
              .testTag(tag),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = label,
              fontFamily = OutfitFontFamily,
              fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
              fontSize = 11.sp,
              color = if (selected) WatchTextPrimary else WatchTextSecondary,
              maxLines = 1
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Live Data Source + Hardware Crown Hint + Quick Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        val sourceLabel = uiState.sp500Data?.sourceName ?: "Connecting..."
        val isLive = uiState.sp500Data?.isLiveNetwork == true
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(if (isLive) MarketBullish else Color(0xFFFBBF24))
          )
          Text(
            text = "$sourceLabel • Crown scrolls dials",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            color = WatchTextSecondary
          )
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(WatchSurfaceElevated)
              .clickable { viewModel.toggleDisplayMode() }
              .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = uiState.displayMode.shortBadge,
              fontFamily = JetBrainsMonoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              color = Sp500Cyan
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (uiState.isAmbientMode) Sp500Cyan.copy(alpha = 0.25f) else WatchSurfaceElevated)
              .clickable { viewModel.toggleAmbientMode() }
              .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "AOD",
              fontFamily = JetBrainsMonoFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              color = if (uiState.isAmbientMode) Sp500Cyan else WatchTextSecondary
            )
          }
        }
      }
    }
  }
}

@Composable
fun PixelWatchCircularFace(
  uiState: MarketWatchUiState,
  viewModel: MarketWatchViewModel,
  modifier: Modifier = Modifier
) {
  val focusRequester = remember { FocusRequester() }
  var rotaryAccumulator by remember { mutableFloatStateOf(0f) }

  LaunchedEffect(Unit) {
    try {
      focusRequester.requestFocus()
    } catch (_: Exception) {
    }
  }

  val activePageIndex = when (uiState.currentDestination) {
    WatchScreenDestination.DUAL_HOME -> 0
    WatchScreenDestination.SP500_DETAIL -> 1
    WatchScreenDestination.NASDAQ100_DETAIL -> 2
    WatchScreenDestination.WATCH_SETTINGS -> 3
  }

  Box(
    modifier = modifier
      .aspectRatio(1f)
      .clip(CircleShape)
      .background(WatchOledBlack)
      .onRotaryScrollEvent { event ->
        rotaryAccumulator += event.verticalScrollPixels
        if (abs(rotaryAccumulator) > 36f) {
          if (rotaryAccumulator > 0) {
            viewModel.cycleDestinationForward()
          } else {
            viewModel.cycleDestinationBackward()
          }
          rotaryAccumulator = 0f
        }
        true
      }
      .focusRequester(focusRequester)
      .focusable()
      .testTag("pixel_watch_circular_face"),
    contentAlignment = Alignment.Center
  ) {
    // Main Watch Dial Content
    AnimatedContent(
      targetState = uiState.currentDestination,
      transitionSpec = { fadeIn() togetherWith fadeOut() },
      label = "watch_dial_transition"
    ) { destination ->
      when (destination) {
        WatchScreenDestination.DUAL_HOME -> DualIndexWatchScreen(
          uiState = uiState,
          onOpenSp500Detail = { viewModel.navigateTo(WatchScreenDestination.SP500_DETAIL) },
          onOpenNasdaq100Detail = { viewModel.navigateTo(WatchScreenDestination.NASDAQ100_DETAIL) },
          onRefresh = { viewModel.refreshMarketData() },
          onToggleDisplayMode = { viewModel.toggleDisplayMode() },
          onOpenSettings = { viewModel.navigateTo(WatchScreenDestination.WATCH_SETTINGS) }
        )

        WatchScreenDestination.SP500_DETAIL -> SingleIndexDetailWatchScreen(
          marketData = uiState.sp500Data,
          displayMode = uiState.displayMode,
          selectedRange = uiState.selectedRange,
          isAmbientMode = uiState.isAmbientMode,
          onSelectRange = { viewModel.selectChartRange(it) },
          onToggleDisplayMode = { viewModel.toggleDisplayMode() },
          onBackToDualHome = { viewModel.navigateTo(WatchScreenDestination.DUAL_HOME) }
        )

        WatchScreenDestination.NASDAQ100_DETAIL -> SingleIndexDetailWatchScreen(
          marketData = uiState.nasdaq100Data,
          displayMode = uiState.displayMode,
          selectedRange = uiState.selectedRange,
          isAmbientMode = uiState.isAmbientMode,
          onSelectRange = { viewModel.selectChartRange(it) },
          onToggleDisplayMode = { viewModel.toggleDisplayMode() },
          onBackToDualHome = { viewModel.navigateTo(WatchScreenDestination.DUAL_HOME) }
        )

        WatchScreenDestination.WATCH_SETTINGS -> WatchSettingsScreen(
          uiState = uiState,
          onSelectInterval = { viewModel.setAutoRefreshInterval(it) },
          onSetDisplayMode = { viewModel.setDisplayMode(it) },
          onToggleAmbientMode = { viewModel.toggleAmbientMode() },
          onBack = { viewModel.navigateTo(WatchScreenDestination.DUAL_HOME) }
        )
      }
    }

    // Top Curved Watch Status Header (Time + Market Session + Page Dots)
    WatchTopStatusHeader(
      watchTime = uiState.watchTimeFormatted,
      sessionState = uiState.sessionState,
      currentDestination = uiState.currentDestination,
      isAmbientMode = uiState.isAmbientMode,
      onSelectDestination = { viewModel.navigateTo(it) },
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 6.dp)
    )

    // Right-Edge Curved Rotary Scroll Indicator
    WatchCurvedPositionIndicator(
      activeIndex = activePageIndex,
      totalCount = 4,
      isAmbientMode = uiState.isAmbientMode
    )
  }
}
