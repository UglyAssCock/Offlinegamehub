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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class GoldenMemoryCard(
    val id: Int,
    val symbol: String,
    val tiltAngle: Float,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

@Composable
fun MemoryGameScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val symbols = listOf("⚽", "🏀", "🌍", "🏔️", "🏆", "🎲", "🎯", "🚀", "💎", "⚡", "👑", "🔥", "🎸")
    var vsBot by remember { mutableStateOf(true) }
    var cards by remember { mutableStateOf<List<GoldenMemoryCard>>(emptyList()) }
    var firstIdx by remember { mutableStateOf<Int?>(null) }
    var secondIdx by remember { mutableStateOf<Int?>(null) }
    var youScore by remember { mutableIntStateOf(0) }
    var botScore by remember { mutableIntStateOf(0) }
    var isPlayerTurn by remember { mutableStateOf(true) }

    fun resetMemory() {
        val deckSyms = (symbols + symbols).shuffled()
        val tilts = listOf(-4f, 3f, -2f, 4f, -3f, 2f)
        cards = deckSyms.mapIndexed { i, s ->
            GoldenMemoryCard(i, s, tilts[i % tilts.size])
        }
        firstIdx = null
        secondIdx = null
        youScore = 0
        botScore = 0
        isPlayerTurn = true
    }

    LaunchedEffect(Unit) { resetMemory() }

    LaunchedEffect(firstIdx, secondIdx) {
        val f = firstIdx
        val s = secondIdx
        if (f != null && s != null) {
            if (cards[f].symbol == cards[s].symbol) {
                delay(350L)
                cards = cards.mapIndexed { idx, c -> if (idx == f || idx == s) c.copy(isMatched = true) else c }
                if (isPlayerTurn) {
                    youScore++
                    onSaveScore(youScore * 100)
                } else {
                    botScore++
                }
            } else {
                delay(700L)
                cards = cards.mapIndexed { idx, c -> if (idx == f || idx == s) c.copy(isFaceUp = false) else c }
                isPlayerTurn = !isPlayerTurn
            }
            firstIdx = null
            secondIdx = null
        }
    }

    LaunchedEffect(isPlayerTurn, vsBot, firstIdx) {
        if (vsBot && !isPlayerTurn && firstIdx == null && cards.any { !it.isMatched }) {
            delay(650L)
            val hidden = cards.indices.filter { !cards[it].isMatched && !cards[it].isFaceUp }
            if (hidden.size >= 2) {
                val pick = hidden.shuffled().take(2)
                cards = cards.mapIndexed { idx, c -> if (idx in pick) c.copy(isFaceUp = true) else c }
                firstIdx = pick[0]
                secondIdx = pick[1]
            }
        }
    }

    // Row distribution matching Image 9 (3, 4, 4, 4, 4, 4, 3 = 26 cards)
    val rowCounts = listOf(3, 4, 4, 4, 4, 4, 3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF47393B)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ArcadeVsHeader(
            youScore = youScore,
            opponentScore = botScore,
            vsBot = vsBot,
            onToggleMode = { vsBot = !vsBot; resetMemory() },
            onBack = onBack,
            onRestart = { resetMemory() }
        )

        if (cards.size == 26) {
            var cursor = 0
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                rowCounts.forEach { count ->
                    val slice = cards.subList(cursor, cursor + count)
                    cursor += count
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        slice.forEach { card ->
                            val revealed = card.isFaceUp || card.isMatched
                            Box(
                                modifier = Modifier
                                    .size(66.dp)
                                    .rotate(card.tiltAngle)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (revealed) Color.White else Color(0xFFFFC03D))
                                    .border(4.dp, Color.White, RoundedCornerShape(10.dp))
                                    .clickable(enabled = !revealed && secondIdx == null && (isPlayerTurn || !vsBot)) {
                                        cards = cards.mapIndexed { idx, c ->
                                            if (idx == card.id) c.copy(isFaceUp = true) else c
                                        }
                                        if (firstIdx == null) firstIdx = card.id else secondIdx = card.id
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (revealed) {
                                    Text(card.symbol, fontSize = 30.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
