package com.example.offlinegamehub

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun YatzyGameScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val categories = listOf(
        "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes",
        "Three of a Kind", "Four of a Kind", "Full House",
        "Small Straight", "Large Straight", "Yatzy (50)", "Chance"
    )

    var dice by remember { mutableStateOf(List(5) { 1 }) }
    var held by remember { mutableStateOf(List(5) { false }) }
    var rollsLeft by remember { mutableIntStateOf(3) }
    var scores by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    fun calculatePotentialScore(category: String, currentDice: List<Int>): Int {
        val counts = currentDice.groupingBy { it }.eachCount()
        val sum = currentDice.sum()
        val uniqueSorted = currentDice.distinct().sorted()

        return when (category) {
            "Ones" -> currentDice.filter { it == 1 }.sum()
            "Twos" -> currentDice.filter { it == 2 }.sum()
            "Threes" -> currentDice.filter { it == 3 }.sum()
            "Fours" -> currentDice.filter { it == 4 }.sum()
            "Fives" -> currentDice.filter { it == 5 }.sum()
            "Sixes" -> currentDice.filter { it == 6 }.sum()
            "Three of a Kind" -> if (counts.values.any { it >= 3 }) sum else 0
            "Four of a Kind" -> if (counts.values.any { it >= 4 }) sum else 0
            "Full House" -> if (counts.values.contains(3) && counts.values.contains(2) || counts.values.contains(5)) 25 else 0
            "Small Straight" -> {
                val straights = listOf(listOf(1, 2, 3, 4), listOf(2, 3, 4, 5), listOf(3, 4, 5, 6))
                if (straights.any { s -> uniqueSorted.containsAll(s) }) 30 else 0
            }
            "Large Straight" -> {
                if (uniqueSorted == listOf(1, 2, 3, 4, 5) || uniqueSorted == listOf(2, 3, 4, 5, 6)) 40 else 0
            }
            "Yatzy (50)" -> if (counts.values.contains(5)) 50 else 0
            "Chance" -> sum
            else -> 0
        }
    }

    val upperCategories = categories.take(6)
    val upperSum = upperCategories.sumOf { scores[it] ?: 0 }
    val upperBonus = if (upperSum >= 63) 35 else 0
    val totalScore = scores.values.sum() + upperBonus
    val isGameOver = scores.size == categories.size

    fun resetGame() {
        dice = List(5) { 1 }
        held = List(5) { false }
        rollsLeft = 3
        scores = emptyMap()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Yatzy Classic",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            IconButton(onClick = { resetGame() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color(0xFFEC4899))
            }
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SCORE", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text("$totalScore", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEC4899))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("UPPER BONUS", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text("$upperSum / 63 (+$upperBonus)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BEST", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text("${maxOf(highScore, totalScore)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dice.forEachIndexed { index, value ->
                val isHeld = held[index]
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isHeld) Color(0xFFEC4899) else Color(0xFF1E293B))
                        .border(
                            width = 2.dp,
                            color = if (isHeld) Color.White else Color(0xFF334155),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable(enabled = rollsLeft < 3 && !isGameOver) {
                            held = held.mapIndexed { i, h -> if (i == index) !h else h }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (rollsLeft == 3) "-" else "$value",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        if (isHeld) {
                            Text(
                                text = "HOLD",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                if (isGameOver) {
                    resetGame()
                } else if (rollsLeft > 0) {
                    dice = dice.mapIndexed { idx, oldVal ->
                        if (held[idx]) oldVal else Random.nextInt(1, 7)
                    }
                    rollsLeft--
                }
            },
            enabled = rollsLeft > 0 || isGameOver,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = when {
                    isGameOver -> "Game Over! Final Score: $totalScore — Play Again"
                    rollsLeft == 3 -> "Roll Dice (3 Rolls Left)"
                    else -> "Roll Again ($rollsLeft Left) or Tap a Category Below"
                },
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "SCORECARD (Tap a row after rolling to lock score)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(6.dp))

        categories.forEach { category ->
            val lockedScore = scores[category]
            val previewScore = if (rollsLeft < 3) calculatePotentialScore(category, dice) else 0
            val canSelect = lockedScore == null && rollsLeft < 3

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (lockedScore != null) Color(0xFF0F172A) else Color(0xFF1E293B)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clickable(enabled = canSelect) {
                        val newScores = scores + (category to previewScore)
                        scores = newScores
                        rollsLeft = 3
                        held = List(5) { false }
                        if (newScores.size == categories.size) {
                            val finalUpper = upperCategories.sumOf { newScores[it] ?: 0 }
                            val finalBonus = if (finalUpper >= 63) 35 else 0
                            onSaveScore(newScores.values.sum() + finalBonus)
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (lockedScore != null) Color(0xFF64748B) else Color.White
                    )
                    Text(
                        text = when {
                            lockedScore != null -> "$lockedScore pts"
                            rollsLeft < 3 -> "+$previewScore"
                            else -> "-"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            lockedScore != null -> Color(0xFF10B981)
                            rollsLeft < 3 && previewScore > 0 -> Color(0xFFF59E0B)
                            else -> Color(0xFF64748B)
                        }
                    )
                }
            }
        }
    }
}
