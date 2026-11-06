package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AutoRefreshInterval
import com.example.model.ChartRange
import com.example.model.IndexMarketData
import com.example.model.MarketIndexId
import com.example.model.QuoteDisplayMode
import com.example.model.WatchScreenDestination
import com.example.ui.components.DayRangeGaugeBar
import com.example.ui.components.SparklineCanvas
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MarketBearish
import com.example.ui.theme.MarketBearishBg
import com.example.ui.theme.MarketBullish
import com.example.ui.theme.MarketBullishBg
import com.example.ui.theme.NasdaqIndigo
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.Sp500Cyan
import com.example.ui.theme.WatchOledBlack
import com.example.ui.theme.WatchSurfaceCard
import com.example.ui.theme.WatchSurfaceElevated
import com.example.ui.theme.WatchTextMuted
import com.example.ui.theme.WatchTextPrimary
import com.example.ui.theme.WatchTextSecondary
import com.example.viewmodel.MarketWatchUiState
import java.util.Locale

@Composable
fun DualIndexWatchScreen(
  uiState: MarketWatchUiState,
  onOpenSp500Detail: () -> Unit,
  onOpenNasdaq100Detail: () -> Unit,
  onRefresh: () -> Unit,
  onToggleDisplayMode: () -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isAmbient = uiState.isAmbientMode
  val showDollar = uiState.displayMode == QuoteDisplayMode.ETF_PRICE

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Spacer(modifier = Modifier.height(34.dp))

    // Card 1: S&P 500
    uiState.sp500Data?.let { sp500 ->
      WatchIndexCompactTile(
        marketData = sp500,
        displayMode = uiState.displayMode,
        accentColor = Sp500Cyan,
        showDollarSign = showDollar,
        isAmbientMode = isAmbient,
        testTag = "watch_card_sp500",
        onClick = onOpenSp500Detail,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Card 2: Nasdaq 100
    uiState.nasdaq100Data?.let { nasdaq ->
      WatchIndexCompactTile(
        marketData = nasdaq,
        displayMode = uiState.displayMode,
        accentColor = NasdaqIndigo,
        showDollarSign = showDollar,
        isAmbientMode = isAmbient,
        testTag = "watch_card_nasdaq100",
        onClick = onOpenNasdaq100Detail,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Bottom Curved Watch Action Dock
    WatchBottomActionDock(
      isRefreshing = uiState.isRefreshing,
      displayMode = uiState.displayMode,
      lastUpdatedFormatted = uiState.lastUpdatedFormatted,
      secondsUntilNext = uiState.secondsUntilNextRefresh,
      autoRefreshEnabled = uiState.autoRefreshInterval != AutoRefreshInterval.MANUAL,
      isAmbientMode = isAmbient,
      onRefresh = onRefresh,
      onToggleDisplayMode = onToggleDisplayMode,
      onOpenSettings = onOpenSettings
    )

    Spacer(modifier = Modifier.height(10.dp))
  }
}

@Composable
private fun WatchIndexCompactTile(
  marketData: IndexMarketData,
  displayMode: QuoteDisplayMode,
  accentColor: Color,
  showDollarSign: Boolean,
  isAmbientMode: Boolean,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val quote = marketData.activeQuote(displayMode)
  val secondaryQuote = marketData.secondaryQuote(displayMode)
  val isBull = quote.isPositive
  val deltaColor = when {
    isAmbientMode -> WatchTextSecondary
    isBull -> MarketBullish
    else -> MarketBearish
  }
  val deltaBg = when {
    isAmbientMode -> Color.Transparent
    isBull -> MarketBullishBg
    else -> MarketBearishBg
  }

  val cardBg = if (isAmbientMode) WatchOledBlack else WatchSurfaceCard
  val borderColor = if (isAmbientMode) Color(0xFF26334D) else accentColor.copy(alpha = 0.28f)

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .background(cardBg)
      .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(20.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 7.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Left Column: Index Title, Ticker Pill, Price, and Point Change
    Column(
      modifier = Modifier.weight(1.25f),
      verticalArrangement = Arrangement.Center
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(if (isAmbientMode) WatchTextSecondary else accentColor)
        )
        Text(
          text = marketData.id.shortTitle,
          fontFamily = OutfitFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          color = if (isAmbientMode) WatchTextSecondary else accentColor,
          maxLines = 1
        )
        Text(
          text = quote.symbol,
          fontFamily = JetBrainsMonoFontFamily,
          fontWeight = FontWeight.Medium,
          fontSize = 9.sp,
          color = WatchTextMuted
        )
      }

      Spacer(modifier = Modifier.height(1.dp))

      Text(
        text = quote.formattedPrice(showDollarSign = showDollarSign),
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = WatchTextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Clip
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        Text(
          text = quote.formattedChange(),
          fontFamily = JetBrainsMonoFontFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 10.sp,
          color = deltaColor
        )
        Text(
          text = "• ${secondaryQuote.symbol} ${
            String.format(
              Locale.US,
              if (displayMode == QuoteDisplayMode.INDEX_POINTS) "$%.1f" else "%,.0f",
              secondaryQuote.price
            )
          }",
          fontFamily = JetBrainsMonoFontFamily,
          fontSize = 8.5.sp,
          color = WatchTextMuted,
          maxLines = 1
        )
      }
    }

    Spacer(modifier = Modifier.width(6.dp))

    // Right Column: Mini Sparkline + Percent Pill
    Column(
      modifier = Modifier.weight(0.85f),
      horizontalAlignment = Alignment.End,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(10.dp))
          .background(deltaBg)
          .border(
            width = if (isAmbientMode) 1.dp else 0.dp,
            color = if (isAmbientMode) WatchTextMuted else Color.Transparent,
            shape = RoundedCornerShape(10.dp)
          )
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          Icon(
            imageVector = if (isBull) {
              Icons.AutoMirrored.Filled.TrendingUp
            } else {
              Icons.AutoMirrored.Filled.TrendingDown
            },
            contentDescription = if (isBull) "Up" else "Down",
            tint = deltaColor,
            modifier = Modifier.size(10.dp)
          )
          Text(
            text = quote.formattedPercent(),
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 9.5.sp,
            color = deltaColor
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      SparklineCanvas(
        points = quote.sparkline,
        previousClose = quote.previousClose,
        isPositive = isBull,
        isAmbientMode = isAmbientMode,
        modifier = Modifier
          .fillMaxWidth()
          .height(24.dp)
      )
    }
  }
}

@Composable
private fun WatchBottomActionDock(
  isRefreshing: Boolean,
  displayMode: QuoteDisplayMode,
  lastUpdatedFormatted: String,
  secondsUntilNext: Int,
  autoRefreshEnabled: Boolean,
  isAmbientMode: Boolean,
  onRefresh: () -> Unit,
  onToggleDisplayMode: () -> Unit,
  onOpenSettings: () -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 900, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "refresh_angle"
  )

  Column(
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Toggle SPX/NDX vs SPY/QQQ pill
      Box(
        modifier = Modifier
          .height(28.dp)
          .clip(CircleShape)
          .background(if (isAmbientMode) WatchOledBlack else WatchSurfaceElevated)
          .border(1.dp, Color(0xFF2B3A55), CircleShape)
          .clickable(onClick = onToggleDisplayMode)
          .padding(horizontal = 10.dp)
          .testTag("toggle_display_mode_button"),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          Icon(
            imageVector = Icons.Default.SwapHoriz,
            contentDescription = "Switch Index or ETF",
            tint = Sp500Cyan,
            modifier = Modifier.size(12.dp)
          )
          Text(
            text = displayMode.shortBadge,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = WatchTextPrimary
          )
        }
      }

      // Refresh Circular Action Button
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(if (isAmbientMode) WatchOledBlack else Sp500Cyan.copy(alpha = 0.18f))
          .border(1.dp, Sp500Cyan.copy(alpha = 0.6f), CircleShape)
          .clickable(onClick = onRefresh)
          .testTag("watch_refresh_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Refresh S&P 500 and Nasdaq 100",
          tint = Sp500Cyan,
          modifier = Modifier
            .size(14.dp)
            .rotate(if (isRefreshing) rotation else 0f)
        )
      }

      // Settings Circular Button
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(if (isAmbientMode) WatchOledBlack else WatchSurfaceElevated)
          .border(1.dp, Color(0xFF2B3A55), CircleShape)
          .clickable(onClick = onOpenSettings)
          .testTag("watch_settings_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Tune,
          contentDescription = "Watch Settings",
          tint = WatchTextSecondary,
          modifier = Modifier.size(13.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(2.dp))

    Text(
      text = if (isRefreshing) {
        "Fetching live quotes..."
      } else if (autoRefreshEnabled) {
        "Upd $lastUpdatedFormatted • ${secondsUntilNext}s"
      } else {
        "Upd $lastUpdatedFormatted"
      },
      fontFamily = JetBrainsMonoFontFamily,
      fontSize = 8.sp,
      color = WatchTextMuted,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
fun SingleIndexDetailWatchScreen(
  marketData: IndexMarketData?,
  displayMode: QuoteDisplayMode,
  selectedRange: ChartRange,
  isAmbientMode: Boolean,
  onSelectRange: (ChartRange) -> Unit,
  onToggleDisplayMode: () -> Unit,
  onBackToDualHome: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (marketData == null) return

  val quote = marketData.activeQuote(displayMode)
  val secondaryQuote = marketData.secondaryQuote(displayMode)
  val accentColor = if (marketData.id == MarketIndexId.SP500) Sp500Cyan else NasdaqIndigo
  val isBull = quote.isPositive
  val deltaColor = when {
    isAmbientMode -> WatchTextSecondary
    isBull -> MarketBullish
    else -> MarketBearish
  }
  val showDollar = displayMode == QuoteDisplayMode.ETF_PRICE

  var scrubbedPrice by remember(quote, selectedRange) { mutableStateOf<Double?>(null) }
  var scrubbedLabel by remember(quote, selectedRange) { mutableStateOf<String?>(null) }

  val displayedPriceStr = scrubbedPrice?.let {
    val prefix = if (showDollar) "$" else ""
    prefix + String.format(Locale.US, "%,.2f", it)
  } ?: quote.formattedPrice(showDollarSign = showDollar)

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 18.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Spacer(modifier = Modifier.height(32.dp))

    // Header Row: Back Button + Index Title + Ticker Switch
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(CircleShape)
          .background(WatchSurfaceElevated)
          .clickable(onClick = onBackToDualHome)
          .testTag("detail_back_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back to Dual Watch View",
          tint = WatchTextPrimary,
          modifier = Modifier.size(13.dp)
        )
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = marketData.id.shortTitle,
          fontFamily = OutfitFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = accentColor
        )
        Text(
          text = scrubbedLabel?.let { "At $it" } ?: "${quote.symbol} • ${marketData.sourceName}",
          fontFamily = JetBrainsMonoFontFamily,
          fontSize = 8.sp,
          color = WatchTextMuted,
          maxLines = 1
        )
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(10.dp))
          .background(WatchSurfaceElevated)
          .clickable(onClick = onToggleDisplayMode)
          .padding(horizontal = 6.dp, vertical = 3.dp)
          .testTag("detail_unit_toggle"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = quote.symbol,
          fontFamily = JetBrainsMonoFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 9.sp,
          color = accentColor
        )
      }
    }

    // Price & Change Hero
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = displayedPriceStr,
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        color = WatchTextPrimary
      )
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = quote.formattedChange(),
          fontFamily = JetBrainsMonoFontFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 10.5.sp,
          color = deltaColor
        )
        Text(
          text = "(${quote.formattedPercent()})",
          fontFamily = JetBrainsMonoFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 10.5.sp,
          color = deltaColor
        )
      }
    }

    // Interactive Sparkline Canvas (Drag or Tap on Watch Face)
    SparklineCanvas(
      points = quote.sparkline,
      previousClose = quote.previousClose,
      isPositive = isBull,
      isAmbientMode = isAmbientMode,
      interactive = true,
      labels = quote.sparklineLabels,
      onScrubPoint = { p, lbl ->
        scrubbedPrice = p
        scrubbedLabel = lbl
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("detail_sparkline_canvas")
    )

    // Chart Timeframe Selector (1D, 5D, 1M, 1Y)
    Row(
      horizontalArrangement = Arrangement.spacedBy(5.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      ChartRange.values().forEach { range ->
        val selected = range == selectedRange
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
              if (selected) accentColor.copy(alpha = 0.25f) else WatchSurfaceCard
            )
            .border(
              width = 1.dp,
              color = if (selected) accentColor else Color(0xFF222F44),
              shape = RoundedCornerShape(8.dp)
            )
            .clickable { onSelectRange(range) }
            .padding(horizontal = 7.dp, vertical = 2.dp)
            .testTag("range_pill_${range.label}"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = range.label,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 8.5.sp,
            color = if (selected) WatchTextPrimary else WatchTextSecondary
          )
        }
      }
    }

    // Day Range Gauge + Open / Prev Close / Companion ETF
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      DayRangeGaugeBar(
        low = quote.dayLow,
        high = quote.dayHigh,
        current = quote.price,
        accentColor = accentColor,
        isAmbientMode = isAmbientMode
      )
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        Text(
          text = "OPEN ${String.format(Locale.US, "%,.1f", quote.dayOpen)}",
          fontFamily = JetBrainsMonoFontFamily,
          fontSize = 8.sp,
          color = WatchTextMuted
        )
        Text(
          text = "${secondaryQuote.symbol} ${
            secondaryQuote.formattedPrice(
              showDollarSign = displayMode == QuoteDisplayMode.INDEX_POINTS
            )
          }",
          fontFamily = JetBrainsMonoFontFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 8.sp,
          color = accentColor
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
  }
}

@Composable
fun WatchSettingsScreen(
  uiState: MarketWatchUiState,
  onSelectInterval: (AutoRefreshInterval) -> Unit,
  onSetDisplayMode: (QuoteDisplayMode) -> Unit,
  onToggleAmbientMode: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(34.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(CircleShape)
          .background(WatchSurfaceElevated)
          .clickable(onClick = onBack)
          .testTag("settings_back_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = WatchTextPrimary,
          modifier = Modifier.size(13.dp)
        )
      }
      Text(
        text = "Watch Config",
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = Sp500Cyan
      )
      Spacer(modifier = Modifier.width(24.dp))
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "AUTO-REFRESH RATE",
      fontFamily = JetBrainsMonoFontFamily,
      fontSize = 8.5.sp,
      color = WatchTextMuted
    )
    Spacer(modifier = Modifier.height(4.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(5.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      AutoRefreshInterval.values().forEach { interval ->
        val selected = uiState.autoRefreshInterval == interval
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Sp500Cyan.copy(alpha = 0.24f) else WatchSurfaceCard)
            .border(
              1.dp,
              if (selected) Sp500Cyan else Color(0xFF26334D),
              RoundedCornerShape(10.dp)
            )
            .clickable { onSelectInterval(interval) }
            .padding(horizontal = 7.dp, vertical = 4.dp)
            .testTag("interval_${interval.label}"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = interval.label,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 9.sp,
            color = if (selected) WatchTextPrimary else WatchTextSecondary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
      text = "PRIMARY TICKER FORMAT",
      fontFamily = JetBrainsMonoFontFamily,
      fontSize = 8.5.sp,
      color = WatchTextMuted
    )
    Spacer(modifier = Modifier.height(4.dp))

    QuoteDisplayMode.values().forEach { mode ->
      val selected = uiState.displayMode == mode
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 2.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(if (selected) NasdaqIndigo.copy(alpha = 0.22f) else WatchSurfaceCard)
          .border(
            1.dp,
            if (selected) NasdaqIndigo else Color(0xFF26334D),
            RoundedCornerShape(14.dp)
          )
          .clickable { onSetDisplayMode(mode) }
          .padding(horizontal = 10.dp, vertical = 6.dp)
          .testTag("mode_option_${mode.shortBadge}"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = mode.label,
          fontFamily = OutfitFontFamily,
          fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
          fontSize = 10.5.sp,
          color = if (selected) WatchTextPrimary else WatchTextSecondary
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Pixel Watch Always-On Display (Ambient OLED) Toggle
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(if (uiState.isAmbientMode) Sp500Cyan.copy(alpha = 0.2f) else WatchSurfaceCard)
        .border(
          1.dp,
          if (uiState.isAmbientMode) Sp500Cyan else Color(0xFF26334D),
          RoundedCornerShape(14.dp)
        )
        .clickable(onClick = onToggleAmbientMode)
        .padding(horizontal = 10.dp, vertical = 6.dp)
        .testTag("ambient_mode_toggle"),
      contentAlignment = Alignment.Center
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(
          imageVector = Icons.Default.BrightnessLow,
          contentDescription = "Ambient AOD Mode",
          tint = if (uiState.isAmbientMode) Sp500Cyan else WatchTextSecondary,
          modifier = Modifier.size(13.dp)
        )
        Text(
          text = if (uiState.isAmbientMode) "AOD Ambient: ON" else "AOD Ambient: OFF",
          fontFamily = OutfitFontFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = 10.5.sp,
          color = WatchTextPrimary
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}
