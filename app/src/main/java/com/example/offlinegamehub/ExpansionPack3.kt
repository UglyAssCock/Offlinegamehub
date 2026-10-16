package com.example.offlinegamehub

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import kotlin.random.Random

// ==========================================
// 1. CLASSIC SNAKE ARCADE
// ==========================================
@Composable
fun SnakeGameScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val gridSize = 15
    var snake by remember { mutableStateOf(listOf(Pair(7, 7), Pair(7, 6), Pair(7, 5))) }
    var dir by remember { mutableStateOf(Pair(0, 1)) }
    var food by remember { mutableStateOf(Pair(3, 10)) }
    var score by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    fun restartSnake() {
        snake = listOf(Pair(7, 7), Pair(7, 6), Pair(7, 5))
        dir = Pair(0, 1)
        food = Pair(Random.nextInt(gridSize), Random.nextInt(gridSize))
        score = 0
        isGameOver = false
    }

    LaunchedEffect(isGameOver) {
        while (!isGameOver) {
            delay(210L)
            val head = snake.first()
            val nextHead = Pair(head.first + dir.first, head.second + dir.second)

            if (nextHead.first !in 0 until gridSize ||
                nextHead.second !in 0 until gridSize ||
                snake.contains(nextHead)
            ) {
                isGameOver = true
                onSaveScore(score)
            } else {
                if (nextHead == food) {
                    val grown = listOf(nextHead) + snake
                    snake = grown
                    score += 50
                    var nextFood: Pair<Int, Int>
                    do {
                        nextFood = Pair(Random.nextInt(gridSize), Random.nextInt(gridSize))
                    } while (grown.contains(nextFood))
                    food = nextFood
                } else {
                    snake = listOf(nextHead) + snake.dropLast(1)
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Text("Snake Classic", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = { restartSnake() }) { Icon(Icons.Default.Refresh, null, tint = Color(0xFF4ADE80)) }
        }

        Text(
            text = "Score: $score   |   Best: ${maxOf(highScore, score)}",
            color = Color(0xFF4ADE80),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 15x15 Board
        Column(
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E293B))
                .border(2.dp, Color(0xFF4ADE80), RoundedCornerShape(12.dp))
        ) {
            for (r in 0 until gridSize) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    for (c in 0 until gridSize) {
                        val pos = Pair(r, c)
                        val isHead = snake.first() == pos
                        val isBody = snake.contains(pos)
                        val isApple = food == pos

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(
                                    when {
                                        isHead -> Color(0xFF22C55E)
                                        isBody -> Color(0xFF86EFAC)
                                        isApple -> Color(0xFFEF4444)
                                        else -> Color.Transparent
                                    }
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (isGameOver) {
            Text("💥 Game Over! Score: $score", color = Color(0xFFEF4444), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { restartSnake() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ADE80))
            ) {
                Text("Play Again", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        } else {
            // D-Pad Controls
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { if (dir.first == 0) dir = Pair(-1, 0) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Button(
                        onClick = { if (dir.second == 0) dir = Pair(0, -1) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, null, tint = Color.White)
                    }
                    Button(
                        onClick = { if (dir.second == 0) dir = Pair(0, 1) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = { if (dir.first == 0) dir = Pair(1, 0) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White)
                }
            }
        }
    }
}

// ==========================================
// 2. WORD FINDER (HIDDEN WORD SEARCH)
// ==========================================
@Composable
fun WordFinderScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val targetWords = remember { listOf("GOAL", "ATLAS", "LION", "STAR", "APEX", "DICE") }
    val gridRows = remember {
        listOf(
            "GOALXRT",
            "PBATLAS",
            "WLIONKM",
            "STARVQP",
            "ZNAPEXD",
            "DICELJH"
        )
    }

    var currentWord by remember { mutableStateOf("") }
    var foundWords by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Text("Word Finder Grid", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = { foundWords = emptySet(); currentWord = "" }) {
                Icon(Icons.Default.Refresh, null, tint = Color(0xFFFB7185))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Found: ${foundWords.size} / ${targetWords.size}   |   Best: $highScore",
            color = Color(0xFFFB7185),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Target Words Checklist
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            targetWords.forEach { w ->
                val isFound = foundWords.contains(w)
                Text(
                    text = w,
                    color = if (isFound) Color(0xFF22C55E) else Color(0xFF94A3B8),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current Typed Selection
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentWord.isEmpty()) "Tap letters below to spell a word..." else currentWord,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                TextButton(onClick = { currentWord = "" }) {
                    Text("Clear", color = Color(0xFFFB7185))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 6x7 Letter Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            gridRows.forEach { rowStr ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowStr.forEach { ch ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                                .clickable {
                                    val next = currentWord + ch
                                    if (targetWords.contains(next) && !foundWords.contains(next)) {
                                        val updated = foundWords + next
                                        foundWords = updated
                                        currentWord = ""
                                        onSaveScore(updated.size * 150)
                                    } else if (next.length >= 6) {
                                        currentWord = "$ch"
                                    } else {
                                        currentWord = next
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$ch", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        if (foundWords.size == targetWords.size) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("🎉 All 6 Hidden Words Found! (+900 pts)", color = Color(0xFF22C55E), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }
    }
}
