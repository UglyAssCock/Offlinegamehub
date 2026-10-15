package com.example.offlinegamehub

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
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
import kotlinx.coroutines.delay

data class MemoryCard(
    val id: Int,
    val symbol: String,
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

    val allSymbols = listOf("⚽", "🏀", "🌍", "🏔️", "🏆", "🎲", "🎯", "🚀", "💎", "⚡")
    var isHardMode by remember { mutableStateOf(false) }
    val pairCount = if (isHardMode) 10 else 8

    var cards by remember { mutableStateOf<List<MemoryCard>>(emptyList()) }
    var firstSelectedIndex by remember { mutableStateOf<Int?>(null) }
    var secondSelectedIndex by remember { mutableStateOf<Int?>(null) }
    var moves by remember { mutableIntStateOf(0) }
    var matchedPairs by remember { mutableIntStateOf(0) }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    var isGameWon by remember { mutableStateOf(false) }
    var earnedScore by remember { mutableIntStateOf(0) }

    fun startNewGame(hard: Boolean = isHardMode) {
        val selectedSymbols = allSymbols.take(if (hard) 10 else 8)
        val deck = (selectedSymbols + selectedSymbols).shuffled().mapIndexed { index, sym ->
            MemoryCard(id = index, symbol = sym)
        }
        cards = deck
        firstSelectedIndex = null
        secondSelectedIndex = null
        moves = 0
        matchedPairs = 0
        secondsElapsed = 0
        isGameWon = false
        earnedScore = 0
    }

    LaunchedEffect(Unit) {
        startNewGame(false)
    }

    LaunchedEffect(isGameWon, moves) {
        if (!isGameWon && moves > 0) {
            while (!isGameWon) {
                delay(1000L)
                secondsElapsed++
            }
        }
    }

    LaunchedEffect(firstSelectedIndex, secondSelectedIndex) {
        val first = firstSelectedIndex
        val second = secondSelectedIndex
        if (first != null && second != null) {
            if (cards[first].symbol == cards[second].symbol) {
                delay(300L)
                cards = cards.mapIndexed { idx, card ->
                    if (idx == first || idx == second) card.copy(isMatched = true) else card
                }
                matchedPairs++
                if (matchedPairs == pairCount) {
                    val base = if (isHardMode) 1500 else 1000
                    val penalty = (moves * 15) + (secondsElapsed * 3)
                    earnedScore = maxOf(100, base - penalty)
                    onSaveScore(earnedScore)
                    isGameWon = true
                }
            } else {
                delay(750L)
                cards = cards.mapIndexed { idx, card ->
                    if (idx == first || idx == second) card.copy(isFaceUp = false) else card
                }
            }
            firstSelectedIndex = null
            secondSelectedIndex = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
                text = "Memory Match",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            IconButton(onClick = { startNewGame(isHardMode) }) {
                Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color(0xFF8B5CF6))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    isHardMode = false
                    startNewGame(false)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isHardMode) Color(0xFF8B5CF6) else Color(0xFF1E293B)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("4x4 Grid (16)", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    isHardMode = true
                    startNewGame(true)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isHardMode) Color(0xFF8B5CF6) else Color(0xFF1E293B)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("4x5 Grid (20)", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MOVES", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text("$moves", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PAIRS", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text("$matchedPairs / $pairCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TIME", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text("${secondsElapsed}s", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BEST", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text("${maxOf(highScore, earnedScore)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isGameWon) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Win",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Board Cleared!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Score: $earnedScore pts ($moves moves in ${secondsElapsed}s)",
                        fontSize = 16.sp,
                        color = Color(0xFF8B5CF6),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { startNewGame(isHardMode) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Play Again", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(cards) { index, card ->
                    val isRevealed = card.isFaceUp || card.isMatched
                    val cardColor = when {
                        card.isMatched -> Color(0xFF065F46)
                        card.isFaceUp -> Color(0xFF8B5CF6)
                        else -> Color(0xFF1E293B)
                    }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(cardColor)
                            .clickable(
                                enabled = !isRevealed && secondSelectedIndex == null
                            ) {
                                cards = cards.mapIndexed { idx, c ->
                                    if (idx == index) c.copy(isFaceUp = true) else c
                                }
                                if (firstSelectedIndex == null) {
                                    firstSelectedIndex = index
                                } else if (firstSelectedIndex != index) {
                                    secondSelectedIndex = index
                                    moves++
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isRevealed) card.symbol else "?",
                            fontSize = if (isRevealed) 30.sp else 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isRevealed) Color.White else Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}
