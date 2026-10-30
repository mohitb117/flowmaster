package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = FlowCyan,
  onPrimary = Color(0xFF003544),
  primaryContainer = Color(0xFF004D63),
  onPrimaryContainer = Color(0xFFBBE9FF),
  secondary = FlowIndigo,
  onSecondary = Color(0xFF1E2156),
  secondaryContainer = Color(0xFF333878),
  onSecondaryContainer = Color(0xFFE0E0FF),
  tertiary = FlowPurple,
  onTertiary = Color(0xFF430067),
  tertiaryContainer = Color(0xFF5F148B),
  onTertiaryContainer = Color(0xFFF7D8FF),
  background = FlowDarkBg,
  onBackground = Color(0xFFF1F5F9),
  surface = FlowDarkSurface,
  onSurface = Color(0xFFF1F5F9),
  surfaceVariant = FlowDarkSurfaceVariant,
  onSurfaceVariant = Color(0xFF94A3B8),
  outline = Color(0xFF334155),
  outlineVariant = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
  primary = FlowLightPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE0F2FE),
  onPrimaryContainer = Color(0xFF0369A1),
  secondary = FlowLightSecondary,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFEEF2FF),
  onSecondaryContainer = Color(0xFF3730A3),
  tertiary = FlowLightTertiary,
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFFF5F3FF),
  onTertiaryContainer = Color(0xFF6B21A8),
  background = FlowLightBg,
  onBackground = Color(0xFF0F172A),
  surface = FlowLightSurface,
  onSurface = Color(0xFF0F172A),
  surfaceVariant = FlowLightSurfaceVariant,
  onSurfaceVariant = Color(0xFF64748B),
  outline = Color(0xFFCBD5E1),
  outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our tailored theme for consistent vibrant aesthetic
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
