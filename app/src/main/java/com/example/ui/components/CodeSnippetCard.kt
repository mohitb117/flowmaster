package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun CodeSnippetCard(
  code: String,
  title: String? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp)),
    color = Color(0xFF0F172A),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column {
      // Header bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF1E293B))
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          // Terminal dots
          Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFEF4444)))
          Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF59E0B)))
          Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF10B981)))
          if (title != null) {
            Text(
              text = title,
              fontFamily = JetBrainsMonoFontFamily,
              fontSize = 11.sp,
              color = Color(0xFF94A3B8),
              modifier = Modifier.padding(start = 6.dp)
            )
          }
        }

        IconButton(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Flow Code", code)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier.size(28.dp).testTag("copy_code_button")
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy code",
            tint = FlowCyan,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      // Code body with horizontal scroll
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(14.dp)
      ) {
        Text(
          text = code,
          fontFamily = JetBrainsMonoFontFamily,
          fontSize = 12.sp,
          lineHeight = 18.sp,
          color = Color(0xFFE2E8F0)
        )
      }
    }
  }
}
