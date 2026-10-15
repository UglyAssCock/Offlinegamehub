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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
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
fun SudokuGameScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var difficulty by remember { mutableIntStateOf(1) } // 0 = Easy, 1 = Medium, 2 = Hard
    var solution by remember { mutableStateOf(IntArray(81)) }
    var board by remember { mutableStateOf(IntArray(81)) }
    var fixedCells by remember { mutableStateOf(BooleanArray(81)) }
    var selectedCell by remember { mutableStateOf<Int?>(null) }
    var mistakes by remember { mutableIntStateOf(0) }
    var hintsLeft by remember { mutableIntStateOf(3) }
    var isWon by remember { mutableStateOf(false) }
    var finalScore by remember { mutableIntStateOf(0) }

    fun generateSudoku(diff: Int) {
        val base = intArrayOf(
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            4, 5, 6, 7, 8, 9, 1, 2, 3,
            7, 8, 9, 1, 2, 3, 4, 5, 6,
            2, 3, 1, 5, 6, 4, 8, 9, 7,
            5, 6, 4, 8, 9, 7, 2, 3, 1,
            8, 9, 7, 2, 3, 1, 5, 6, 4,
            3, 1, 2, 6, 4, 5, 9, 7, 8,
            6, 4, 5, 9, 7, 8, 3, 1, 2,
            9, 7, 8, 3, 1, 2, 6, 4, 5
        )

        val numMap = (1..9).shuffled()
        val solved = IntArray(81) { i -> numMap[base[i] - 1] }

        // Shuffle rows within each 3-row band
        for (band in 0..2) {
            val rows = (0..2).shuffled()
            val copy = solved.clone()
            for (r in 0..2) {
                for (c in 0..8) {
                    solved[(band * 3 + r) * 9 + c] = copy[(band * 3 + rows[r]) * 9 + c]
                }
            }
        }

        val blanksCount = when (diff) {
            0 -> 32
            1 -> 42
            else -> 50
        }

        val puzzle = solved.clone()
        val fixed = BooleanArray(81) { true }
        val indicesToBlank = (0..80).shuffled().take(blanksCount)
        for (idx in indicesToBlank) {
            puzzle[idx] = 0
            fixed[idx] = false
        }

        solution = solved
        board = puzzle
        fixedCells = fixed
        selectedCell = null
        mistakes = 0
        hintsLeft = 3
        isWon = false
        finalScore = 0
    }

    LaunchedEffect(Unit) {
        generateSudoku(difficulty)
    }

    fun checkCompletion(currentBoard: IntArray) {
        if (currentBoard.indices.all { currentBoard[it] == solution[it] }) {
            val baseScore = when (difficulty) {
                0 -> 1000
                1 -> 1500
                else -> 2200
            }
            finalScore = maxOf(100, baseScore - (mistakes * 150))
            onSaveScore(finalScore)
            isWon = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
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
                text = "Sudoku 9x9",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            IconButton(onClick = { generateSudoku(difficulty) }) {
                Icon(Icons.Default.Refresh, contentDescription = "New Board", tint = Color(0xFF3B82F6))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Easy" to 0, "Medium" to 1, "Hard" to 2).forEach { (label, level) ->
                Button(
                    onClick = {
                        difficulty = level
                        generateSudoku(level)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (difficulty == level) Color(0xFF3B82F6) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text(label, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Mistakes: $mistakes", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Hints: $hintsLeft", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Best: ${maxOf(highScore, finalScore)}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isWon) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = "Win", tint = Color(0xFFF59E0B), modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sudoku Solved!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("Score: $finalScore pts", fontSize = 18.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { generateSudoku(difficulty) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Play Another Grid", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (board.size == 81) {
            // 9x9 Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF475569))
                    .border(2.dp, Color(0xFF94A3B8), RoundedCornerShape(10.dp))
            ) {
                for (row in 0..8) {
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        for (col in 0..8) {
                            val idx = row * 9 + col
                            val value = board[idx]
                            val isFixed = fixedCells[idx]
                            val isSelected = selectedCell == idx
                            val isWrong = value != 0 && value != solution[idx]
                            val isSameNumber = selectedCell != null && value != 0 && value == board[selectedCell!!]

                            val bgColor = when {
                                isSelected -> Color(0xFF2563EB)
                                isWrong -> Color(0xFF7F1D1D)
                                isSameNumber -> Color(0xFF1E3A8A)
                                ((row / 3) + (col / 3)) % 2 == 0 -> Color(0xFF0F172A)
                                else -> Color(0xFF1E293B)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(
                                        end = if (col == 2 || col == 5) 2.dp else 0.5.dp,
                                        bottom = if (row == 2 || row == 5) 2.dp else 0.5.dp
                                    )
                                    .background(bgColor)
                                    .clickable { selectedCell = idx },
                                contentAlignment = Alignment.Center
                            ) {
                                if (value != 0) {
                                    Text(
                                        text = "$value",
                                        fontSize = 18.sp,
                                        fontWeight = if (isFixed) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = when {
                                            isWrong -> Color(0xFFFCA5A5)
                                            isFixed -> Color.White
                                            else -> Color(0xFF38BDF8)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Number Pad (1-9)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (num in 1..9) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .clickable {
                                val sel = selectedCell
                                if (sel != null && !fixedCells[sel]) {
                                    val updated = board.clone()
                                    updated[sel] = num
                                    board = updated
                                    if (num != solution[sel]) {
                                        mistakes++
                                    } else {
                                        checkCompletion(updated)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$num",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val sel = selectedCell
                        if (sel != null && !fixedCells[sel]) {
                            val updated = board.clone()
                            updated[sel] = 0
                            board = updated
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Erase Cell", color = Color.White)
                }

                Button(
                    onClick = {
                        val sel = selectedCell
                        if (sel != null && !fixedCells[sel] && hintsLeft > 0 && board[sel] != solution[sel]) {
                            val updated = board.clone()
                            updated[sel] = solution[sel]
                            board = updated
                            hintsLeft--
                            checkCompletion(updated)
                        }
                    },
                    enabled = hintsLeft > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = "Hint", tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Use Hint ($hintsLeft)", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
