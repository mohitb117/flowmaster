package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PixelWatchColorScheme = darkColorScheme(
  primary = Sp500Cyan,
  onPrimary = Color(0xFF002635),
  primaryContainer = Color(0xFF07364B),
  onPrimaryContainer = Color(0xFFBBE9FF),
  secondary = NasdaqIndigo,
  onSecondary = Color(0xFF161948),
  secondaryContainer = Color(0xFF282D69),
  onSecondaryContainer = Color(0xFFE0E0FF),
  tertiary = MarketBullish,
  onTertiary = Color(0xFF003919),
  background = WatchOledBlack,
  onBackground = WatchTextPrimary,
  surface = WatchSurfaceDark,
  onSurface = WatchTextPrimary,
  surfaceVariant = WatchSurfaceCard,
  onSurfaceVariant = WatchTextSecondary,
  outline = Color(0xFF26334D),
  outlineVariant = Color(0xFF172033)
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = PixelWatchColorScheme,
    typography = Typography,
    content = content
  )
}
