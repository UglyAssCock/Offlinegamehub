package com.example.offlinegamehub

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// =========================================================
// 1. SNAKE CLASSIC (CHECKERED BOARD + EYEBALL SNAKE GRAPHICS)
// =========================================================
@Composable
fun SnakeGameScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val cols = 13
    val rows = 17
    var snake by remember { mutableStateOf(listOf(Pair(8, 6), Pair(8, 5), Pair(8, 4))) }
    var dir by remember { mutableStateOf(Pair(0, 1)) }
    var foods by remember { mutableStateOf(listOf(Pair(5, 3), Pair(6, 10))) }
    var score by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }

    fun restartSnake() {
        snake = listOf(Pair(8, 6), Pair(8, 5), Pair(8, 4))
        dir = Pair(0, 1)
        foods = listOf(Pair(4, 3), Pair(11, 9))
        score = 0
        isGameOver = false
    }

    LaunchedEffect(isGameOver) {
        while (!isGameOver) {
            delay(190L)
            val head = snake.first()
            val nextHead = Pair(head.first + dir.first, head.second + dir.second)

            if (nextHead.first !in 0 until rows ||
                nextHead.second !in 0 until cols ||
                snake.contains(nextHead)
            ) {
                isGameOver = true
                onSaveScore(score)
            } else {
                if (foods.contains(nextHead)) {
                    val grown = listOf(nextHead) + snake
                    snake = grown
                    score++
                    onSaveScore(score)
                    val remaining = foods.filter { it != nextHead }.toMutableList()
                    var newBerry: Pair<Int, Int>
                    do {
                        newBerry = Pair(Random.nextInt(rows), Random.nextInt(cols))
                    } while (grown.contains(newBerry) || remaining.contains(newBerry))
                    remaining.add(newBerry)
                    foods = remaining
                } else {
                    snake = listOf(nextHead) + snake.dropLast(1)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00C2FF))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val (dx, dy) = dragAmount
                    if (abs(dx) > abs(dy)) {
                        if (dx > 12 && dir.second == 0) dir = Pair(0, 1)
                        else if (dx < -12 && dir.second == 0) dir = Pair(0, -1)
                    } else {
                        if (dy > 12 && dir.first == 0) dir = Pair(1, 0)
                        else if (dy < -12 && dir.first == 0) dir = Pair(-1, 0)
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ArcadeSoloHeader(
            leftTitle = "MEDIUM MODE",
            leftValue = "$score",
            rightTitle = "ALL TIME",
            rightValue = "${maxOf(highScore, score)}",
            onBack = onBack,
            onRestart = { restartSnake() }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Navy Outer Frame + Warm Checkered Board
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .weight(1f)
                .background(Color(0xFF142F43))
                .padding(14.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellW = size.width / cols
                val cellH = size.height / rows

                // 1. Checkered Sand Tiles
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val isLight = (r + c) % 2 == 0
                        drawRect(
                            color = if (isLight) Color(0xFFFFE5B4) else Color(0xFFFCD99B),
                            topLeft = Offset(c * cellW, r * cellH),
                            size = Size(cellW + 1f, cellH + 1f)
                        )
                    }
                }

                // 2. Red Berry Foods with Dark Outline & Green Leaf
                foods.forEach { f ->
                    val center = Offset((f.second + 0.5f) * cellW, (f.first + 0.5f) * cellH)
                    val radius = minOf(cellW, cellH) * 0.42f
                    drawCircle(color = Color(0xFF2B2B2B), radius = radius + 3f, center = center)
                    drawCircle(color = Color(0xFFFF5252), radius = radius, center = center)
                    drawCircle(color = Color(0xFF4ADE80), radius = radius * 0.28f, center = center)
                }

                // 3. Snake Body Segments + Cartoon Eyes on Head
                snake.reversed().forEachIndexed { revIdx, seg ->
                    val isHead = revIdx == snake.lastIndex
                    val cx = (seg.second + 0.5f) * cellW
                    val cy = (seg.first + 0.5f) * cellH
                    val r = minOf(cellW, cellH) * (if (isHead) 0.48f else 0.42f)

                    // Outer dark green + inner bright green
                    drawCircle(color = Color(0xFF2E7D32), radius = r + 3f, center = Offset(cx, cy))
                    drawCircle(color = Color(0xFF66BB6A), radius = r, center = Offset(cx, cy))

                    if (!isHead) {
                        drawCircle(color = Color(0xFF81C784), radius = r * 0.45f, center = Offset(cx, cy))
                    } else {
                        // Googly Eyes
                        val eyeOffset = r * 0.45f
                        val e1 = if (dir.first == 0) Offset(cx, cy - eyeOffset) else Offset(cx - eyeOffset, cy)
                        val e2 = if (dir.first == 0) Offset(cx, cy + eyeOffset) else Offset(cx + eyeOffset, cy)

                        drawCircle(color = Color.Black, radius = r * 0.38f, center = e1)
                        drawCircle(color = Color.White, radius = r * 0.28f, center = e1)
                        drawCircle(color = Color.Black, radius = r * 0.14f, center = e1)

                        drawCircle(color = Color.Black, radius = r * 0.38f, center = e2)
                        drawCircle(color = Color.White, radius = r * 0.28f, center = e2)
                        drawCircle(color = Color.Black, radius = r * 0.14f, center = e2)
                    }
                }
            }

            if (isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CRASHED!", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { restartSnake() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ADE80))
                        ) {
                            Text("PLAY AGAIN", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        // Compact Touch Direction Bar at Bottom (Swipe or Tap)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { if (dir.second == 0) dir = Pair(0, -1) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF142F43))) { Text("◀") }
            Button(onClick = { if (dir.first == 0) dir = Pair(-1, 0) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF142F43))) { Text("▲") }
            Button(onClick = { if (dir.first == 0) dir = Pair(1, 0) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF142F43))) { Text("▼") }
            Button(onClick = { if (dir.second == 0) dir = Pair(0, 1) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF142F43))) { Text("▶") }
        }
    }
}

// =========================================================
// 2. WORD FINDER (PURPLE CROSSWORD + CIRCULAR LETTER WHEEL)
// =========================================================
data class CrosswordStage(
    val level: Int,
    val wheelLetters: List<Char>,
    val targetWords: List<String>
)

@Composable
fun WordFinderScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val stages = remember {
        listOf(
            CrosswordStage(1, listOf('B', 'E', 'A', 'L', 'D'), listOf("BALD", "LEAD", "DEAL", "ABLE", "BALE", "BED")),
            CrosswordStage(2, listOf('S', 'P', 'O', 'R', 'T'), listOf("SPORT", "PORTS", "STOP", "SPOT", "TOPS", "POT")),
            CrosswordStage(3, listOf('E', 'A', 'R', 'T', 'H'), listOf("EARTH", "HEART", "HEAT", "HATE", "TEAR", "ART"))
        )
    }

    var stageIdx by remember { mutableIntStateOf(0) }
    val currentStage = stages[stageIdx % stages.size]
    var wheelOrder by remember(stageIdx) { mutableStateOf(currentStage.wheelLetters) }
    var foundWords by remember(stageIdx) { mutableStateOf(setOf<String>()) }
    var currentInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF905596))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header matching Image 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text("<", color = Color(0xFFD9779B), fontSize = 24.sp, fontWeight = FontWeight.Black)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF6B3A70))
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                ) {
                    Text("HARD", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text("Level ${currentStage.level}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable {
                        foundWords = emptySet()
                        currentInput = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("🇬🇧", fontSize = 22.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // White Rounded Crossword Grid Tiles
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            currentStage.targetWords.forEach { word ->
                val isFound = foundWords.contains(word)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    word.forEach { ch ->
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .shadow(2.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isFound) Color(0xFF4ADE80) else Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isFound) {
                                Text(
                                    text = "$ch",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // Current Word Pill Banner
        if (currentInput.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF6B3A70))
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(currentInput, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Bottom Controls + Circular Pink Letter Wheel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            // Left Shuffle Button
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp)
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE06397))
                    .clickable { wheelOrder = wheelOrder.shuffled() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = Color.White)
            }

            // Right Hint Button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 12.dp)
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE06397))
                    .clickable {
                        val remaining = currentStage.targetWords.firstOrNull { !foundWords.contains(it) }
                        if (remaining != null) {
                            foundWords = foundWords + remaining
                            onSaveScore(foundWords.size * 150)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = "Hint", tint = Color.White)
            }

            // Pink Circular Letter Wheel
            Box(
                modifier = Modifier
                    .size(215.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFC486A8))
                    .border(8.dp, Color(0xFFB37497), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val radiusDp = 68f
                wheelOrder.forEachIndexed { idx, letter ->
                    val angle = (2.0 * Math.PI * idx / wheelOrder.size) - (Math.PI / 2.0)
                    val offsetX = (cos(angle) * radiusDp).dp
                    val offsetY = (sin(angle) * radiusDp).dp

                    Box(
                        modifier = Modifier
                            .offset(x = offsetX, y = offsetY)
                            .size(50.dp)
                            .clip(CircleShape)
                            .clickable {
                                val next = currentInput + letter
                                if (currentStage.targetWords.contains(next) && !foundWords.contains(next)) {
                                    val updated = foundWords + next
                                    foundWords = updated
                                    currentInput = ""
                                    onSaveScore(updated.size * 200)
                                    if (updated.size == currentStage.targetWords.size) {
                                        stageIdx++
                                    }
                                } else if (next.length > 5) {
                                    currentInput = "$letter"
                                } else {
                                    currentInput = next
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$letter",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2D232E)
                        )
                    }
                }

                // Tap center of wheel to clear letters
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { currentInput = "" },
                    contentAlignment = Alignment.Center
                ) {
                    if (currentInput.isNotEmpty()) {
                        Text("✕", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
