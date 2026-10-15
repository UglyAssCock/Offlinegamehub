package com.example.offlinegamehub

import androidx.compose.runtime.Composable

@Composable
fun SolitaireGameScreen(highScore: Int, onSaveScore: (Int) -> Unit, onBack: () -> Unit) {
    ComingNextPlaceholder("Solitaire Klondike", onBack)
}
