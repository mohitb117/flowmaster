package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketSessionState
import com.example.model.WatchScreenDestination
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MarketBearish
import com.example.ui.theme.MarketBullish
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.Sp500Cyan
import com.example.ui.theme.WatchAmber
import com.example.ui.theme.WatchTextMuted
import com.example.ui.theme.WatchTextPrimary
import com.example.ui.theme.WatchTextSecondary
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun WatchTopStatusHeader(
  watchTime: String,
  sessionState: MarketSessionState,
  currentDestination: WatchScreenDestination,
  isAmbientMode: Boolean,
  onSelectDestination: (WatchScreenDestination) -> Unit,
  modifier: Modifier = Modifier
) {
  val sessionColor = when {
    isAmbientMode -> WatchTextMuted
    sessionState == MarketSessionState.OPEN -> MarketBullish
    sessionState == MarketSessionState.PRE_MARKET || sessionState == MarketSessionState.AFTER_HOURS -> WatchAmber
    else -> WatchTextSecondary
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Text(
        text = watchTime,
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        color = if (isAmbientMode) WatchTextSecondary else WatchTextPrimary
      )
      Spacer(modifier = Modifier.width(6.dp))
      Box(
        modifier = Modifier
          .size(5.dp)
          .clip(CircleShape)
          .background(sessionColor)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = sessionState.badgeText,
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 9.sp,
        color = sessionColor
      )
    }

    Spacer(modifier = Modifier.height(3.dp))

    // Watch Page Dots (Home / SPX / NDX / Settings)
    val destinations = listOf(
      WatchScreenDestination.DUAL_HOME,
      WatchScreenDestination.SP500_DETAIL,
      WatchScreenDestination.NASDAQ100_DETAIL,
      WatchScreenDestination.WATCH_SETTINGS
    )
    Row(
      horizontalArrangement = Arrangement.spacedBy(5.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      destinations.forEach { dest ->
        val selected = dest == currentDestination
        Box(
          modifier = Modifier
            .size(width = if (selected) 12.dp else 5.dp, height = 5.dp)
            .clip(CircleShape)
            .background(
              when {
                selected && isAmbientMode -> WatchTextSecondary
                selected -> Sp500Cyan
                else -> Color(0xFF28354A)
              }
            )
            .clickable { onSelectDestination(dest) }
        )
      }
    }
  }
}

@Composable
fun WatchCurvedPositionIndicator(
  activeIndex: Int,
  totalCount: Int,
  isAmbientMode: Boolean,
  modifier: Modifier = Modifier
) {
  if (isAmbientMode) return
  Canvas(modifier = modifier.fillMaxSize()) {
    val strokeWidth = 3.dp.toPx()
    val inset = 5.dp.toPx()
    val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
    val topLeft = Offset(inset, inset)

    // Track arc on the right bezel (-28 deg to +28 deg)
    val totalSweep = 52f
    val startTrackAngle = -26f
    drawArc(
      color = Color(0xFF1F2B3E),
      startAngle = startTrackAngle,
      sweepAngle = totalSweep,
      useCenter = false,
      topLeft = topLeft,
      size = arcSize,
      style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    // Thumb arc for current page
    val count = max(1, totalCount)
    val thumbSweep = totalSweep / count.toFloat()
    val thumbStart = startTrackAngle + (activeIndex.coerceIn(0, count - 1) * thumbSweep)
    drawArc(
      color = Sp500Cyan,
      startAngle = thumbStart,
      sweepAngle = max(8f, thumbSweep - 2f),
      useCenter = false,
      topLeft = topLeft,
      size = arcSize,
      style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
  }
}

@Composable
fun SparklineCanvas(
  points: List<Double>,
  previousClose: Double,
  isPositive: Boolean,
  isAmbientMode: Boolean,
  modifier: Modifier = Modifier,
  interactive: Boolean = false,
  labels: List<String> = emptyList(),
  onScrubPoint: ((price: Double?, label: String?) -> Unit)? = null
) {
  var scrubIndex by remember(points) { mutableStateOf<Int?>(null) }

  val lineColor = when {
    isAmbientMode -> WatchTextSecondary
    isPositive -> MarketBullish
    else -> MarketBearish
  }

  val interactionModifier = if (interactive && points.size >= 2) {
    Modifier
      .pointerInput(points) {
        detectTapGestures(
          onPress = { offset ->
            val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
            val idx = (fraction * (points.size - 1)).toInt().coerceIn(0, points.lastIndex)
            scrubIndex = idx
            onScrubPoint?.invoke(points[idx], labels.getOrNull(idx))
            tryAwaitRelease()
            scrubIndex = null
            onScrubPoint?.invoke(null, null)
          }
        )
      }
      .pointerInput(points) {
        detectHorizontalDragGestures(
          onDragEnd = {
            scrubIndex = null
            onScrubPoint?.invoke(null, null)
          },
          onDragCancel = {
            scrubIndex = null
            onScrubPoint?.invoke(null, null)
          },
          onHorizontalDrag = { change, _ ->
            change.consume()
            val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
            val idx = (fraction * (points.size - 1)).toInt().coerceIn(0, points.lastIndex)
            scrubIndex = idx
            onScrubPoint?.invoke(points[idx], labels.getOrNull(idx))
          }
        )
      }
  } else Modifier

  Canvas(modifier = modifier.then(interactionModifier)) {
    if (points.size < 2) return@Canvas

    val allValues = points + previousClose
    val minVal = allValues.minOrNull() ?: 0.0
    val maxVal = allValues.maxOrNull() ?: 1.0
    val span = max(0.0001, maxVal - minVal)

    val padVertical = 4.dp.toPx()
    val padRight = 5.dp.toPx()
    val usableWidth = max(1f, size.width - padRight)
    val usableHeight = max(1f, size.height - padVertical * 2f)

    fun mapPoint(index: Int, value: Double): Offset {
      val x = (index.toFloat() / (points.size - 1).toFloat()) * usableWidth
      val normalizedY = ((value - minVal) / span).toFloat().coerceIn(0f, 1f)
      val y = padVertical + usableHeight * (1f - normalizedY)
      return Offset(x, y)
    }

    // Previous close reference line
    if (previousClose in minVal..maxVal) {
      val prevY = mapPoint(0, previousClose).y
      drawLine(
        color = Color(0xFF475569).copy(alpha = if (isAmbientMode) 0.35f else 0.65f),
        start = Offset(0f, prevY),
        end = Offset(usableWidth, prevY),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
      )
    }

    val strokePath = Path()
    val fillPath = Path()

    val mappedOffsets = points.mapIndexed { idx, value -> mapPoint(idx, value) }
    strokePath.moveTo(mappedOffsets.first().x, mappedOffsets.first().y)
    fillPath.moveTo(mappedOffsets.first().x, size.height)
    fillPath.lineTo(mappedOffsets.first().x, mappedOffsets.first().y)

    for (i in 1 until mappedOffsets.size) {
      val prev = mappedOffsets[i - 1]
      val curr = mappedOffsets[i]
      val controlX = (prev.x + curr.x) / 2f
      strokePath.cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
      fillPath.cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
    }

    fillPath.lineTo(mappedOffsets.last().x, size.height)
    fillPath.close()

    if (!isAmbientMode) {
      drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
          colors = listOf(
            lineColor.copy(alpha = 0.32f),
            lineColor.copy(alpha = 0.04f),
            Color.Transparent
          ),
          startY = 0f,
          endY = size.height
        )
      )
    }

    drawPath(
      path = strokePath,
      color = lineColor,
      style = Stroke(
        width = if (isAmbientMode) 1.4.dp.toPx() else 2.2.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
      )
    )

    val activeIdx = scrubIndex ?: mappedOffsets.lastIndex
    val highlightOffset = mappedOffsets[activeIdx.coerceIn(0, mappedOffsets.lastIndex)]

    if (scrubIndex != null) {
      drawLine(
        color = Sp500Cyan.copy(alpha = 0.75f),
        start = Offset(highlightOffset.x, 0f),
        end = Offset(highlightOffset.x, size.height),
        strokeWidth = 1.dp.toPx()
      )
    }

    if (!isAmbientMode) {
      drawCircle(
        color = lineColor.copy(alpha = 0.3f),
        radius = 5.5.dp.toPx(),
        center = highlightOffset
      )
    }
    drawCircle(
      color = if (scrubIndex != null) Sp500Cyan else lineColor,
      radius = 2.8.dp.toPx(),
      center = highlightOffset
    )
  }
}

@Composable
fun DayRangeGaugeBar(
  low: Double,
  high: Double,
  current: Double,
  accentColor: Color,
  isAmbientMode: Boolean,
  modifier: Modifier = Modifier
) {
  val span = max(0.01, high - low)
  val fraction = ((current - low) / span).toFloat().coerceIn(0f, 1f)

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "L ${String.format(Locale.US, "%,.1f", low)}",
        fontFamily = JetBrainsMonoFontFamily,
        fontSize = 8.5.sp,
        color = WatchTextSecondary
      )
      Text(
        text = "DAY RANGE",
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 8.sp,
        color = WatchTextMuted
      )
      Text(
        text = "H ${String.format(Locale.US, "%,.1f", high)}",
        fontFamily = JetBrainsMonoFontFamily,
        fontSize = 8.5.sp,
        color = WatchTextSecondary
      )
    }
    Spacer(modifier = Modifier.height(3.dp))
    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(5.dp)
        .clip(RoundedCornerShape(3.dp))
    ) {
      drawRoundRect(
        color = Color(0xFF1E293B),
        size = size
      )
      if (!isAmbientMode) {
        drawRoundRect(
          color = accentColor.copy(alpha = 0.45f),
          size = Size(size.width * fraction, size.height)
        )
      }
      val dotX = (size.width * fraction).coerceIn(3.dp.toPx(), max(3.dp.toPx(), size.width - 3.dp.toPx()))
      drawCircle(
        color = if (isAmbientMode) WatchTextPrimary else accentColor,
        radius = 2.6.dp.toPx(),
        center = Offset(dotX, size.height / 2f)
      )
    }
  }
}
