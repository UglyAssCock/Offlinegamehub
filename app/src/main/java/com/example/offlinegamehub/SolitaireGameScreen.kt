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

data class PlayingCard(
    val suit: Int, // 0 = ♠, 1 = ♥, 2 = ♦, 3 = ♣
    val rank: Int, // 1 = A .. 13 = K
    val isFaceUp: Boolean = false
) {
    val isRed: Boolean get() = suit == 1 || suit == 2
    val suitSymbol: String
        get() = when (suit) {
            0 -> "♠"
            1 -> "♥"
            2 -> "♦"
            else -> "♣"
        }
    val rankLabel: String
        get() = when (rank) {
            1 -> "A"
            11 -> "J"
            12 -> "Q"
            13 -> "K"
            else -> "$rank"
        }
}

sealed class CardSource {
    object Waste : CardSource()
    data class Tableau(val col: Int, val cardIndex: Int) : CardSource()
}

@Composable
fun SolitaireGameScreen(
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var stock by remember { mutableStateOf<List<PlayingCard>>(emptyList()) }
    var waste by remember { mutableStateOf<List<PlayingCard>>(emptyList()) }
    var foundations by remember { mutableStateOf(List(4) { emptyList<PlayingCard>() }) }
    var tableau by remember { mutableStateOf(List(7) { emptyList<PlayingCard>() }) }
    var selected by remember { mutableStateOf<CardSource?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var moves by remember { mutableIntStateOf(0) }
    var isWon by remember { mutableStateOf(false) }

    fun dealNewGame() {
        val fullDeck = mutableListOf<PlayingCard>()
        for (s in 0..3) {
            for (r in 1..13) {
                fullDeck.add(PlayingCard(suit = s, rank = r, isFaceUp = false))
            }
        }
        fullDeck.shuffle()

        val cols = MutableList(7) { mutableListOf<PlayingCard>() }
        var deckIdx = 0
        for (c in 0..6) {
            for (r in 0..c) {
                val card = fullDeck[deckIdx++]
                cols[c].add(card.copy(isFaceUp = (r == c)))
            }
        }

        stock = fullDeck.drop(deckIdx)
        waste = emptyList()
        foundations = List(4) { emptyList() }
        tableau = cols
        selected = null
        score = 0
        moves = 0
        isWon = false
    }

    LaunchedEffect(Unit) {
        dealNewGame()
    }

    fun revealTopIfNeeded(cols: List<List<PlayingCard>>): List<List<PlayingCard>> {
        return cols.map { col ->
            if (col.isNotEmpty() && !col.last().isFaceUp) {
                score += 5
                col.dropLast(1) + col.last().copy(isFaceUp = true)
            } else {
                col
            }
        }
    }

    fun canMoveToFoundation(card: PlayingCard, foundationPile: List<PlayingCard>): Boolean {
        return if (foundationPile.isEmpty()) {
            card.rank == 1
        } else {
            val top = foundationPile.last()
            top.suit == card.suit && card.rank == top.rank + 1
        }
    }

    fun canMoveToTableau(movingCard: PlayingCard, targetPile: List<PlayingCard>): Boolean {
        return if (targetPile.isEmpty()) {
            movingCard.rank == 13
        } else {
            val top = targetPile.last()
            top.isFaceUp && (top.isRed != movingCard.isRed) && (top.rank == movingCard.rank + 1)
        }
    }

    fun checkWinCondition(newFoundations: List<List<PlayingCard>>) {
        if (newFoundations.sumOf { it.size } == 52) {
            score += 500
            onSaveScore(score)
            isWon = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
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
                text = "Klondike Solitaire",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            IconButton(onClick = { dealNewGame() }) {
                Icon(Icons.Default.Refresh, contentDescription = "New Deal", tint = Color(0xFF14B8A6))
            }
        }

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
                Text("Score: $score", color = Color(0xFF14B8A6), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Moves: $moves", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Best: ${maxOf(highScore, score)}", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

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
                    Text("You Won Solitaire!", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("Final Score: $score pts", fontSize = 16.sp, color = Color(0xFF14B8A6), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { dealNewGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14B8A6))
                    ) {
                        Text("Deal New Game", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Top Row: Stock, Waste, and 4 Foundations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stock Pile
                Box(
                    modifier = Modifier
                        .size(width = 46.dp, height = 64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (stock.isNotEmpty()) Color(0xFF0F766E) else Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF475569), RoundedCornerShape(8.dp))
                        .clickable {
                            selected = null
                            if (stock.isNotEmpty()) {
                                val drawn = stock.last().copy(isFaceUp = true)
                                stock = stock.dropLast(1)
                                waste = waste + drawn
                                moves++
                            } else if (waste.isNotEmpty()) {
                                stock = waste.reversed().map { it.copy(isFaceUp = false) }
                                waste = emptyList()
                                moves++
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (stock.isNotEmpty()) "${stock.size}" else "↻",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Waste Pile
                val isWasteSelected = selected is CardSource.Waste
                val topWaste = waste.lastOrNull()
                Box(
                    modifier = Modifier
                        .size(width = 46.dp, height = 64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (topWaste != null) Color.White else Color(0xFF1E293B))
                        .border(
                            width = if (isWasteSelected) 3.dp else 1.dp,
                            color = if (isWasteSelected) Color(0xFFF59E0B) else Color(0xFF475569),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = topWaste != null) {
                            selected = if (isWasteSelected) null else CardSource.Waste
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (topWaste != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = topWaste.rankLabel,
                                color = if (topWaste.isRed) Color(0xFFDC2626) else Color.Black,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = topWaste.suitSymbol,
                                color = if (topWaste.isRed) Color(0xFFDC2626) else Color.Black,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // 4 Foundation Piles
                val suitLabels = listOf("♠", "♥", "♦", "♣")
                for (fIdx in 0..3) {
                    val pile = foundations[fIdx]
                    val topCard = pile.lastOrNull()
                    Box(
                        modifier = Modifier
                            .size(width = 46.dp, height = 64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (topCard != null) Color.White else Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF14B8A6), RoundedCornerShape(8.dp))
                            .clickable {
                                val src = selected
                                if (src is CardSource.Waste && waste.isNotEmpty()) {
                                    val card = waste.last()
                                    if (canMoveToFoundation(card, pile)) {
                                        waste = waste.dropLast(1)
                                        val updatedF = foundations.toMutableList()
                                        updatedF[fIdx] = pile + card
                                        foundations = updatedF
                                        score += 15
                                        moves++
                                        selected = null
                                        checkWinCondition(updatedF)
                                    }
                                } else if (src is CardSource.Tableau) {
                                    val colList = tableau[src.col]
                                    if (src.cardIndex == colList.lastIndex) {
                                        val card = colList.last()
                                        if (canMoveToFoundation(card, pile)) {
                                            val updatedT = tableau.toMutableList()
                                            updatedT[src.col] = colList.dropLast(1)
                                            tableau = revealTopIfNeeded(updatedT)

                                            val updatedF = foundations.toMutableList()
                                            updatedF[fIdx] = pile + card
                                            foundations = updatedF
                                            score += 15
                                            moves++
                                            selected = null
                                            checkWinCondition(updatedF)
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (topCard != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = topCard.rankLabel,
                                    color = if (topCard.isRed) Color(0xFFDC2626) else Color.Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = topCard.suitSymbol,
                                    color = if (topCard.isRed) Color(0xFFDC2626) else Color.Black,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Text(
                                text = suitLabels[fIdx],
                                color = Color(0xFF475569),
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7 Tableau Columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (colIdx in 0..6) {
                    val colCards = tableau[colIdx]
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 280.dp)
                    ) {
                        if (colCards.isEmpty()) {
                            // Empty Column Slot (for Kings)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                                    .clickable {
                                        val src = selected
                                        if (src is CardSource.Waste && waste.isNotEmpty()) {
                                            val card = waste.last()
                                            if (canMoveToTableau(card, emptyList())) {
                                                waste = waste.dropLast(1)
                                                val updatedT = tableau.toMutableList()
                                                updatedT[colIdx] = listOf(card)
                                                tableau = updatedT
                                                score += 5
                                                moves++
                                                selected = null
                                            }
                                        } else if (src is CardSource.Tableau && src.col != colIdx) {
                                            val movingStack = tableau[src.col].subList(src.cardIndex, tableau[src.col].size)
                                            if (canMoveToTableau(movingStack.first(), emptyList())) {
                                                val updatedT = tableau.toMutableList()
                                                updatedT[src.col] = tableau[src.col].take(src.cardIndex)
                                                updatedT[colIdx] = movingStack
                                                tableau = revealTopIfNeeded(updatedT)
                                                score += 5
                                                moves++
                                                selected = null
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("K", color = Color(0xFF334155), fontSize = 12.sp)
                            }
                        } else {
                            colCards.forEachIndexed { cardIdx, card ->
                                val isCardSelected = selected == CardSource.Tableau(colIdx, cardIdx)
                                val isLastCard = cardIdx == colCards.lastIndex

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 3.dp)
                                        .height(if (isLastCard) 54.dp else 32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when {
                                                !card.isFaceUp -> Color(0xFF1E3A8A)
                                                isCardSelected -> Color(0xFFFEF08A)
                                                else -> Color.White
                                            }
                                        )
                                        .border(
                                            width = if (isCardSelected) 2.dp else 1.dp,
                                            color = if (isCardSelected) Color(0xFFF59E0B) else Color(0xFF64748B),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable(enabled = card.isFaceUp) {
                                            val src = selected
                                            if (src == null) {
                                                selected = CardSource.Tableau(colIdx, cardIdx)
                                            } else if (src == CardSource.Tableau(colIdx, cardIdx)) {
                                                selected = null
                                            } else if (src is CardSource.Waste && isLastCard && waste.isNotEmpty()) {
                                                val moving = waste.last()
                                                if (canMoveToTableau(moving, colCards)) {
                                                    waste = waste.dropLast(1)
                                                    val updatedT = tableau.toMutableList()
                                                    updatedT[colIdx] = colCards + moving
                                                    tableau = updatedT
                                                    score += 5
                                                    moves++
                                                    selected = null
                                                } else {
                                                    selected = CardSource.Tableau(colIdx, cardIdx)
                                                }
                                            } else if (src is CardSource.Tableau && src.col != colIdx && isLastCard) {
                                                val movingStack = tableau[src.col].subList(src.cardIndex, tableau[src.col].size)
                                                if (canMoveToTableau(movingStack.first(), colCards)) {
                                                    val updatedT = tableau.toMutableList()
                                                    updatedT[src.col] = tableau[src.col].take(src.cardIndex)
                                                    updatedT[colIdx] = colCards + movingStack
                                                    tableau = revealTopIfNeeded(updatedT)
                                                    score += 5
                                                    moves++
                                                    selected = null
                                                } else {
                                                    selected = CardSource.Tableau(colIdx, cardIdx)
                                                }
                                            } else {
                                                selected = CardSource.Tableau(colIdx, cardIdx)
                                            }
                                        },
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    if (card.isFaceUp) {
                                        Text(
                                            text = "${card.rankLabel}${card.suitSymbol}",
                                            color = if (card.isRed) Color(0xFFDC2626) else Color.Black,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Tip: Tap any face-up card to highlight it in yellow, then tap the column or foundation pile where you want it to go!",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
        }
    }
}
