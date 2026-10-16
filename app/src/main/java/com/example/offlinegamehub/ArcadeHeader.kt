package com.example.offlinegamehub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ArcadeVsHeader(
    youScore: Int,
    opponentScore: Int,
    vsBot: Boolean,
    modeLabel: String = "HARD",
    onToggleMode: () -> Unit,
    onBack: () -> Unit,
    onRestart: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left White Circle Back Button
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = Color(0xFFD9779B),
                modifier = Modifier.size(32.dp)
            )
        }

        // Center YOU VS BOT Pill (Tap to switch Bot / Friend)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF4A444B))
                .border(2.dp, Color(0xFF2D282E), RoundedCornerShape(16.dp))
                .clickable { onToggleMode() }
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("YOU", color = Color(0xFFFF6B6B), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    Text("$youScore", color = Color(0xFFFF6B6B), fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(modeLabel, color = Color(0xFFCBD5E1), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("VS", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (vsBot) "BOT" else "P2",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("$opponentScore", color = Color(0xFF38BDF8), fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Right White Circle Restart Button
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onRestart() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Restart",
                tint = Color(0xFFD9779B),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun ArcadeSoloHeader(
    leftTitle: String,
    leftValue: String,
    rightTitle: String,
    rightValue: String,
    onBack: () -> Unit,
    onRestart: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = Color(0xFFD9779B),
                modifier = Modifier.size(32.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1F2937).copy(alpha = 0.85f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(leftTitle, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(leftValue, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1F2937).copy(alpha = 0.85f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(rightTitle, color = Color(0xFF34D399), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(rightValue, color = Color(0xFF34D399), fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onRestart() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Restart",
                tint = Color(0xFFD9779B),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
