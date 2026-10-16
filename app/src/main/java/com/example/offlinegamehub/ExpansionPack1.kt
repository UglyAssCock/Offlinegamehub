package com.example.offlinegamehub

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun ModeSelectorBar(
    vsBot: Boolean,
    accentColor: Color,
    onModeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = { onModeChange(true) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (vsBot) accentColor else Color(0xFF1E293B)
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text("🤖 Vs Smart Bot", color = if (vsBot) Color.Black else Color.White, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = { onModeChange(false) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!vsBot) accentColor else Color(0xFF1E293B)
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text("👥 Vs Friend (2P)", color = if (!vsBot) Color.Black else Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 1. TIC TAC TOE (VS BOT & VS FRIEND)
// ==========================================
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
    var winner by remember { mutableStateOf<String?>(null) }
    var p1Wins by remember { mutableIntStateOf(0) }
    var p2Wins by remember { mutableIntStateOf(0) }

    val winLines = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
        listOf(0, 4, 8), listOf(2, 4, 6)
    )

    fun checkWinner(b: List<String>): String? {
        for (line in winLines) {
            val a = b[line[0]]
            if (a.isNotEmpty() && a == b[line[1]] && a == b[line[2]]) return a
        }
        return if (b.none { it.isEmpty() }) "DRAW" else null
    }

    fun resetBoard() {
        board = List(9) { "" }
        isXTurn = true
        winner = null
    }

    LaunchedEffect(isXTurn, vsBot, winner) {
        if (vsBot && !isXTurn && winner == null) {
            delay(450L)
            val emptyIndices = board.indices.filter { board[it].isEmpty() }
            if (emptyIndices.isNotEmpty()) {
                // Find winning move for O, or block X, or take center
                var chosen: Int? = null
                for (idx in emptyIndices) {
                    val test = board.toMutableList().apply { this[idx] = "O" }
                    if (checkWinner(test) == "O") { chosen = idx; break }
                }
                if (chosen == null) {
                    for (idx in emptyIndices) {
                        val test = board.toMutableList().apply { this[idx] = "X" }
                        if (checkWinner(test) == "X") { chosen = idx; break }
                    }
                }
                if (chosen == null && 4 in emptyIndices) chosen = 4
                val move = chosen ?: emptyIndices.random()

                val updated = board.toMutableList().apply { this[move] = "O" }
                board = updated
                val w = checkWinner(updated)
                if (w != null) {
                    winner = w
                    if (w == "O") p2Wins++
                } else {
                    isXTurn = true
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
            Text("Tic Tac Toe", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = { resetBoard() }) { Icon(Icons.Default.Refresh, null, tint = Color(0xFFEF4444)) }
        }

        ModeSelectorBar(vsBot = vsBot, accentColor = Color(0xFFEF4444)) {
            vsBot = it
            p1Wins = 0
            p2Wins = 0
            resetBoard()
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Player X: $p1Wins", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                Text(if (vsBot) "Bot O: $p2Wins" else "Friend O: $p2Wins", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                Text("Best: $highScore", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when (winner) {
                "X" -> "🎉 Player X Wins!"
                "O" -> if (vsBot) "🤖 Bot Wins!" else "🎉 Friend O Wins!"
                "DRAW" -> "🤝 It's a Draw!"
                else -> if (isXTurn) "Turn: Player X" else if (vsBot) "Bot is thinking..." else "Turn: Friend O"
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.size(300.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (r in 0..2) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (c in 0..2) {
                        val idx = r * 3 + c
                        val cell = board[idx]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1E293B))
                                .clickable(enabled = cell.isEmpty() && winner == null && (!vsBot || isXTurn)) {
                                    val sym = if (isXTurn) "X" else "O"
                                    val updated = board.toMutableList().apply { this[idx] = sym }
                                    board = updated
                                    val w = checkWinner(updated)
                                    if (w != null) {
                                        winner = w
                                        if (w == "X") {
                                            p1Wins++
                                            onSaveScore(p1Wins * 150)
                                        } else if (w == "O") {
                                            p2Wins++
                                        }
                                    } else {
                                        isXTurn = !isXTurn
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cell,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (cell == "X") Color(0xFF38BDF8) else Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }

        if (winner != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { resetBoard() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Text("Next Round", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 2. DOTS AND BOXES (VS BOT & VS FRIEND)
// ==========================================
@Composable
fun DotsAndBoxesScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val gridSize = 3 // 3x3 boxes (4x4 dots)
    var vsBot by remember { mutableStateOf(true) }
    var hLines by remember { mutableStateOf(BooleanArray(12)) } // 4 rows of 3 horizontal edges
    var vLines by remember { mutableStateOf(BooleanArray(12)) } // 3 rows of 4 vertical edges
    var boxOwners by remember { mutableStateOf(IntArray(9)) }   // 0 = none, 1 = P1, 2 = P2/Bot
    var currentPlayer by remember { mutableIntStateOf(1) }

    val p1Score = boxOwners.count { it == 1 }
    val p2Score = boxOwners.count { it == 2 }
    val isGameOver = boxOwners.all { it != 0 }

    fun resetGame() {
        hLines = BooleanArray(12)
        vLines = BooleanArray(12)
        boxOwners = IntArray(9)
        currentPlayer = 1
    }

    fun applyMove(isHorizontal: Boolean, index: Int, player: Int) {
        val newH = hLines.clone()
        val newV = vLines.clone()
        if (isHorizontal) newH[index] = true else newV[index] = true

        val newOwners = boxOwners.clone()
        var scoredBox = false
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val boxIdx = r * gridSize + c
                if (newOwners[boxIdx] == 0) {
                    val top = newH[r * 3 + c]
                    val bottom = newH[(r + 1) * 3 + c]
                    val left = newV[r * 4 + c]
                    val right = newV[r * 4 + (c + 1)]
                    if (top && bottom && left && right) {
                        newOwners[boxIdx] = player
                        scoredBox = true
                    }
                }
            }
        }
        hLines = newH
        vLines = newV
        boxOwners = newOwners

        if (newOwners.all { it != 0 }) {
            val p1Total = newOwners.count { it == 1 }
            if (p1Total > newOwners.count { it == 2 }) {
                onSaveScore(p1Total * 120)
            }
        } else if (!scoredBox) {
            currentPlayer = if (player == 1) 2 else 1
        }
    }

    LaunchedEffect(currentPlayer, vsBot, hLines, vLines, isGameOver) {
        if (vsBot && currentPlayer == 2 && !isGameOver) {
            delay(450L)
            val availableH = hLines.indices.filter { !hLines[it] }.map { true to it }
            val availableV = vLines.indices.filter { !vLines[it] }.map { false to it }
            val allMoves = availableH + availableV
            if (allMoves.isNotEmpty()) {
                val chosen = allMoves.random()
                applyMove(chosen.first, chosen.second, 2)
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
            Text("Dots & Boxes", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = { resetGame() }) { Icon(Icons.Default.Refresh, null, tint = Color(0xFF06B6D4)) }
        }

        ModeSelectorBar(vsBot = vsBot, accentColor = Color(0xFF06B6D4)) {
            vsBot = it
            resetGame()
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("P1 (Blue): $p1Score", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                Text(if (vsBot) "Bot (Amber): $p2Score" else "P2 (Amber): $p2Score", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (isGameOver) {
                if (p1Score > p2Score) "🎉 Player 1 Wins!" else if (p2Score > p1Score) "🏆 Player 2 / Bot Wins!" else "Draw!"
            } else {
                if (currentPlayer == 1) "Your Turn (Tap any gray line)" else if (vsBot) "Bot is drawing a line..." else "Friend's Turn (Tap a line)"
            },
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Render 4x4 Dots & Interactive Lines
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            for (r in 0..3) {
                // Horizontal row of dots and horizontal edges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (c in 0..3) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        if (c < 3) {
                            val hIdx = r * 3 + c
                            val taken = hLines[hIdx]
                            Box(
                                modifier = Modifier
                                    .width(68.dp)
                                    .height(14.dp)
                                    .background(if (taken) Color(0xFF06B6D4) else Color(0xFF334155))
                                    .clickable(enabled = !taken && (!vsBot || currentPlayer == 1)) {
                                        applyMove(true, hIdx, currentPlayer)
                                    }
                            )
                        }
                    }
                }
                // Vertical edges + Boxes row
                if (r < 3) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        for (c in 0..3) {
                            val vIdx = r * 4 + c
                            val taken = vLines[vIdx]
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .height(68.dp)
                                    .background(if (taken) Color(0xFF06B6D4) else Color(0xFF334155))
                                    .clickable(enabled = !taken && (!vsBot || currentPlayer == 1)) {
                                        applyMove(false, vIdx, currentPlayer)
                                    }
                            )
                            if (c < 3) {
                                val owner = boxOwners[r * 3 + c]
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .background(
                                            when (owner) {
                                                1 -> Color(0xFF38BDF8).copy(alpha = 0.35f)
                                                2 -> Color(0xFFF59E0B).copy(alpha = 0.35f)
                                                else -> Color.Transparent
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (owner != 0) {
                                        Text(
                                            text = if (owner == 1) "P1" else if (vsBot) "BOT" else "P2",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. WORDLE 5-LETTER WORD GUESS
// ==========================================
@Composable
fun WordleGameScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val wordList = remember {
        listOf(
            "WORLD", "SPORT", "SCORE", "BRAIN", "SMART", "MATCH", "PLANT", "OCEAN",
            "RIVER", "MOUNT", "TIGER", "EAGLE", "FLAME", "CROWN", "QUEST", "ATLAS"
        )
    }
    var secretWord by remember { mutableStateOf(wordList.random()) }
    var guesses by remember { mutableStateOf(listOf<String>()) }
    var currentGuess by remember { mutableStateOf("") }
    var gameStatus by remember { mutableStateOf("PLAYING") } // PLAYING, WON, LOST

    fun startNewWord() {
        secretWord = wordList.random()
        guesses = emptyList()
        currentGuess = ""
        gameStatus = "PLAYING"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Text("Wordle 5-Letter", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = { startNewWord() }) { Icon(Icons.Default.Refresh, null, tint = Color(0xFF22C55E)) }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 6 Rows of 5 Letter Tiles
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
                        val bgColor = if (!isSubmitted) {
                            Color(0xFF1E293B)
                        } else if (secretWord[col] == ch) {
                            Color(0xFF16A34A) // Green exact match
                        } else if (secretWord.contains(ch)) {
                            Color(0xFFD97706) // Yellow wrong spot
                        } else {
                            Color(0xFF334155) // Gray
                        }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .border(1.dp, Color(0xFF475569), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (ch == ' ') "" else "$ch",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (gameStatus != "PLAYING") {
            Text(
                text = if (gameStatus == "WON") "🎉 Correct! The word was $secretWord" else "Word was: $secretWord",
                color = Color(0xFF22C55E),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { startNewWord() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
            ) {
                Text("Next Word", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        } else {
            // On-screen QWERTY Keyboard
            val rows = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM")
            rows.forEach { rowKeys ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 3.dp)
                ) {
                    rowKeys.forEach { k ->
                        Box(
                            modifier = Modifier
                                .size(width = 31.dp, height = 42.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E293B))
                                .clickable {
                                    if (currentGuess.length < 5) currentGuess += k
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$k", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (currentGuess.isNotEmpty()) currentGuess = currentGuess.dropLast(1)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("⌫ Delete", color = Color.White)
                }
                Button(
                    onClick = {
                        if (currentGuess.length == 5) {
                            val updated = guesses + currentGuess
                            guesses = updated
                            if (currentGuess == secretWord) {
                                gameStatus = "WON"
                                val pts = (7 - updated.size) * 200
                                onSaveScore(pts)
                            } else if (updated.size >= 6) {
                                gameStatus = "LOST"
                            }
                            currentGuess = ""
                        }
                    },
                    enabled = currentGuess.length == 5,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Submit Guess", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 4. NUMBER SLIDE (4x4 15-PUZZLE)
// ==========================================
@Composable
fun NumberSlideScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var tiles by remember { mutableStateOf((1..15).toList() + listOf(0)) }
    var moves by remember { mutableIntStateOf(0) }
    var isSolved by remember { mutableStateOf(false) }

    fun scrambleBoard() {
        val current = ((1..15).toList() + listOf(0)).toMutableList()
        var zeroIdx = 15
        repeat(80) {
            val r = zeroIdx / 4
            val c = zeroIdx % 4
            val neighbors = mutableListOf<Int>()
            if (r > 0) neighbors.add((r - 1) * 4 + c)
            if (r < 3) neighbors.add((r + 1) * 4 + c)
            if (c > 0) neighbors.add(r * 4 + (c - 1))
            if (c < 3) neighbors.add(r * 4 + (c + 1))
            val pick = neighbors.random()
            current[zeroIdx] = current[pick]
            current[pick] = 0
            zeroIdx = pick
        }
        tiles = current
        moves = 0
        isSolved = false
    }

    LaunchedEffect(Unit) { scrambleBoard() }

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
            Text("Number Slide 15", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = { scrambleBoard() }) { Icon(Icons.Default.Refresh, null, tint = Color(0xFFA855F7)) }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text("Moves: $moves   \vert{}   Best Score:$highScore", color = Color(0xFFA855F7), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.size(310.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (r in 0..3) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (c in 0..3) {
                        val idx = r * 4 + c
                        val num = tiles[idx]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    when {
                                        num == 0 -> Color(0xFF0F172A)
                                        num == idx + 1 -> Color(0xFF065F46)
                                        else -> Color(0xFF1E293B)
                                    }
                                )
                                .clickable(enabled = num != 0 && !isSolved) {
                                    val zeroIdx = tiles.indexOf(0)
                                    val zr = zeroIdx / 4
                                    val zc = zeroIdx % 4
                                    if (abs(r - zr) + abs(c - zc) == 1) {
                                        val updated = tiles.toMutableList()
                                        updated[zeroIdx] = num
                                        updated[idx] = 0
                                        tiles = updated
                                        moves++
                                        if (updated.take(15) == (1..15).toList()) {
                                            isSolved = true
                                            onSaveScore(maxOf(100, 1500 - moves * 10))
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (num != 0) {
                                Text("$num", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        if (isSolved) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("🎉 Puzzle Solved in $moves moves!", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

// ==========================================
// 5. DARTS 301 (VS BOT & VS FRIEND)
// ==========================================
@Composable
fun DartsGameScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var vsBot by remember { mutableStateOf(true) }
    var p1Remaining by remember { mutableIntStateOf(301) }
    var p2Remaining by remember { mutableIntStateOf(301) }
    var isP1Turn by remember { mutableStateOf(true) }
    var aimPos by remember { mutableFloatStateOf(0.5f) }
    var lastShotText by remember { mutableStateOf("Tap THROW when the bar hits center!") }

    LaunchedEffect(Unit) {
        var dir = 1f
        while (true) {
            delay(18L)
            aimPos += 0.032f * dir
            if (aimPos >= 1f) { aimPos = 1f; dir = -1f }
            if (aimPos <= 0f) { aimPos = 0f; dir = 1f }
        }
    }

    LaunchedEffect(isP1Turn, vsBot, p1Remaining, p2Remaining) {
        if (vsBot && !isP1Turn && p1Remaining > 0 && p2Remaining > 0) {
            delay(800L)
            val botHit = listOf(20, 25, 40, 50, 60).random()
            val nextScore = if (p2Remaining - botHit >= 0) p2Remaining - botHit else p2Remaining
            p2Remaining = nextScore
            lastShotText = "🤖 Bot threw $botHit pts!"
            if (nextScore > 0) isP1Turn = true
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
            Text("Darts 301 Showdown", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = { p1Remaining = 301; p2Remaining = 301; isP1Turn = true }) {
                Icon(Icons.Default.Refresh, null, tint = Color(0xFFF97316))
            }
