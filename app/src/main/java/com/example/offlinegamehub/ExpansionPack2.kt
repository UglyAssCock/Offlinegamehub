package com.example.offlinegamehub

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

// =========================================================
// 1. LUDO (FULL 4-COLOR CROSS BOARD MATCHING IMAGE 3)
// =========================================================
@Composable
fun LudoGameScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var vsBot by remember { mutableStateOf(true) }
    var redStep by remember { mutableIntStateOf(0) }
    var blueStep by remember { mutableIntStateOf(0) }
    var isPlayerTurn by remember { mutableStateOf(true) }
    var bannerText by remember { mutableStateOf("Tap Board to Roll Dice!") }

    LaunchedEffect(isPlayerTurn, vsBot) {
        if (vsBot && !isPlayerTurn) {
            bannerText = "Bot thinking"
            delay(850L)
            val roll = Random.nextInt(1, 7)
            blueStep = minOf(20, blueStep + roll)
            bannerText = "Bot rolled $roll! Your Turn"
            isPlayerTurn = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4CA3CE)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ArcadeVsHeader(
            youScore = redStep,
            opponentScore = blueStep,
            vsBot = vsBot,
            modeLabel = "MEDIUM",
            onToggleMode = { vsBot = !vsBot; redStep = 0; blueStep = 0 },
            onBack = onBack,
            onRestart = { redStep = 0; blueStep = 0; isPlayerTurn = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rounded Status Banner ("Bot thinking" from Image 3)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .height(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF417D9A)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = bannerText,
                color = Color(0xFFB8D8E8),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 15x15 Vector Ludo Cross Board
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .aspectRatio(1f)
                .background(Color(0xFF262B2E))
                .padding(4.dp)
                .clickable(enabled = isPlayerTurn || !vsBot) {
                    val roll = Random.nextInt(1, 7)
                    if (isPlayerTurn) {
                        redStep = minOf(20, redStep + roll)
                        if (redStep == 20) onSaveScore(500)
                        bannerText = "You rolled $roll!"
                        isPlayerTurn = false
                    } else {
                        blueStep = minOf(20, blueStep + roll)
                        bannerText = "P2 rolled $roll!"
                        isPlayerTurn = true
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cell = size.width / 15f

                // White background grid for track cells
                drawRect(color = Color.White, size = size)
                for (i in 0..15) {
                    drawLine(Color(0xFF262B2E), Offset(i * cell, 0f), Offset(i * cell, size.height), 2f)
                    drawLine(Color(0xFF262B2E), Offset(0f, i * cell), Offset(size.width, i * cell), 2f)
                }

                // Colored Home Paths
                drawRect(Color(0xFFFFBB2C), Offset(1 * cell, 7 * cell), Size(5 * cell, cell))
                drawRect(Color(0xFF00B4EE), Offset(7 * cell, 1 * cell), Size(cell, 5 * cell))
                drawRect(Color(0xFFFF5A52), Offset(7 * cell, 9 * cell), Size(cell, 5 * cell))
                drawRect(Color(0xFF72C837), Offset(9 * cell, 7 * cell), Size(5 * cell, cell))

                // 4 Corner Bases (6x6 cells each)
                fun drawBase(startX: Float, startY: Float, mainCol: Color, innerCol: Color, drawTokens: Boolean, tokenCol: Color) {
                    drawRect(mainCol, Offset(startX, startY), Size(6 * cell, 6 * cell))
                    drawRoundRect(
                        color = innerCol,
                        topLeft = Offset(startX + 1.1f * cell, startY + 1.1f * cell),
                        size = Size(3.8f * cell, 3.8f * cell),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
                    )
                    val offsets = listOf(
                        Offset(startX + 2.1f * cell, startY + 2.1f * cell),
                        Offset(startX + 3.9f * cell, startY + 2.1f * cell),
                        Offset(startX + 2.1f * cell, startY + 3.9f * cell),
                        Offset(startX + 3.9f * cell, startY + 3.9f * cell)
                    )
                    offsets.forEach { pt ->
                        drawCircle(Color.White.copy(alpha = 0.55f), radius = cell * 0.55f, center = pt)
                        if (drawTokens) {
                            drawCircle(Color.Black, radius = cell * 0.46f, center = pt)
                            drawCircle(tokenCol, radius = cell * 0.38f, center = pt)
                        }
                    }
                }

                drawBase(0f, 0f, Color(0xFFFFBB2C), Color(0xFFFFD470), false, Color(0xFFFFBB2C))
                drawBase(9 * cell, 0f, Color(0xFF00B4EE), Color(0xFF8BE2FF), true, Color(0xFF00B4EE))
                drawBase(0f, 9 * cell, Color(0xFFFF5A52), Color(0xFFFFB2AE), true, Color(0xFFFF5A52))
                drawBase(9 * cell, 9 * cell, Color(0xFF72C837), Color(0xFFA9E382), false, Color(0xFF72C837))

                // Center 3x3 Home Triangles
                val cx = 7.5f * cell
                val cy = 7.5f * cell
                val tl = Offset(6 * cell, 6 * cell)
                val tr = Offset(9 * cell, 6 * cell)
                val bl = Offset(6 * cell, 9 * cell)
                val br = Offset(9 * cell, 9 * cell)

                drawPath(Path().apply { moveTo(tl.x, tl.y); lineTo(tr.x, tr.y); lineTo(cx, cy); close() }, Color(0xFF00B4EE))
                drawPath(Path().apply { moveTo(bl.x, bl.y); lineTo(br.x, br.y); lineTo(cx, cy); close() }, Color(0xFFFF5A52))
                drawPath(Path().apply { moveTo(tl.x, tl.y); lineTo(bl.x, bl.y); lineTo(cx, cy); close() }, Color(0xFFFFBB2C))
                drawPath(Path().apply { moveTo(tr.x, tr.y); lineTo(br.x, br.y); lineTo(cx, cy); close() }, Color(0xFF72C837))
            }
        }
    }
}

// =========================================================
// 2. DOMINOES (CORAL TABLE + CREAM PIP TILES MATCHING IMAGE 7)
// =========================================================
data class DominoTile(val top: Int, val bottom: Int)

@Composable
fun DominoPipTileView(
    tile: DominoTile,
    isFaceUp: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .size(width = 45.dp, height = 92.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFF4D2))
            .border(2.5.dp, Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
            .let { if (onClick != null) it.clickable { onClick() } else it }
    ) {
        if (isFaceUp) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawLine(Color(0xFF1E1E1E), Offset(w * 0.12f, h / 2f), Offset(w * 0.88f, h / 2f), 3f)

                fun drawPips(count: Int, yOffset: Float) {
                    val cx = w / 2f
                    val cy = yOffset + h / 4f
                    val dx = w * 0.25f
                    val dy = h * 0.12f
                    val r = 5.5f
                    val col = Color(0xFF1E1E1E)

                    if (count % 2 == 1) drawCircle(col, r, Offset(cx, cy))
                    if (count >= 2) {
                        drawCircle(col, r, Offset(cx - dx, cy - dy))
                        drawCircle(col, r, Offset(cx + dx, cy + dy))
                    }
                    if (count >= 4) {
                        drawCircle(col, r, Offset(cx + dx, cy - dy))
                        drawCircle(col, r, Offset(cx - dx, cy + dy))
                    }
                    if (count == 6) {
                        drawCircle(col, r, Offset(cx - dx, cy))
                        drawCircle(col, r, Offset(cx + dx, cy))
                    }
                }

                drawPips(tile.top, 0f)
                drawPips(tile.bottom, h / 2f)
            }
        }
    }
}

@Composable
fun DominoesGameScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    var vsBot by remember { mutableStateOf(true) }
    var playerHand by remember { mutableStateOf(listOf<DominoTile>()) }
    var botCount by remember { mutableIntStateOf(7) }
    var playedChain by remember { mutableStateOf(listOf<DominoTile>()) }
    var leftEnd by remember { mutableIntStateOf(-1) }
    var rightEnd by remember { mutableIntStateOf(-1) }
    var p1Score by remember { mutableIntStateOf(0) }
    var botScore by remember { mutableIntStateOf(0) }

    fun dealDominoes() {
        val deck = mutableListOf<DominoTile>()
        for (a in 0..6) for (b in a..6) deck.add(DominoTile(a, b))
        deck.shuffle()
        playerHand = deck.take(7)
        botCount = 7
        playedChain = emptyList()
        leftEnd = -1
        rightEnd = -1
    }

    LaunchedEffect(Unit) { dealDominoes() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF39C74)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Blue Header Band + Opponent Face-Down Tiles
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF00C2FF))
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArcadeVsHeader(
                youScore = p1Score,
                opponentScore = botScore,
                vsBot = vsBot,
                modeLabel = "GOAL 150",
                onToggleMode = { vsBot = !vsBot; dealDominoes() },
                onBack = onBack,
                onRestart = { dealDominoes() }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(botCount) {
                    DominoPipTileView(tile = DominoTile(0, 0), isFaceUp = false)
                }
            }
        }

        // Center Table Chain
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            if (playedChain.isEmpty()) {
                Text("Tap any domino below to start the chain!", color = Color(0xFF5A2E1B), fontWeight = FontWeight.Black)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    playedChain.takeLast(6).forEach { t ->
                        DominoPipTileView(tile = t, isFaceUp = true)
                    }
                }
            }
        }

        // Bottom Red Player Dock with Pip Dominoes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFF5A52))
                .padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            playerHand.forEachIndexed { idx, tile ->
                DominoPipTileView(
                    tile = tile,
                    isFaceUp = true,
                    onClick = {
                        val valid = playedChain.isEmpty() ||
                            tile.top == leftEnd || tile.bottom == leftEnd ||
                            tile.top == rightEnd || tile.bottom == rightEnd
                        if (valid) {
                            if (playedChain.isEmpty()) {
                                leftEnd = tile.top
                                rightEnd = tile.bottom
                            } else if (tile.top == rightEnd) {
                                rightEnd = tile.bottom
                            } else if (tile.bottom == rightEnd) {
                                rightEnd = tile.top
                            } else if (tile.top == leftEnd) {
                                leftEnd = tile.bottom
                            } else {
                                leftEnd = tile.top
                            }
                            playedChain = playedChain + tile
                            val next = playerHand.toMutableList().apply { removeAt(idx) }
                            playerHand = next
                            p1Score += (tile.top + tile.bottom)
                            onSaveScore(p1Score)
                            if (botCount > 1) {
                                botCount--
                                botScore += Random.nextInt(4, 11)
                            }
                            if (next.isEmpty()) dealDominoes()
                        }
                    }
                )
            }
        }
    }
}
