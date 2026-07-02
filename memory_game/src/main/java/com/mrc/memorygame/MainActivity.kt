package com.mrc.memorygame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val PersianWords = listOf(
    "خورشید",
    "ماه",
    "کتاب",
    "خانه",
    "باران",
    "گل",
    "دریا",
    "دوست",
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemoryGameApp()
        }
    }
}

@Composable
private fun MemoryGameApp() {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
    ) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                background = Color(0xFF111827),
                surface = Color(0xFF111827),
                primary = Color(0xFF22C55E),
                secondary = Color(0xFFF59E0B),
                onBackground = Color(0xFFF8FAFC),
                onSurface = Color(0xFFF8FAFC),
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                MemoryGameScreen()
            }
        }
    }
}

@Composable
private fun MemoryGameScreen() {
    var moves by remember { mutableIntStateOf(0) }
    var matchedPairs by remember { mutableIntStateOf(0) }
    var waitingToHide by remember { mutableStateOf(false) }
    val cards = remember { mutableStateListOf<MemoryCard>() }

    fun resetGame() {
        moves = 0
        matchedPairs = 0
        waitingToHide = false
        cards.clear()
        cards.addAll(createCards())
    }

    LaunchedEffect(Unit) {
        resetGame()
    }

    val selectedCards = cards.filter { it.isFaceUp && !it.isMatched }

    LaunchedEffect(selectedCards.map { it.id }) {
        if (selectedCards.size == 2) {
            waitingToHide = true
            moves++
            val first = selectedCards[0]
            val second = selectedCards[1]
            if (first.word == second.word) {
                cards.updateCard(first.id) { copy(isMatched = true) }
                cards.updateCard(second.id) { copy(isMatched = true) }
                matchedPairs++
                waitingToHide = false
            } else {
                delay(850)
                cards.updateCard(first.id) { copy(isFaceUp = false) }
                cards.updateCard(second.id) { copy(isFaceUp = false) }
                waitingToHide = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E293B),
                        Color(0xFF111827),
                        Color(0xFF0F172A),
                    )
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(maxWidth = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "بازی حافظه",
                color = Color(0xFFF8FAFC),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "دو کارت را انتخاب کن و کلمه های مشابه را پیدا کن.",
                color = Color(0xFFCBD5E1),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(modifier = Modifier.height(18.dp))
            ScoreRow(
                moves = moves,
                matchedPairs = matchedPairs,
                totalPairs = PersianWords.size,
            )
            Spacer(modifier = Modifier.height(18.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = false,
            ) {
                items(cards, key = { it.id }) { card ->
                    MemoryCardButton(
                        card = card,
                        enabled = !waitingToHide && !card.isMatched && !card.isFaceUp,
                        onClick = {
                            if (cards.count { it.isFaceUp && !it.isMatched } < 2) {
                                cards.updateCard(card.id) { copy(isFaceUp = true) }
                            }
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            if (matchedPairs == PersianWords.size && cards.isNotEmpty()) {
                Text(
                    text = "آفرین! همه کلمه ها را پیدا کردی.",
                    color = Color(0xFF86EFAC),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Button(onClick = ::resetGame) {
                Text(text = "شروع دوباره", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun ScoreRow(
    moves: Int,
    matchedPairs: Int,
    totalPairs: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ScoreBox(
            title = "حرکت ها",
            value = moves.toString(),
            modifier = Modifier.weight(1f),
        )
        ScoreBox(
            title = "جفت های پیدا شده",
            value = "$matchedPairs / $totalPairs",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ScoreBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1F2937))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            color = Color(0xFFCBD5E1),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            text = value,
            color = Color(0xFFF8FAFC),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MemoryCardButton(
    card: MemoryCard,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val visible = card.isFaceUp || card.isMatched
    val background = when {
        card.isMatched -> Color(0xFF14532D)
        visible -> Color(0xFFF8FAFC)
        else -> Color(0xFF2563EB)
    }
    val border = when {
        card.isMatched -> Color(0xFF22C55E)
        visible -> Color(0xFFF59E0B)
        else -> Color(0xFF60A5FA)
    }
    val textColor = when {
        visible -> Color(0xFF111827)
        else -> Color(0xFFF8FAFC)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(2.dp, border, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (visible) card.word else "؟",
            color = textColor,
            fontSize = if (visible) 14.sp else 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
        )
    }
}

private data class MemoryCard(
    val id: Int,
    val word: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false,
)

private fun createCards(): List<MemoryCard> =
    PersianWords
        .flatMap { word -> listOf(word, word) }
        .shuffled()
        .mapIndexed { index, word -> MemoryCard(id = index, word = word) }

private fun MutableList<MemoryCard>.updateCard(
    id: Int,
    transform: MemoryCard.() -> MemoryCard,
) {
    val index = indexOfFirst { it.id == id }
    if (index >= 0) {
        this[index] = this[index].transform()
    }
}

@Preview
@Composable
private fun MemoryGamePreview() {
    MemoryGameApp()
}
