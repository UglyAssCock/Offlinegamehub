package com.example.offlinegamehub

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.random.Random

// =========================================================
// 1. DARTS 301 (20-SLICE RADIAL BOARD + X/Y TIMING BARS)
// =========================================================
@Composable
fun DartsGameScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var vsBot by remember { mutableStateOf(true) }
    var youScore by remember { mutableIntStateOf(301) }
    var botScore by remember { mutableIntStateOf(301) }
    var isPlayerTurn by remember { mutableStateOf(true) }

    // 0 = aiming vertical (Y), 1 = aiming horizontal (X), 2 = dart landed
    var aimPhase by remember { mutableIntStateOf(0) }
    var aimY by remember { mutableFloatStateOf(0.5f) }
    var aimX by remember { mutableFloatStateOf(0.5f) }
    var lastHitOffset by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var statusText by remember { mutableStateOf("Tap screen to lock Vertical aim!") }

    LaunchedEffect(aimPhase, isPlayerTurn) {
        var dir = 1f
        while (isPlayerTurn && (aimPhase == 0 || aimPhase == 1)) {
            delay(16L)
            if (aimPhase == 0) {
                aimY += 0.032f * dir
                if (aimY >= 1f) { aimY = 1f; dir = -1f }
                if (aimY <= 0f) { aimY = 0f; dir = 1f }
            } else {
                aimX += 0.035f * dir
                if (aimX >= 1f) { aimX = 1f; dir = -1f }
                if (aimX <= 0f) { aimX = 0f; dir = 1f }
            }
        }
    }

    LaunchedEffect(isPlayerTurn, vsBot, youScore, botScore) {
        if (vsBot && !isPlayerTurn && youScore > 0 && botScore > 0) {
            statusText = "Bot is aiming..."
            delay(850L)
            val bx = Random.nextFloat() * 0.4f + 0.3f
            val by = Random.nextFloat() * 0.4f + 0.3f
            lastHitOffset = Pair(bx, by)
            val pts = listOf(20, 25, 40, 50, 60).random()
            if (botScore - pts >= 0) botScore -= pts
            statusText = "Bot scored $pts!"
            delay(650L)
            if (botScore > 0) {
                isPlayerTurn = true
                aimPhase = 0
                statusText = "Your Turn! Tap to lock Vertical aim."
            }
        }
    }

    fun resetDarts() {
        youScore = 301
        botScore = 301
        isPlayerTurn = true
        aimPhase = 0
        lastHitOffset = null
        statusText = "Tap screen to lock Vertical aim!"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF5FA8D3))
            .clickable(enabled = isPlayerTurn && youScore > 0 && botScore > 0) {
                if (aimPhase == 0) {
                    aimPhase = 1
                    statusText = "Now tap to lock Horizontal aim & THROW!"
                } else if (aimPhase == 1) {
                    aimPhase = 2
                    lastHitOffset = Pair(aimX, aimY)
                    val dist = hypot((aimX - 0.5f).toDouble(), (aimY - 0.5f).toDouble()).toFloat()
                    val pts = when {
                        dist < 0.07f -> 50
                        dist < 0.16f -> 60
                        dist < 0.28f -> 40
                        dist < 0.42f -> 20
                        else -> 10
                    }
                    if (youScore - pts >= 0) youScore -= pts
                    statusText = "Bullseye zone! You scored $pts pts!"
                    if (youScore == 0) {
                        onSaveScore(500)
                        statusText = "YOU WIN 301!"
                    } else {
                        isPlayerTurn = false
                    }
                } else if (!vsBot) {
                    isPlayerTurn = true
                    aimPhase = 0
                }
            }
    ) {
        // Striped Wallpaper + Wooden Floor Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stripeW = size.width / 11f
            for (i in 0..10) {
                if (i % 2 == 0) {
                    drawRect(
                        color = Color(0xFF539DC8),
                        topLeft = Offset(i * stripeW, 0f),
                        size = Size(stripeW, size.height * 0.82f)
                    )
                }
            }
            drawRect(
                color = Color(0xFF9C5233),
                topLeft = Offset(0f, size.height * 0.82f),
                size = Size(size.width, size.height * 0.18f)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArcadeVsHeader(
                youScore = youScore,
                opponentScore = botScore,
                vsBot = vsBot,
                onToggleMode = { vsBot = !vsBot; resetDarts() },
                onBack = onBack,
                onRestart = { resetDarts() }
            )

            // Cyan Dart Icon at Top
            Canvas(modifier = Modifier.size(68.dp)) {
                drawCircle(color = Color(0xFF00D2FF), radius = size.minDimension * 0.35f)
                drawCircle(color = Color.Black, radius = size.minDimension * 0.35f, style = Stroke(5f))
                drawCircle(color = Color.White, radius = size.minDimension * 0.12f)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Middle Row: Left Vertical Aim Slider + 20-Slice Radial Dartboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Left Vertical Aim Bar
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(230.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color(0xFF3E3A3F))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = (aimY * 220f).dp)
                            .background(Color.White)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // 20-Slice Radial Vector Dartboard
                Box(
                    modifier = Modifier.size(275.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val r = size.minDimension / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)

                        // Outer Black Number Ring
                        drawCircle(color = Color(0xFF111315), radius = r, center = center)

                        val sliceAngle = 360f / 20f
                        for (i in 0 until 20) {
                            val startAngle = -99f + i * sliceAngle
                            val isEven = i % 2 == 0
                            val mainColor = if (isEven) Color(0xFF111315) else Color(0xFFFFF8E7)
                            val ringColor = if (isEven) Color(0xFFE53935) else Color(0xFF2E7D32)

                            // Double Ring (Outer Red/Green)
                            drawArc(
                                color = ringColor,
                                startAngle = startAngle,
                                sweepAngle = sliceAngle,
                                useCenter = true,
                                topLeft = Offset(center.x - r * 0.78f, center.y - r * 0.78f),
                                size = Size(r * 1.56f, r * 1.56f)
                            )
                            // Outer Single Wedge
                            drawArc(
                                color = mainColor,
                                startAngle = startAngle,
                                sweepAngle = sliceAngle,
                                useCenter = true,
                                topLeft = Offset(center.x - r * 0.70f, center.y - r * 0.70f),
                                size = Size(r * 1.40f, r * 1.40f)
                            )
                            // Triple Ring (Inner Red/Green)
                            drawArc(
                                color = ringColor,
                                startAngle = startAngle,
                                sweepAngle = sliceAngle,
                                useCenter = true,
                                topLeft = Offset(center.x - r * 0.48f, center.y - r * 0.48f),
                                size = Size(r * 0.96f, r * 0.96f)
                            )
                            // Inner Single Wedge
                            drawArc(
                                color = mainColor,
                                startAngle = startAngle,
                                sweepAngle = sliceAngle,
                                useCenter = true,
                                topLeft = Offset(center.x - r * 0.40f, center.y - r * 0.40f),
                                size = Size(r * 0.80f, r * 0.80f)
                            )
                        }

                        // Bullseye Rings
                        drawCircle(color = Color.White, radius = r * 0.14f, center = center)
                        drawCircle(color = Color(0xFFE53935), radius = r * 0.07f, center = center)

                        // Landed Dart Marker
                        lastHitOffset?.let { (hx, hy) ->
                            val hitPt = Offset(
                                center.x + (hx - 0.5f) * r * 1.4f,
                                center.y + (hy - 0.5f) * r * 1.4f
                            )
                            drawCircle(color = Color(0xFF38BDF8), radius = 14f, center = hitPt)
                            drawCircle(color = Color.White, radius = 6f, center = hitPt)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Bottom Horizontal Aim Slider
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.55f))
            ) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .offset(x = (aimX * 230f).dp)
                        .background(Color.Black)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = statusText,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }
    }
}

// =========================================================
// 2. WORDLE (CREAM BACKGROUND + GREEN/CHARCOAL TILES)
// =========================================================
@Composable
fun WordleGameScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val wordList = remember {
        listOf(
            "CROPS", "CROWD", "DRIVE", "WORLD", "SPORT", "SCORE", "BRAIN",
            "SMART", "MATCH", "PLANT", "OCEAN", "TIGER", "FLAME", "ATLAS"
        )
    }
    var secretWord by remember { mutableStateOf(wordList.random()) }
    var guesses by remember { mutableStateOf(listOf<String>()) }
    var currentGuess by remember { mutableStateOf("") }
    var streak by remember { mutableIntStateOf(0) }

    fun nextWord() {
        secretWord = wordList.random()
        guesses = emptyList()
        currentGuess = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF0D5))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ArcadeSoloHeader(
            leftTitle = "CURRENT STREAK",
            leftValue = "$streak",
            rightTitle = "ALL TIME",
            rightValue = "${maxOf(highScore, streak)}",
            onBack = onBack,
            onRestart = { nextWord() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 6x5 Wordle Tile Grid matching Image 2
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (row in 0..5) {
                val word = when {
                    row < guesses.size -> guesses[row]
                    row == guesses.size -> currentGuess.padEnd(5, ' ')
                    else -> "     "
                }
                val isSubmitted = row < guesses.size

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (col in 0..4) {
                        val ch = word[col]
                        val bgColor = when {
                            !isSubmitted -> Color.White
                            secretWord[col] == ch -> Color(0xFF22B549)
                            secretWord.contains(ch) -> Color(0xFFF59E0B)
                            else -> Color(0xFF54585A)
                        }

                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (ch == ' ') "" else "$ch",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSubmitted) Color.White else Color(0xFF2B2B2B)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Colored QWERTY Keyboard matching Image 2
        val allGuessedChars = guesses.joinToString("")
        val rows = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM")
        rows.forEachIndexed { rIdx, rowKeys ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(vertical = 3.dp)
            ) {
                rowKeys.forEach { k ->
                    val isGreen = guesses.any { g -> g.indices.any { i -> g[i] == k && secretWord[i] == k } }
                    val isGray = allGuessedChars.contains(k) && !secretWord.contains(k)
                    val keyBg = when {
                        isGreen -> Color(0xFF22B549)
                        isGray -> Color(0xFF54585A)
                        else -> Color(0xFFEED7B5)
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 32.dp, height = 46.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(keyBg)
                            .clickable {
                                if (currentGuess.length < 5 && guesses.size < 6) currentGuess += k
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$k",
                            color = if (isGreen || isGray) Color.White else Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                }
                if (rIdx == 2) {
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 46.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEED7B5))
                            .clickable {
                                if (currentGuess.isNotEmpty()) currentGuess = currentGuess.dropLast(1)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⌫", color = Color(0xFF54585A), fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Submit Pill + Pink Hint Circle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (currentGuess.length == 5) {
                        val next = guesses + currentGuess
                        guesses = next
                        if (currentGuess == secretWord) {
                            streak++
                            onSaveScore(streak)
                            nextWord()
                        } else if (next.size >= 6) {
                            streak = 0
                            nextWord()
                        } else {
                            currentGuess = ""
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5B1B8)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text("SUBMIT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE06397))
                    .clickable {
                        if (currentGuess.length < 5) {
                            currentGuess += secretWord[currentGuess.length]
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = "Hint", tint = Color.White)
            }
        }
    }
}

// =========================================================
// 3. 2048 MERGE BLOCK DROP (IMAGE 5 STYLE)
// =========================================================
@Composable
fun NumberSlideScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var columns by remember {
        mutableStateOf(
            listOf(
                emptyList(),
                emptyList(),
                listOf(64, 32, 16),
                emptyList(),
                listOf(8)
            )
        )
    }
    var nextBlock by remember { mutableIntStateOf(16) }
    var score by remember { mutableIntStateOf(96) }

    fun tileColor(v: Int): Color = when (v) {
        2, 4 -> Color(0xFFA855F7)
        8 -> Color(0xFFFFC107)
        16 -> Color(0xFF7ED957)
        32 -> Color(0xFF5CE1E6)
        64 -> Color(0xFFFF9F68)
        128 -> Color(0xFFFF5757)
        else -> Color(0xFF38BDF8)
    }

    fun dropInColumn(colIdx: Int) {
        val col = columns[colIdx].toMutableList()
        if (col.size >= 7) return
        col.add(nextBlock)
        while (col.size >= 2 && col[col.lastIndex] == col[col.lastIndex - 1]) {
            val merged = col.removeAt(col.lastIndex) * 2
            col[col.lastIndex] = merged
            score += merged
            onSaveScore(score)
        }
        val updated = columns.toMutableList()
        updated[colIdx] = col
        columns = updated
        nextBlock = listOf(4, 8, 16, 32, 64).random()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3C434A))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ArcadeSoloHeader(
            leftTitle = "HARD MODE",
            leftValue = "$score",
            rightTitle = "ALL TIME",
            rightValue = "${maxOf(highScore, score)}",
            onBack = onBack,
            onRestart = {
                columns = List(5) { emptyList() }
                score = 0
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Next Dropping Block Preview above Board
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tileColor(nextBlock)),
            contentAlignment = Alignment.Center
        ) {
            Text("$nextBlock", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 5-Column Dark Merge Arena
        Row(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .height(380.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2B3035))
                .border(4.dp, Color(0xFF202428), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (c in 0..4) {
                val colStack = columns[c]
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (c % 2 == 0) Color(0xFF32373D) else Color(0xFF2B3035))
                        .clickable { dropInColumn(c) },
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    colStack.reversed().forEach { num ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(tileColor(num)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$num",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================
// 4. TIC TAC TOE & DOTS AND BOXES & ARCHERY (ARCADE STYLED)
// =========================================================
@Composable
fun TicTacToeScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var vsBot by remember { mutableStateOf(true) }
    var board by remember { mutableStateOf(List(9) { "" }) }
    var isXTurn by remember { mutableStateOf(true) }
    var p1Wins by remember { mutableIntStateOf(0) }
    var p2Wins by remember { mutableIntStateOf(0) }

    val lines = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
        listOf(0, 4, 8), listOf(2, 4, 6)
    )
    fun checkW(b: List<String>): String? {
        for (l in lines) if (b[l[0]].isNotEmpty() && b[l[0]] == b[l[1]] && b[l[0]] == b[l[2]]) return b[l[0]]
        return if (b.none { it.isEmpty() }) "DRAW" else null
    }

    LaunchedEffect(isXTurn, vsBot, board) {
        if (vsBot && !isXTurn && checkW(board) == null) {
            delay(400L)
            val empty = board.indices.filter { board[it].isEmpty() }
            if (empty.isNotEmpty()) {
                val pick = empty.random()
                val next = board.toMutableList().apply { this[pick] = "O" }
                board = next
                if (checkW(next) == "O") p2Wins++ else isXTurn = true
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF48A9D6)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ArcadeVsHeader(p1Wins, p2Wins, vsBot, onToggleMode = { vsBot = !vsBot }, onBack = onBack, onRestart = { board = List(9) { "" }; isXTurn = true })
        Spacer(modifier = Modifier.height(30.dp))
        Column(modifier = Modifier.size(300.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (r in 0..2) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (c in 0..2) {
                        val idx = r * 3 + c
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .clickable(enabled = board[idx].isEmpty() && checkW(board) == null) {
                                    val sym = if (isXTurn) "X" else "O"
                                    val next = board.toMutableList().apply { this[idx] = sym }
                                    board = next
                                    val w = checkW(next)
                                    if (w == "X") { p1Wins++; onSaveScore(p1Wins * 100) }
                                    else if (w == "O") p2Wins++
                                    else isXTurn = !isXTurn
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(board[idx], fontSize = 46.sp, fontWeight = FontWeight.Black, color = if (board[idx] == "X") Color(0xFFFF5A52) else Color(0xFF00CFE8))
                        }
                    }
                }
            }
        }
        if (checkW(board) != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { board = List(9) { "" }; isXTurn = true }) { Text("NEXT ROUND") }
        }
    }
}

@Composable
fun DotsAndBoxesScreen(highScore: Int, onSaveScore: (Int) -> Unit, onBack: () -> Unit) {
    BackHandler { onBack() }
    var vsBot by remember { mutableStateOf(true) }
    var boxOwners by remember { mutableStateOf(IntArray(9)) }
    var isP1 by remember { mutableStateOf(true) }

    LaunchedEffect(isP1, vsBot, boxOwners) {
        if (vsBot && !isP1 && boxOwners.any { it == 0 }) {
            delay(450L)
            val pick = boxOwners.indices.filter { boxOwners[it] == 0 }.random()
            boxOwners = boxOwners.clone().apply { this[pick] = 2 }
            isP1 = true
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF39C7D)), horizontalAlignment = Alignment.CenterHorizontally) {
        ArcadeVsHeader(
            youScore = boxOwners.count { it == 1 },
            opponentScore = boxOwners.count { it == 2 },
            vsBot = vsBot,
            onToggleMode = { vsBot = !vsBot; boxOwners = IntArray(9) },
            onBack = onBack,
            onRestart = { boxOwners = IntArray(9); isP1 = true }
        )
        Spacer(modifier = Modifier.height(30.dp))
        Column(modifier = Modifier.size(300.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (r in 0..2) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (c in 0..2) {
                        val idx = r * 3 + c
                        val owner = boxOwners[idx]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(14.dp))
                                .background(when (owner) { 1 -> Color(0xFFFF6B6B); 2 -> Color(0xFF38BDF8); else -> Color(0xFFFFF8E7) })
                                .border(3.dp, Color(0xFF2B2B2B), RoundedCornerShape(14.dp))
                                .clickable(enabled = owner == 0 && (isP1 || !vsBot)) {
                                    val next = boxOwners.clone().apply { this[idx] = if (isP1) 1 else 2 }
                                    boxOwners = next
                                    onSaveScore(next.count { it == 1 } * 100)
                                    isP1 = !isP1
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(when (owner) { 1 -> "YOU"; 2 -> if (vsBot) "BOT" else "P2"; else -> "+" }, fontWeight = FontWeight.Black, color = Color(0xFF2B2B2B))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArcheryGameScreen(highScore: Int, onSaveScore: (Int) -> Unit, onBack: () -> Unit) {
    DartsGameScreen(highScore = highScore, onSaveScore = onSaveScore, onBack = onBack)
}
