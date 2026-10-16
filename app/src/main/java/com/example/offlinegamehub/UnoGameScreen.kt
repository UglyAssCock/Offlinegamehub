package com.example.offlinegamehub

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
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
import kotlin.random.Random

data class UnoCard(
    val colorIndex: Int, // 0=Red, 1=Yellow, 2=Green, 3=Blue, 4=Wild
    val label: String
) {
    val bgColor: Color
        get() = when (colorIndex) {
            0 -> Color(0xFFFF5A52)
            1 -> Color(0xFFFFBC2B)
            2 -> Color(0xFF28C76F)
            3 -> Color(0xFF00CFE8)
            else -> Color(0xFF8B5CF6)
        }
}

@Composable
fun UnoCardView(
    card: UnoCard,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .size(width = 72.dp, height = 112.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(2.5.dp, Color.Black, RoundedCornerShape(10.dp))
            .padding(4.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(card.bgColor)
            .let { if (onClick != null) it.clickable { onClick() } else it }
    ) {
        // Top-Left Corner Number
        Text(
            text = card.label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 5.dp, top = 2.dp)
        )

        // Center White Oval
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 48.dp, height = 56.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (card.colorIndex == 4) {
                // 4-Color Wheel inside Wild Card
                Canvas(modifier = Modifier.size(34.dp)) {
                    drawArc(Color(0xFFFF5A52), 0f, 90f, true)
                    drawArc(Color(0xFFFFBC2B), 90f, 90f, true)
                    drawArc(Color(0xFF28C76F), 180f, 90f, true)
                    drawArc(Color(0xFF00CFE8), 270f, 90f, true)
                    drawCircle(Color.White, radius = size.minDimension * 0.22f)
                }
            } else {
                Text(
                    text = card.label,
                    color = card.bgColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Bottom-Right Corner Number
        Text(
            text = card.label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 5.dp, bottom = 2.dp)
        )
    }
}

@Composable
fun UnoGameScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var playerHand by remember { mutableStateOf(listOf<UnoCard>()) }
    var zoeCount by remember { mutableIntStateOf(7) }
    var roseCount by remember { mutableIntStateOf(7) }
    var noraCount by remember { mutableIntStateOf(7) }
    var topCard by remember { mutableStateOf(UnoCard(1, "1")) }
    var turnIndex by remember { mutableIntStateOf(0) } // 0=YOU, 1=Rose, 2=Zoe, 3=Nora
    var winner by remember { mutableStateOf<String?>(null) }

    fun randomCard(): UnoCard {
        val isWild = Random.nextInt(10) == 0
        if (isWild) return UnoCard(4, "+4")
        val labels = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "+2")
        return UnoCard(Random.nextInt(0, 4), labels.random())
    }

    fun startNewUno() {
        playerHand = List(7) { randomCard() }
        zoeCount = 7
        roseCount = 7
        noraCount = 7
        topCard = UnoCard(1, "1")
        turnIndex = 0
        winner = null
    }

    LaunchedEffect(Unit) { startNewUno() }

    fun canPlay(card: UnoCard, target: UnoCard): Boolean {
        return card.colorIndex == 4 || target.colorIndex == 4 ||
            card.colorIndex == target.colorIndex || card.label == target.label
    }

    LaunchedEffect(turnIndex, winner) {
        if (turnIndex != 0 && winner == null) {
            delay(650L)
            topCard = UnoCard(Random.nextInt(0, 4), (1..9).random().toString())
            when (turnIndex) {
                1 -> {
                    if (Random.nextBoolean() && roseCount > 1) roseCount-- else roseCount++
                    turnIndex = 2
                }
                2 -> {
                    if (Random.nextBoolean() && zoeCount > 1) zoeCount-- else zoeCount++
                    turnIndex = 3
                }
                3 -> {
                    if (Random.nextBoolean() && noraCount > 1) noraCount-- else noraCount++
                    turnIndex = 0
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6B311F))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Circular Back & Restart Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, null, tint = Color(0xFFD9779B), modifier = Modifier.size(32.dp))
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { startNewUno() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Refresh, null, tint = Color(0xFFD9779B), modifier = Modifier.size(26.dp))
            }
        }

        // Top Opponent Card Fan (Zoe)
        Row(horizontalArrangement = Arrangement.spacedBy((-18).dp)) {
            repeat(zoeCount.coerceAtMost(8)) {
                Box(
                    modifier = Modifier
                        .size(width = 42.dp, height = 64.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF8D7B74))
                        .border(2.dp, Color.Black, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CARDS", color = Color(0xFFB5A59E), fontSize = 7.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Center Circular Brown Table with Rose, Zoe, Nora, YOU
        Box(
            modifier = Modifier
                .size(290.dp)
                .clip(CircleShape)
                .background(Color(0xFF8C432B))
                .border(14.dp, Color(0xFF7D3A24), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Zoe",
                color = if (turnIndex == 2) Color(0xFFFFBC2B) else Color(0xFF2B130B),
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp)
            )
            Text(
                text = "Rose ($roseCount)",
                color = if (turnIndex == 1) Color(0xFFFFBC2B) else Color(0xFF2B130B),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp)
            )
            Text(
                text = "Nora ($noraCount)",
                color = if (turnIndex == 3) Color(0xFFFFBC2B) else Color(0xFF2B130B),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp)
            )
            Text(
                text = if (winner != null) "🎉 YOU WON!" else "YOU",
                color = Color(0xFFFF9F76),
                fontWeight = FontWeight.Black,
                fontSize = 19.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
            )

            // Center Draw Pile + Discard Pile
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Draw Deck ("CARD PARTY")
                Box(
                    modifier = Modifier
                        .size(width = 70.dp, height = 108.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(2.5.dp, Color.Black, RoundedCornerShape(10.dp))
                        .padding(4.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFF8D7B74))
                        .clickable(enabled = turnIndex == 0 && winner == null) {
                            playerHand = playerHand + randomCard()
                            turnIndex = 1
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CARD\nPARTY",
                        color = Color(0xFFB5A59E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Current Top UNO Card
                UnoCardView(card = topCard)
            }
        }

        // Bottom Overlapping Player Card Fan
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy((-14).dp)
        ) {
            playerHand.forEachIndexed { idx, card ->
                val playable = turnIndex == 0 && canPlay(card, topCard)
                UnoCardView(
                    card = card,
                    modifier = Modifier.offset(y = if (playable) (-6).dp else 0.dp),
                    onClick = if (playable) {
                        {
                            val next = playerHand.toMutableList().apply { removeAt(idx) }
                            playerHand = next
                            topCard = if (card.colorIndex == 4) UnoCard(Random.nextInt(0, 4), "+4") else card
                            if (next.isEmpty()) {
                                winner = "YOU"
                                onSaveScore(600)
                            } else {
                                turnIndex = 1
                            }
                        }
                    } else null
                )
            }
        }
    }
}
