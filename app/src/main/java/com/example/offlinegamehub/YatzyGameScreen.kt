package com.example.offlinegamehub

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun YatzyGameScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var vsBot by remember { mutableStateOf(true) }
    var dice by remember { mutableStateOf(List(5) { 0 }) }
    var held by remember { mutableStateOf(List(5) { false }) }
    var rollsUsed by remember { mutableIntStateOf(0) }
    var isPlayerTurn by remember { mutableStateOf(true) }

    val leftCats = listOf("⚀", "⚁", "⚂", "⚃", "⚄", "⚅")
    val rightCats = listOf("3x", "4x", "🏠", "SMALL", "LARGE", "YATZY", "?")
    var p1Scores by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var botScores by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    fun calcScore(cat: String, d: List<Int>): Int {
        if (d.all { it == 0 }) return 0
        val counts = d.groupingBy { it }.eachCount()
        val sum = d.sum()
        return when (cat) {
            "⚀" -> d.filter { it == 1 }.sum()
            "⚁" -> d.filter { it == 2 }.sum()
            "⚂" -> d.filter { it == 3 }.sum()
            "⚃" -> d.filter { it == 4 }.sum()
            "⚄" -> d.filter { it == 5 }.sum()
            "⚅" -> d.filter { it == 6 }.sum()
            "3x" -> if (counts.values.any { it >= 3 }) sum else 0
            "4x" -> if (counts.values.any { it >= 4 }) sum else 0
            "🏠" -> if ((counts.values.contains(3) && counts.values.contains(2)) || counts.values.contains(5)) 25 else 0
            "SMALL" -> 30
            "LARGE" -> 40
            "YATZY" -> if (counts.values.contains(5)) 50 else 0
            "?" -> sum
            else -> 0
        }
    }

    LaunchedEffect(isPlayerTurn, vsBot) {
        if (vsBot && !isPlayerTurn) {
            delay(650L)
            val allCats = leftCats + rightCats
            val avail = allCats.filter { !botScores.containsKey(it) }
            if (avail.isNotEmpty()) {
                val pick = avail.random()
                botScores = botScores + (pick to Random.nextInt(8, 28))
            }
            dice = List(5) { 0 }
            held = List(5) { false }
            rollsUsed = 0
            isPlayerTurn = true
        }
    }

    val p1Total = p1Scores.values.sum()
    val botTotal = botScores.values.sum()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF39C7D))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ArcadeVsHeader(
            youScore = p1Total,
            opponentScore = botTotal,
            vsBot = vsBot,
            onToggleMode = { vsBot = !vsBot; p1Scores = emptyMap(); botScores = emptyMap() },
            onBack = onBack,
            onRestart = { p1Scores = emptyMap(); botScores = emptyMap(); rollsUsed = 0; dice = List(5) { 0 } }
        )

        // Cream 2-Column Scorecard matching Image 8
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFF4D2))
                .border(4.dp, Color(0xFF3E3A3F), RoundedCornerShape(16.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Column (1-6 + Bonus)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                leftCats.forEach { cat ->
                    YatzyScoreRow(
                        label = cat,
                        p1Val = p1Scores[cat],
                        botVal = botScores[cat],
                        preview = if (rollsUsed > 0) calcScore(cat, dice) else null,
                        onSelect = {
                            if (rollsUsed > 0 && !p1Scores.containsKey(cat)) {
                                val next = p1Scores + (cat to calcScore(cat, dice))
                                p1Scores = next
                                onSaveScore(next.values.sum())
                                isPlayerTurn = false
                            }
                        }
                    )
                }
                Text("BONUS +35", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF6B3A70))
            }

            // Right Column (3x, 4x, Full House, Straights, Yatzy, ?)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rightCats.forEach { cat ->
                    YatzyScoreRow(
                        label = cat,
                        p1Val = p1Scores[cat],
                        botVal = botScores[cat],
                        preview = if (rollsUsed > 0) calcScore(cat, dice) else null,
                        onSelect = {
                            if (rollsUsed > 0 && !p1Scores.containsKey(cat)) {
                                val next = p1Scores + (cat to calcScore(cat, dice))
                                p1Scores = next
                                onSaveScore(next.values.sum())
                                isPlayerTurn = false
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5 Dark Dice Slots with Star Icons / Numbers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dice.forEachIndexed { idx, v ->
                val isHeld = held[idx]
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isHeld) Color(0xFFFFBC2B) else Color(0xFF54585A))
                        .border(3.dp, Color(0xFF3E3A3F), RoundedCornerShape(10.dp))
                        .clickable(enabled = rollsUsed > 0) {
                            held = held.mapIndexed { i, h -> if (i == idx) !h else h }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (v == 0) "✦" else "$v",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Red ROLL [1] [2] [3] Button Bar matching Image 8
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFD9534F))
                .clickable(enabled = rollsUsed < 3 && isPlayerTurn) {
                    dice = dice.mapIndexed { i, old -> if (held[i] && old != 0) old else Random.nextInt(1, 7) }
                    rollsUsed++
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("ROLL", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                for (r in 1..3) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (rollsUsed < r) Color.White else Color(0xFF54585A))
                            .border(2.dp, Color(0xFF3E3A3F), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$r", color = Color(0xFF3E3A3F), fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun YatzyScoreRow(
    label: String,
    p1Val: Int?,
    botVal: Int?,
    preview: Int?,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(2.dp, Color(0xFF6B3A70), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(label, fontSize = if (label.length > 3) 10.sp else 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF3E3A3F))
        }
        // Red YOU Score Box
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF39C7D))
                .border(2.dp, Color(0xFFD9534F), RoundedCornerShape(8.dp))
                .clickable { onSelect() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = p1Val?.toString() ?: (preview?.toString() ?: ""),
                fontWeight = FontWeight.Black,
                color = if (p1Val != null) Color.Black else Color.White
            )
        }
        // Blue BOT Score Box
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF81C3D7)),
            contentAlignment = Alignment.Center
        ) {
            Text(botVal?.toString() ?: "", fontWeight = FontWeight.Black, color = Color.Black)
        }
    }
}
