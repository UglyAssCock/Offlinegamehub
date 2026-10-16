package com.example.offlinegamehub

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import kotlin.random.Random

// ==========================================
// 1. UNO COLOR CARD BATTLE (VS BOT)
// ==========================================
data class UnoCard(
    val colorIndex: Int, // 0=Red, 1=Blue, 2=Green, 3=Yellow
    val label: String    // "0".."9", "SKIP", "+2"
) {
    val bgColor: Color
        get() = when (colorIndex) {
            0 -> Color(0xFFEF4444)
            1 -> Color(0xFF3B82F6)
            2 -> Color(0xFF10B981)
            else -> Color(0xFFF59E0B)
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
    var botHand by remember { mutableStateOf(listOf<UnoCard>()) }
    var topCard by remember { mutableStateOf(UnoCard(0, "5")) }
    var isPlayerTurn by remember { mutableStateOf(true) }
    var statusText by remember { mutableStateOf("Match the top card's Color or Number/Symbol!") }
    var winner by remember { mutableStateOf<String?>(null) }

    fun randomUnoCard(): UnoCard {
        val labels = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "SKIP", "+2")
        return UnoCard(Random.nextInt(0, 4), labels.random())
    }

    fun startNewUno() {
        playerHand = List(7) { randomUnoCard() }
        botHand = List(7) { randomUnoCard() }
        topCard = UnoCard(Random.nextInt(0, 4), (1..9).random().toString())
        isPlayerTurn = true
        winner = null
        statusText = "Your Turn! Tap a matching card or Draw."
    }

    LaunchedEffect(Unit) { startNewUno() }

    fun canPlay(card: UnoCard, target: UnoCard): Boolean {
        return card.colorIndex == target.colorIndex || card.label == target.label
    }

    LaunchedEffect(isPlayerTurn, winner) {
        if (!isPlayerTurn && winner == null) {
            delay(800L)
            val playableIdx = botHand.indexOfFirst { canPlay(it, topCard) }
            if (playableIdx != -1) {
                val played = botHand[playableIdx]
                val nextBotHand = botHand.toMutableList().apply { removeAt(playableIdx) }
                botHand = nextBotHand
                topCard = played

                if (nextBotHand.isEmpty()) {
                    winner = "BOT"
                    statusText = "🤖 Bot emptied its hand and won!"
                } else {
                    when (played.label) {
                        "+2" -> {
                            playerHand = playerHand + randomUnoCard() + randomUnoCard()
                            statusText = "🤖 Bot played +2! You drew 2 cards and Bot goes again!"
                            // Bot keeps turn briefly
                            delay(600L)
                            isPlayerTurn = true
                        }
                        "SKIP" -> {
                            statusText = "🤖 Bot played SKIP! Bot takes another turn!"
                            delay(600L)
                            isPlayerTurn = true
                        }
                        else -> {
                            statusText = "🤖 Bot played ${played.label}. Your turn!"
                            isPlayerTurn = true
                        }
                    }
                }
            } else {
                botHand = botHand + randomUnoCard()
                statusText = "🤖 Bot had no match and drew 1 card. Your turn!"
                isPlayerTurn = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Text("UNO Card Battle", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = { startNewUno() }) { Icon(Icons.Default.Refresh, null, tint = Color(0xFFE11D48)) }
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("🤖 Bot Cards: ${botHand.size}", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                Text("Your Cards: ${playerHand.size}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                Text("Best: $highScore", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("CURRENT TOP CARD", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(width = 110.dp, height = 155.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(topCard.bgColor)
                .border(4.dp, Color.White, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = topCard.label,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = statusText,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (winner == null) {
            Button(
                onClick = {
                    if (isPlayerTurn) {
                        playerHand = playerHand + randomUnoCard()
                        statusText = "You drew a card. Bot's turn!"
                        isPlayerTurn = false
                    }
                },
                enabled = isPlayerTurn,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Draw 1 Card from Deck", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("YOUR HAND (Tap a matching card to play)", fontSize = 12.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                playerHand.forEachIndexed { index, card ->
                    val playable = isPlayerTurn && canPlay(card, topCard)
                    Box(
                        modifier = Modifier
                            .size(width = 80.dp, height = 118.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(card.bgColor.copy(alpha = if (playable) 1f else 0.45f))
                            .border(
                                width = if (playable) 3.dp else 1.dp,
                                color = if (playable) Color.White else Color.Gray,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable(enabled = playable) {
                                val nextHand = playerHand.toMutableList().apply { removeAt(index) }
                                playerHand = nextHand
                                topCard = card
                                if (nextHand.isEmpty()) {
                                    winner = "PLAYER"
                                    val pts = 500 + (botHand.size * 50)
                                    onSaveScore(pts)
                                    statusText = "🎉 UNO VICTORY! You won $pts pts!"
                                } else {
                                    when (card.label) {
                                        "+2" -> {
                                            botHand = botHand + randomUnoCard() + randomUnoCard()
                                            statusText = "🔥 You hit Bot with +2! Play again!"
                                        }
                                        "SKIP" -> {
                                            statusText = "⚡ Bot Skipped! Play another card!"
                                        }
                                        else -> {
                                            statusText = "Bot is thinking..."
                                            isPlayerTurn = false
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = card.label,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            Button(
                onClick = { startNewUno() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Play Another Match", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 2. DOMINOES (VS BOT & VS FRIEND)
// ==========================================
data class DominoTile(val left: Int, val right: Int)

@Composable
fun DominoesGameScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var vsBot by remember { mutableStateOf(true) }
    var p1Hand by remember { mutableStateOf(listOf<DominoTile>()) }
    var p2Hand by remember { mutableStateOf(listOf<DominoTile>()) }
    var boneyard by remember { mutableStateOf(listOf<DominoTile>()) }
    var leftEnd by remember { mutableIntStateOf(6) }
    var rightEnd by remember { mutableIntStateOf(6) }
    var boardCount by remember { mutableIntStateOf(1) }
    var isP1Turn by remember { mutableStateOf(true) }
    var statusText by remember { mutableStateOf("Match either end of the Domino chain!") }
    var winner by remember { mutableStateOf<String?>(null) }

    fun startDominoes() {
        val deck = mutableListOf<DominoTile>()
        for (i in 0..6) {
            for (j in i..6) {
                if (!(i == 6 && j == 6)) deck.add(DominoTile(i, j))
            }
        }
        deck.shuffle()
        p1Hand = deck.take(7)
        p2Hand = deck.drop(7).take(7)
        boneyard = deck.drop(14)
        leftEnd = 6
        rightEnd = 6
        boardCount = 1
        isP1Turn = true
        winner = null
        statusText = "Center starter tile is [6 | 6]. Tap a matching tile!"
    }

    LaunchedEffect(Unit) { startDominoes() }

    fun canPlayTile(t: DominoTile): Boolean {
        return t.left == leftEnd || t.right == leftEnd || t.left == rightEnd || t.right == rightEnd
    }

    fun playTileOnBoard(t: DominoTile) {
        when {
            t.left == leftEnd -> leftEnd = t.right
            t.right == leftEnd -> leftEnd = t.left
            t.left == rightEnd -> rightEnd = t.right
            t.right == rightEnd -> rightEnd = t.left
        }
        boardCount++
    }

    LaunchedEffect(isP1Turn, vsBot, winner) {
        if (vsBot && !isP1Turn && winner == null) {
            delay(750L)
            val idx = p2Hand.indexOfFirst { canPlayTile(it) }
            if (idx != -1) {
                val tile = p2Hand[idx]
                p2Hand = p2Hand.toMutableList().apply { removeAt(idx) }
                playTileOnBoard(tile)
                if (p2Hand.isEmpty()) {
                    winner = "🤖 Bot Won Dominoes!"
                } else {
                    statusText = "🤖 Bot played [${tile.left}\vert{}${tile.right}]. Your turn!"
                    isP1Turn = true
                }
            } else if (boneyard.isNotEmpty()) {
                p2Hand = p2Hand + boneyard.first()
                boneyard = boneyard.drop(1)
                statusText = "🤖 Bot drew from boneyard and passed."
                isP1Turn = true
            } else {
                statusText = "🤖 Bot passed! Your turn."
                isP1Turn = true
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
            Text("Classic Dominoes", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = { startDominoes() }) { Icon(Icons.Default.Refresh, null, tint = Color(0xFF38BDF8)) }
        }

        ModeSelectorBar(vsBot = vsBot, accentColor = Color(0xFF38BDF8)) {
            vsBot = it
            startDominoes()
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("P1 Tiles: ${p1Hand.size}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                Text("${if (vsBot) "Bot" else "P2"} Tiles: ${p2Hand.size}", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                Text("Boneyard: ${boneyard.size}", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("OPEN CHAIN ENDS ($boardCount tiles played)", fontSize = 12.sp, color = Color(0xFF94A3B8))
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF1E293B)).border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("$leftEnd", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            Text("⟷", fontSize = 26.sp, color = Color(0xFF38BDF8))
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF1E293B)).border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("$rightEnd", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(winner ?: statusText, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(14.dp))

        val activeHand = if (isP1Turn) p1Hand else p2Hand

        if (winner == null) {
            Button(
                onClick = {
                    if (boneyard.isNotEmpty()) {
                        val drawn = boneyard.first()
                        boneyard = boneyard.drop(1)
                        if (isP1Turn) p1Hand = p1Hand + drawn else p2Hand = p2Hand + drawn
                    } else {
                        isP1Turn = !isP1Turn
                    }
                },
                enabled = isP1Turn || !vsBot,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (boneyard.isNotEmpty()) "Draw Tile from Boneyard (${boneyard.size})" else "Pass Turn", color = Color.White)
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (isP1Turn) "PLAYER 1 HAND" else "FRIEND (P2) HAND",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                activeHand.forEachIndexed { idx, tile ->
                    val playable = (isP1Turn || !vsBot) && canPlayTile(tile)
                    Box(
                        modifier = Modifier
                            .size(width = 70.dp, height = 110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (playable) Color.White else Color(0xFF334155))
                            .border(2.dp, if (playable) Color(0xFF38BDF8) else Color.Gray, RoundedCornerShape(12.dp))
                            .clickable(enabled = playable) {
                                playTileOnBoard(tile)
                                if (isP1Turn) {
                                    val next = p1Hand.toMutableList().apply { removeAt(idx) }
                                    p1Hand = next
                                    if (next.isEmpty()) {
                                        winner = "🎉 Player 1 Wins Dominoes!"
                                        onSaveScore(400 + p2Hand.size * 40)
                                    } else {
                                        isP1Turn = false
                                    }
                                } else {
                                    val next = p2Hand.toMutableList().apply { removeAt(idx) }
                                    p2Hand = next
                                    if (next.isEmpty()) {
                                        winner = "🎉 Friend (P2) Wins Dominoes
