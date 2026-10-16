package com.example.offlinegamehub

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
        statusText = "Starter tile is [6 | 6]. Tap a matching tile!"
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
                    winner = "Bot Won Dominoes!"
                } else {
                    statusText = "Bot played [${tile.left}|${tile.right}]. Your turn!"
                    isP1Turn = true
                }
            } else if (boneyard.isNotEmpty()) {
                p2Hand = p2Hand + boneyard.first()
                boneyard = boneyard.drop(1)
                statusText = "Bot drew from boneyard and passed."
                isP1Turn = true
            } else {
                statusText = "Bot passed! Your turn."
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

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { vsBot = true; startDominoes() }, colors = ButtonDefaults.buttonColors(containerColor = if (vsBot) Color(0xFF38BDF8) else Color(0xFF1E293B)), modifier = Modifier.weight(1f)) {
                Text("Vs Smart Bot", color = if (vsBot) Color.Black else Color.White, fontWeight = FontWeight.Bold)
            }
            Button(onClick = { vsBot = false; startDominoes() }, colors = ButtonDefaults.buttonColors(containerColor = if (!vsBot) Color(0xFF38BDF8) else Color(0xFF1E293B)), modifier = Modifier.weight(1f)) {
                Text("Vs Friend (2P)", color = if (!vsBot) Color.Black else Color.White, fontWeight = FontWeight.Bold)
            }
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

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
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
            Text(if (isP1Turn) "PLAYER 1 HAND" else "FRIEND (P2) HAND", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
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
                                        winner = "Player 1 Wins Dominoes!"
                                        onSaveScore(400 + p2Hand.size * 40)
                                    } else {
                                        isP1Turn = false
                                    }
                                } else {
                                    val next = p2Hand.toMutableList().apply { removeAt(idx) }
                                    p2Hand = next
                                    if (next.isEmpty()) {
                                        winner = "Friend (P2) Wins Dominoes!"
                                    } else {
                                        isP1Turn = true
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${tile.left}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.Black)
                            Text("${tile.right}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LudoGameScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val finishLine = 25
    var vsBot by remember { mutableStateOf(true) }
    var p1Pos by remember { mutableIntStateOf(0) }
    var p2Pos by remember { mutableIntStateOf(0) }
    var diceVal by remember { mutableIntStateOf(1) }
    var hasRolled by remember { mutableStateOf(false) }
    var isP1Turn by remember { mutableStateOf(true) }
    var statusText by remember { mutableStateOf("Roll and race to Space 25!") }
    var winner by remember { mutableStateOf<String?>(null) }

    fun resetLudo() {
        p1Pos = 0
        p2Pos = 0
        diceVal = 1
        hasRolled = false
        isP1Turn = true
        winner = null
        statusText = "Player 1: Tap ROLL DICE!"
    }

    LaunchedEffect(isP1Turn, vsBot, winner) {
        if (vsBot && !isP1Turn && winner == null) {
            delay(750L)
            val roll = Random.nextInt(1, 7)
            diceVal = roll
            val next = minOf(finishLine, p2Pos + roll)
            p2Pos = next
            statusText = "Bot rolled $roll and moved to $next/$finishLine!"
            if (next == finishLine) {
                winner = "Bot Won Ludo Race!"
            } else {
                isP1Turn = true
            }
            hasRolled = false
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
            Text("Ludo Quick Race", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = { resetLudo() }) { Icon(Icons.Default.Refresh, null, tint = Color(0xFFFACC15)) }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { vsBot = true; resetLudo() }, colors = ButtonDefaults.buttonColors(containerColor = if (vsBot) Color(0xFFFACC15) else Color(0xFF1E293B)), modifier = Modifier.weight(1f)) {
                Text("Vs Smart Bot", color = if (vsBot) Color.Black else Color.White, fontWeight = FontWeight.Bold)
            }
            Button(onClick = { vsBot = false; resetLudo() }, colors = ButtonDefaults.buttonColors(containerColor = if (!vsBot) Color(0xFFFACC15) else Color(0xFF1E293B)), modifier = Modifier.weight(1f)) {
                Text("Vs Friend (2P)", color = if (!vsBot) Color.Black else Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(winner ?: statusText, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFFACC15))
                .clickable(enabled = !hasRolled && winner == null && (isP1Turn || !vsBot)) {
                    diceVal = Random.nextInt(1, 7)
                    hasRolled = true
                    statusText = "Rolled $diceVal! Tap your token below to move."
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎲 $diceVal", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Text(if (hasRolled) "MOVE" else "ROLL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (hasRolled && isP1Turn) {
                    val next = minOf(finishLine, p1Pos + diceVal)
                    p1Pos = next
                    hasRolled = false
                    if (next == finishLine) {
                        winner = "Player 1 Wins Ludo!"
                        onSaveScore(600)
                    } else if (diceVal != 6) {
                        isP1Turn = false
                    } else {
                        statusText = "Rolled 6! Bonus turn for Player 1."
                    }
                }
            },
            enabled = hasRolled && isP1Turn && p1Pos < finishLine,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Player 1 Token: Space $p1Pos / $finishLine", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (!vsBot && hasRolled && !isP1Turn) {
                    val next = minOf(finishLine, p2Pos + diceVal)
                    p2Pos = next
                    hasRolled = false
                    if (next == finishLine) {
                        winner = "Friend (P2) Wins Ludo!"
                    } else if (diceVal != 6) {
                        isP1Turn = true
                    } else {
                        statusText = "Rolled 6! Bonus turn for Player 2."
                    }
                }
            },
            enabled = !vsBot && hasRolled && !isP1Turn && p2Pos < finishLine,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(if (vsBot) "Bot Token: Space $p2Pos / $finishLine" else "Friend (P2) Token: Space $p2Pos / $finishLine", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
