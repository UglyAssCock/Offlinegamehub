package com.example.offlinegamehub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class GameMode {
    SOLO,
    BOT_EASY,
    BOT_HARD,
    FRIEND_LOCAL
}

@Composable
fun ModeSelectHeader(
    gameTitle: String,
    currentMode: GameMode,
    onModeChanged: (GameMode) -> Unit,
    accentColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SELECT GAME MODE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val modes = listOf(
                GameMode.SOLO to "Solo",
                GameMode.BOT_EASY to "Easy Bot",
                GameMode.BOT_HARD to "Pro Bot",
                GameMode.FRIEND_LOCAL to "2 Players"
            )
            modes.forEach { (mode, label) ->
                val isSelected = currentMode == mode
                Button(
                    onClick = { onModeChanged(mode) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) accentColor else Color(0xFF1E293B)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else Color.White
                    )
                }
            }
        }
    }
}
