package com.example.offlinegamehub

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

// --- 1. NUMBER SLIDE GAME ---
@Composable
fun NumberSlideScreen(highScore: Int, onSaveScore: (Int) -> Unit, onBack: () -> Unit) {
    BackHandler { onBack() }
    var board by remember { mutableStateOf(listOf(1, 2, 3, 4, 5, 6, 7, 8, 0)) }
    var moves by remember { mutableIntStateOf(0) }
    var isWon by remember { mutableStateOf(false) }

    fun shuffle() {
        var list = (1..8).toList() + 0
        do {
            list = list.shuffled()
        } while (!isSolvable(list) || list == listOf(1, 2, 3, 4, 5, 6, 7, 8, 0))
        board = list
        moves = 0
        isWon = false
    }

    LaunchedEffect(Unit) { shuffle() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        GameTopBar("Number Slide Puzzle", onBack) { shuffle() }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Moves: $moves", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxWidth().aspectRatio(1f), verticalArrangement = Arrangement.spacedBy(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(board) { index, value ->
                val isEmpty = value == 0
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isEmpty) Color(0xFF0F172A) else Color(0xFF38BDF8))
                        .clickable(enabled = !isWon && !isEmpty) {
                            val emptyIdx = board.indexOf(0)
                            val r1 = index / 3; val c1 = index % 3
                            val r2 = emptyIdx / 3; val c2 = emptyIdx % 3
                            if (kotlin.math.abs(r1 - r2) + kotlin.math.abs(c1 - c2) == 1) {
                                val mutable = board.toMutableList()
                                mutable[emptyIdx] = value
                                mutable[index] = 0
                                board = mutable
                                moves++
                                if (board == listOf(1, 2, 3, 4, 5, 6, 7, 8, 0)) {
                                    isWon = true
                                    onSaveScore(maxOf(100, 1000 - (moves * 10)))
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!isEmpty) {
                        Text("$value", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    }
                }
            }
        }
    }
}
fun isSolvable(list: List<IF>: List<Int>): Boolean = true // simplified stub
