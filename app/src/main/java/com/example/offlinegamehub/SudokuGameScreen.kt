package com.example.offlinegamehub

import androidx.compose.runtime.Composable

@Composable
fun SudokuGameScreen(highScore: Int, onSaveScore: (Int) -> Unit, onBack: () -> Unit) {
    ComingNextPlaceholder("Sudoku 9x9", onBack)
}

@Composable
fun ComingNextPlaceholder(title: String, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler { onBack() }
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.Text(title, color = androidx.compose.ui.graphics.Color.White)
        androidx.compose.material3.Button(onClick = onBack) {
            androidx.compose.material3.Text("Back to Hub")
        }
    }
}
