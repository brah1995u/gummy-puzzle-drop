package studio.cortex.gummypuzzledrop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import studio.cortex.gummypuzzledrop.R
import studio.cortex.gummypuzzledrop.data.LeaderboardEntry
import studio.cortex.gummypuzzledrop.data.PlayerProgress
import studio.cortex.gummypuzzledrop.game.model.CandyType
import studio.cortex.gummypuzzledrop.ui.CandyAqua
import studio.cortex.gummypuzzledrop.ui.CandyCream
import studio.cortex.gummypuzzledrop.ui.CandyDeepPurple
import studio.cortex.gummypuzzledrop.ui.CandyMint
import studio.cortex.gummypuzzledrop.ui.CandyOrange
import studio.cortex.gummypuzzledrop.ui.components.CandyButton
import studio.cortex.gummypuzzledrop.ui.components.CandyProgressBar
import studio.cortex.gummypuzzledrop.ui.gameplay.CandyImage

@Composable
fun LeaderboardScreen(progress: PlayerProgress, onPlay: () -> Unit, onBack: () -> Unit) {
    MetaScreenScaffold(
        title = "LOCAL LEADERBOARD",
        subtitle = "Your best 10 completed runs — stored only on this device",
        onBack = onBack,
    ) {
        if (progress.leaderboard.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CandyImage(CandyType.PURPLE_BEAR, Modifier.size(112.dp))
                Text(
                    "NO SCORES YET",
                    color = CandyCream,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    "Finish a game to claim the first spot.",
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
                    color = CandyCream.copy(alpha = .70f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
                CandyButton(
                    "START A RUN", onPlay, Modifier.fillMaxWidth(), orange = true,
                    iconRes = R.drawable.gummy_green_bear,
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                item { LeaderboardHero(progress.leaderboard.first()) }
                itemsIndexed(progress.leaderboard, key = { _, entry -> entry.playedAtMillis }) { index, entry ->
                    LeaderboardRow(index + 1, entry)
                }
                item {
                    CandyButton(
                        "PLAY NEW RUN", onPlay, Modifier.fillMaxWidth(), orange = true,
                        iconRes = R.drawable.gummy_green_bear,
                    )
                    LeaderboardArchive(progress.leaderboard.size)
                }
            }
        }
    }
}

@Composable
private fun LeaderboardArchive(runCount: Int) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            CandyImage(CandyType.GREEN_BEAR, Modifier.size(43.dp))
            CandyImage(CandyType.GREEN_STAR, Modifier.size(43.dp))
            CandyImage(CandyType.RED_BEAR, Modifier.size(43.dp))
        }
        Text(
            "SWEET SCORE ARCHIVE • $runCount/10",
            color = CandyMint,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
        )
        CandyProgressBar(
            progress = runCount / 10f,
            modifier = Modifier.fillMaxWidth(.72f).padding(top = 7.dp),
            completed = runCount >= 10,
        )
    }
}

@Composable
private fun LeaderboardHero(entry: LeaderboardEntry) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF7D1C9C).copy(alpha = .92f), shape)
            .border(2.dp, CandyOrange, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CandyImage(CandyType.ORANGE_HEART, Modifier.size(66.dp))
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text("PERSONAL BEST", color = CandyMint, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Text(entry.score.toString(), color = CandyCream, fontSize = 28.sp, fontWeight = FontWeight.Black)
        }
        Text("#1", color = CandyOrange, fontSize = 30.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun LeaderboardRow(rank: Int, entry: LeaderboardEntry) {
    val shape = RoundedCornerShape(16.dp)
    val accent = when (rank) {
        1 -> CandyOrange
        2 -> CandyAqua
        3 -> CandyMint
        else -> CandyCream.copy(alpha = .40f)
    }
    Row(
        Modifier
            .fillMaxWidth()
            .background(CandyDeepPurple.copy(alpha = .78f), shape)
            .border(1.5.dp, accent, shape)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
            Text("#$rank", color = accent, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
        Column(Modifier.weight(1f)) {
            Text(entry.score.toString(), color = CandyCream, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text(
                "LEVEL ${entry.level}  •  COMBO ×${entry.maxCombo}",
                color = CandyCream.copy(alpha = .66f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            formatDate(entry.playedAtMillis).uppercase(),
            color = CandyAqua,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd MMM", Locale.US).format(Date(timestamp))
