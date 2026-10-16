package com.example.offlinegamehub

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AppScreen {
    HOME,
    GEO_QUIZ,
    SPORTS_QUIZ,
    MEMORY_GAME,
    YATZY_GAME,
    SUDOKU_GAME,
    SOLITAIRE_GAME,
    TIC_TAC_TOE,
    DOTS_AND_BOXES,
    WORDLE_GAME,
    NUMBER_SLIDE,
    DARTS_GAME,
    ARCHERY_GAME,
    UNO_GAME,
    DOMINOES_GAME,
    LUDO_GAME,
    SNAKE_GAME,
    WORD_FINDER
}

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class HubItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val screen: AppScreen,
    val prefKey: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF38BDF8),
                    secondary = Color(0xFFF59E0B),
                    background = Color(0xFF0F172A),
                    surface = Color(0xFF1E293B)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameHubApp()
                }
            }
        }
    }
}

@Composable
fun GameHubApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("game_hub_scores", Context.MODE_PRIVATE) }
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    fun saveHighScore(key: String, score: Int) {
        val best = prefs.getInt(key, 0)
        if (score > best) {
            prefs.edit().putInt(key, score).apply()
            refreshTrigger++
        }
    }

    when (currentScreen) {
        AppScreen.HOME -> HomeScreen(
            prefs = prefs,
            refreshKey = refreshTrigger,
            onNavigate = { currentScreen = it }
        )
        AppScreen.GEO_QUIZ -> QuizEngineScreen(
            title = "World Geography Quiz",
            accentColor = Color(0xFF10B981),
            questions = remember { getGeographyQuestions().shuffled() },
            highScore = prefs.getInt("best_geo", 0),
            onSaveScore = { saveHighScore("best_geo", it) },
            onBack = { currentScreen = AppScreen.HOME }
        )
        AppScreen.SPORTS_QUIZ -> QuizEngineScreen(
            title = "World Sports Quiz",
            accentColor = Color(0xFFF59E0B),
            questions = remember { getSportsQuestions().shuffled() },
            highScore = prefs.getInt("best_sports", 0),
            onSaveScore = { saveHighScore("best_sports", it) },
            onBack = { currentScreen = AppScreen.HOME }
        )
        AppScreen.MEMORY_GAME -> MemoryGameScreen(
            highScore = prefs.getInt("best_memory", 0),
            onSaveScore = { saveHighScore("best_memory", it) },
            onBack = { currentScreen = AppScreen.HOME }
        )
        AppScreen.YATZY_GAME -> YatzyGameScreen(
            highScore = prefs.getInt("best_yatzy", 0),
            onSaveScore = { saveHighScore("best_yatzy", it) },
            onBack = { currentScreen = AppScreen.HOME }
        )
        AppScreen.SUDOKU_GAME -> SudokuGameScreen(
            highScore = prefs.getInt("best_sudoku", 0),
            onSaveScore = { saveHighScore("best_sudoku", it) },
            onBack = { currentScreen = AppScreen.HOME }
        )
        AppScreen.SOLITAIRE_GAME -> SolitaireGameScreen(
            highScore = prefs.getInt("best_solitaire", 0),
            onSaveScore = { saveHighScore("best_solitaire", it) },
            onBack = { currentScreen = AppScreen.HOME }
        )
        AppScreen.TIC_TAC_TOE -> TicTacToeScreen(
            highScore = prefs.getInt("best_ttt", 0),
            onSaveScore = { saveHighScore("best_ttt", it) },
            onBack = { currentScreen = AppScreen.HOME }
        )
        AppScreen.DOTS_AND_BOXES -> DotsAndBoxesScreen(
            highScore = prefs.getInt("best_dots", 0),
            onSaveScore = { saveHighScore("best_dots", it) },
            onBack = { currentScreen = AppScreen.HOME }
        )
        AppScreen.WORDLE_GAME -> WordleGameScreen(
            highScore = prefs.getInt("best_wordle", 0),
            onSaveScore = { saveHighScore("best_wordle", it) },
            onBack = { currentScreen = AppScreen.HOME }
        )
        AppScreen.NUMBER_SLIDE -> NumberSlideScreen(
            highScore = prefs.getInt("best_slide", 0),
            onSaveScore = { saveHighScore("best_slide", it) },
            onBack = { currentScreen = AppScreen.HOME }
        )
        AppScreen.DARTS_GAME -> DartsGameScreen(
            highScore = prefs.getInt("best_darts", 0),
            onSaveScore = { saveHighScore("best_darts", it) },
            onBack = { currentScreen = AppScreen.HOME }
        )
        AppScreen.ARCHERY_GAME -> ArcheryGameScreen(
            highScore = prefs.getInt("best_archery", 0),
            onSaveScore = { saveHighScore("best_archery", it) },
            onBack = { currentScreen = AppScreen.HOME }
        )
        AppScreen.UNO_GAME -> UnoGameScreen(
            highScore = prefs.getInt("best_uno", 0),
            onSaveScore = { saveHighScore("best_uno", it) },
            onBack = { currentScreen = AppScreen.HOME }
        )
        AppScreen.DOMINOES_GAME -> DominoesGameScreen(
            highScore = prefs.getInt("best_dominoes", 0),
            onSaveScore = { saveHighScore("best_dominoes", it) },
            onBack = { currentScreen = AppScreen.HOME }
        )
        AppScreen.LUDO_GAME -> LudoGameScreen(
            highScore = prefs.getInt("best_ludo", 0),
            onSaveScore = { saveHighScore("best_ludo", it) },
            onBack = { currentScreen = AppScreen.HOME }
        )
        AppScreen.SNAKE_GAME -> SnakeGameScreen(
            highScore = prefs.getInt("best_snake", 0),
            onSaveScore = { saveHighScore("best_snake", it) },
            onBack = { currentScreen = AppScreen.HOME }
        )
        AppScreen.WORD_FINDER -> WordFinderScreen(
            highScore = prefs.getInt("best_wordfinder", 0),
            onSaveScore = { saveHighScore("best_wordfinder", it) },
            onBack = { currentScreen = AppScreen.HOME }
        )
    }
}

@Composable
fun HomeScreen(
    prefs: android.content.SharedPreferences,
    refreshKey: Int,
    onNavigate: (AppScreen) -> Unit
) {
    val items = listOf(
        HubItem("Geography Quiz", "Capitals, Flags & Borders", Icons.Default.Public, Color(0xFF10B981), AppScreen.GEO_QUIZ, "best_geo"),
        HubItem("Sports Quiz", "Football, World Cup & Records", Icons.Default.SportsSoccer, Color(0xFFF59E0B), AppScreen.SPORTS_QUIZ, "best_sports"),
        HubItem("Tic Tac Toe", "Vs Bot or 2-Player Friend", Icons.Default.Tag, Color(0xFFEF4444), AppScreen.TIC_TAC_TOE, "best_ttt"),
        HubItem("Dots & Boxes", "Grid Territory Battle (Bot/2P)", Icons.Default.BorderAll, Color(0xFF06B6D4), AppScreen.DOTS_AND_BOXES, "best_dots"),
        HubItem("Wordle 5-Letter", "Daily Offline Word Guess", Icons.Default.Spellcheck, Color(0xFF22C55E), AppScreen.WORDLE_GAME, "best_wordle"),
        HubItem("Number Slide", "Classic 4x4 15-Tile Puzzle", Icons.Default.Dialpad, Color(0xFFA855F7), AppScreen.NUMBER_SLIDE, "best_slide"),
        HubItem("Darts 301", "Precision Aim vs Bot/Friend", Icons.Default.GpsFixed, Color(0xFFF97316), AppScreen.DARTS_GAME, "best_darts"),
        HubItem("Archery Pro", "Wind & Bullseye vs Bot/Friend", Icons.Default.TrackChanges, Color(0xFF84CC16), AppScreen.ARCHERY_GAME, "best_archery"),
        HubItem("UNO Card Battle", "Color & Action Cards vs Bot", Icons.Default.ViewCarousel, Color(0xFFE11D48), AppScreen.UNO_GAME, "best_uno"),
        HubItem("Dominoes", "Tile Matching vs Bot/Friend", Icons.Default.ViewModule, Color(0xFF38BDF8), AppScreen.DOMINOES_GAME, "best_dominoes"),
        HubItem("Ludo Race", "Dice Board Race vs Bot/Friend", Icons.Default.Gamepad, Color(0xFFFACC15), AppScreen.LUDO_GAME, "best_ludo"),
        HubItem("Snake Classic", "Retro Arcade Apple Hunter", Icons.Default.Timeline, Color(0xFF4ADE80), AppScreen.SNAKE_GAME, "best_snake"),
        HubItem("Word Finder", "Hidden Word Grid Search", Icons.Default.Search, Color(0xFFFB7185), AppScreen.WORD_FINDER, "best_wordfinder"),
        HubItem("Memory Match", "Card Flip Brain Trainer", Icons.Default.GridView, Color(0xFF8B5CF6), AppScreen.MEMORY_GAME, "best_memory"),
        HubItem("Yatzy Classic", "5-Dice Strategy & Scorecard", Icons.Default.Casino, Color(0xFFEC4899), AppScreen.YATZY_GAME, "best_yatzy"),
        HubItem("Sudoku 9x9", "Offline Logic Number Grids", Icons.Default.Apps, Color(0xFF3B82F6), AppScreen.SUDOKU_GAME, "best_sudoku"),
        HubItem("Solitaire", "Classic Klondike Patience", Icons.Default.Style, Color(0xFF14B8A6), AppScreen.SOLITAIRE_GAME, "best_solitaire")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "17-IN-1 OFFLINE ARCADE (SOLO • BOT • 2P)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8),
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Game & Quiz Hub",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                val bestScore = remember(refreshKey) { prefs.getInt(item.prefKey, 0) }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                        .clickable { onNavigate(item.screen) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(item.color.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = item.color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = item.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = item.subtitle,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Best: $bestScore pts",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = item.color
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizEngineScreen(
    title: String,
    accentColor: Color,
    questions: List<QuizQuestion>,
    highScore: Int,
    onSaveScore: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isFinished by remember { mutableStateOf(false) }

    val currentQuestion = questions[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = "Score: $score", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isFinished) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = "Trophy", tint = accentColor, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Quiz Complete!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Final Score: $score", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = accentColor)
                    Text("All-Time Best: ${maxOf(highScore, score)}", fontSize = 14.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            currentIndex = 0
                            score = 0
                            streak = 0
                            selectedOption = null
                            isFinished = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Play Again", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Text("Back to Game Hub", color = Color.White)
                    }
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Question ${currentIndex + 1} of ${questions.size}", color = Color(0xFF94A3B8), fontSize = 14.sp)
                Text("Streak: $streak 🔥", color = Color(0xFFF59E0B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / questions.size.toFloat() },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = accentColor,
                trackColor = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currentQuestion.question,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            currentQuestion.options.forEachIndexed { index, optionText ->
                val isSelected = selectedOption == index
                val isCorrect = index == currentQuestion.correctIndex
                val bgColor = when {
                    selectedOption == null -> Color(0xFF1E293B)
                    isCorrect -> Color(0xFF065F46)
                    isSelected -> Color(0xFF7F1D1D)
                    else -> Color(0xFF1E293B)
                }
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable(enabled = selectedOption == null) {
                            selectedOption = index
                            if (index == currentQuestion.correctIndex) {
                                streak++
                                score += 100 + (streak * 20)
                            } else {
                                streak = 0
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${('A' + index)}.", fontWeight = FontWeight.Bold, color = accentColor, fontSize = 16.sp, modifier = Modifier.width(28.dp))
                        Text(optionText, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            if (selectedOption != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(currentQuestion.explanation, color = Color(0xFFCBD5E1), fontSize = 14.sp, modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (currentIndex + 1 < questions.size) {
                            currentIndex++
                            selectedOption = null
                        } else {
                            onSaveScore(score)
                            isFinished = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        text = if (currentIndex + 1 < questions.size) "Next Question" else "Finish Quiz",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

fun getGeographyQuestions(): List<QuizQuestion> = listOf(
    QuizQuestion("Which African country is the largest by land area?", listOf("Algeria", "DR Congo", "Sudan", "Libya"), 0, "Algeria is the largest country in Africa, covering over 2.38 million sq km."),
    QuizQuestion("What is the capital city of Australia?", listOf("Sydney", "Melbourne", "Canberra", "Perth"), 2, "Canberra was chosen as Australia's capital in 1908."),
    QuizQuestion("Which river flows through the most countries in the world?", listOf("Nile", "Amazon", "Danube", "Yangtze"), 2, "The Danube flows through or borders 10 European countries."),
    QuizQuestion("Which country has the highest number of official time zones?", listOf("Russia", "United States", "France", "China"), 2, "France spans 12 time zones due to its overseas territories."),
    QuizQuestion("The Atacama Desert is primarily located in which South American country?", listOf("Peru", "Chile", "Argentina", "Bolivia"), 1, "The Atacama Desert in northern Chile is the driest non-polar desert on Earth."),
    QuizQuestion("Which strait separates Europe (Spain) from Africa (Morocco)?", listOf("Strait of Hormuz", "Strait of Malacca", "Strait of Gibraltar", "Bering Strait"), 2, "The Strait of Gibraltar connects the Atlantic Ocean to the Mediterranean Sea."),
    QuizQuestion("What is the highest mountain peak in Africa?", listOf("Mount Kenya", "Mount Kilimanjaro", "Mount Toubkal", "Mount Stanley"), 1, "Mount Kilimanjaro in Tanzania stands at 5,895 meters above sea level."),
    QuizQuestion("Which landlocked country lies entirely inside South Africa?", listOf("Eswatini", "Lesotho", "Botswana", "Rwanda"), 1, "Lesotho is completely surrounded by South Africa."),
    QuizQuestion("Ulaanbaatar is the capital of which Asian nation?", listOf("Kazakhstan", "Uzbekistan", "Mongolia", "Kyrgyzstan"), 2, "Ulaanbaatar is the capital and largest city of Mongolia."),
    QuizQuestion("Which ocean lies along the western coast of South America?", listOf("Atlantic Ocean", "Indian Ocean", "Pacific Ocean", "Southern Ocean"), 2, "The Pacific Ocean borders the entire western coast of South America.")
)

fun getSportsQuestions(): List<QuizQuestion> = listOf(
    QuizQuestion("Which country won the first-ever FIFA Men's World Cup in 1930?", listOf("Brazil", "Uruguay", "Argentina", "Italy"), 1, "Host nation Uruguay defeated Argentina 4-2 in the 1930 World Cup Final."),
    QuizQuestion("Which football club has won the most UEFA Champions League titles?", listOf("AC Milan", "Bayern Munich", "Real Madrid", "Liverpool"), 2, "Real Madrid holds the all-time record for the most European Cup / Champions League titles."),
    QuizQuestion("Which African nation became the first to reach a FIFA World Cup Semi-Final in 2022?", listOf("Senegal", "Algeria", "Morocco", "Ghana"), 2, "Morocco made history in Qatar 2022 by reaching the World Cup semi-finals."),
    QuizQuestion("How many players from one team are on the court at once in basketball?", listOf("4", "5", "6", "7"), 1, "Each basketball team fields 5 active players on the court."),
    QuizQuestion("In tennis, which Grand Slam tournament is played on clay courts?", listOf("Wimbledon", "US Open", "French Open (Roland Garros)", "Australian Open"), 2, "Roland Garros in Paris is contested on red clay courts."),
    QuizQuestion("Who holds the men's world record for the 100-meter sprint at 9.58 seconds?", listOf("Tyson Gay", "Yohan Blake", "Usain Bolt", "Carl Lewis"), 2, "Usain Bolt set the 9.58-second world record in Berlin in 2009."),
    QuizQuestion("How long is a standard Olympic marathon in kilometers?", listOf("40.0 km", "42.195 km", "45.5 km", "50.0 km"), 1, "The official marathon distance is 42.195 kilometers."),
    QuizQuestion("Which country won the 2019 Africa Cup of Nations (AFCON) hosted in Egypt?", listOf("Senegal", "Algeria", "Egypt", "Nigeria"), 1, "Algeria defeated Senegal 1-0 in the Cairo final to win AFCON 2019."),
    QuizQuestion("What is the maximum score with three darts in a single turn of standard darts?", listOf("150", "180", "200", "210"), 1, "Three triple-20s score the maximum 180 points."),
    QuizQuestion("Which player has scored the most career international goals in men's football?", listOf("Lionel Messi", "Cristiano Ronaldo", "Ali Daei", "Pelé"), 1, "Cristiano Ronaldo holds the all-time men's international goalscoring record.")
)
