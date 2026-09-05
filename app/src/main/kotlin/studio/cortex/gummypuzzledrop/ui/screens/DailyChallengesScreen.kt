package studio.cortex.gummypuzzledrop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import studio.cortex.gummypuzzledrop.R
import studio.cortex.gummypuzzledrop.data.DailyChallenge
import studio.cortex.gummypuzzledrop.data.DailyMetric
import studio.cortex.gummypuzzledrop.data.LocalDayClock
import studio.cortex.gummypuzzledrop.data.MetaProgressRules
import studio.cortex.gummypuzzledrop.data.PlayerProgress
import studio.cortex.gummypuzzledrop.game.model.CandyType
import studio.cortex.gummypuzzledrop.ui.CandyAqua
import studio.cortex.gummypuzzledrop.ui.CandyCream
import studio.cortex.gummypuzzledrop.ui.CandyDeepPurple
import studio.cortex.gummypuzzledrop.ui.CandyMint
import studio.cortex.gummypuzzledrop.ui.components.CandyButton
import studio.cortex.gummypuzzledrop.ui.components.CandyProgressBar
import studio.cortex.gummypuzzledrop.ui.components.StatPill
import studio.cortex.gummypuzzledrop.ui.gameplay.CandyImage

@Composable
fun DailyChallengesScreen(
    progress: PlayerProgress,
    onPlay: () -> Unit,
    onRefreshDay: () -> Unit,
    onBack: () -> Unit,
) {
    val challenges = MetaProgressRules.dailyChallenges(progress.daily)
    val completed = challenges.count(DailyChallenge::complete)
    val remaining by produceState(initialValue = LocalDayClock.millisUntilNextDay()) {
        while (true) {
            onRefreshDay()
            value = LocalDayClock.millisUntilNextDay()
            delay(30_000L)
        }
    }
    MetaScreenScaffold(
        title = "DAILY SUGAR RUSH",
        subtitle = "Fresh goals in ${formatCountdown(remaining)} — progress uses your local day",
        onBack = onBack,
    ) {
        LazyColumn(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    StatPill("Done", "$completed/3")
                    StatPill("Streak", "${progress.dailyStreak} days")
                }
            }
            items(challenges, key = { it.id }) { challenge -> DailyChallengeCard(challenge) }
            item {
                if (completed == challenges.size) {
                    Text(
                        "DAILY SET COMPLETE — COME BACK TOMORROW",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        color = CandyAqua,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
                CandyButton(
                    "PLAY NOW", onPlay, Modifier.fillMaxWidth(), orange = true,
                    iconRes = R.drawable.gummy_green_bear,
                )
                Spacer(Modifier.height(2.dp))
            }
        }
    }
}

@Composable
private fun DailyChallengeCard(challenge: DailyChallenge) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .background(CandyDeepPurple.copy(alpha = .80f), shape)
            .border(1.5.dp, if (challenge.complete) CandyAqua else CandyCream.copy(alpha = .42f), shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        CandyImage(challengeCandy(challenge.metric), Modifier.size(58.dp))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    challenge.title.uppercase(),
                    modifier = Modifier.weight(1f),
                    color = if (challenge.complete) CandyMint else CandyCream,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    if (challenge.complete) "DONE" else "${challenge.progress.coerceAtMost(challenge.target)}/${challenge.target}",
                    color = if (challenge.complete) CandyAqua else CandyCream.copy(alpha = .76f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            Text(challenge.description, color = CandyCream.copy(alpha = .70f), fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            CandyProgressBar(challenge.fraction, completed = challenge.complete)
        }
    }
}

private fun challengeCandy(metric: DailyMetric): CandyType = when (metric) {
    DailyMetric.BEST_SCORE -> CandyType.ORANGE_HEART
    DailyMetric.CANDIES -> CandyType.PURPLE_BEAR
    DailyMetric.PIECES -> CandyType.GREEN_BEAR
    DailyMetric.GAMES -> CandyType.RED_BEAR
    DailyMetric.BEST_COMBO -> CandyType.GREEN_STAR
    DailyMetric.BOMBS -> CandyType.PINK_BOMB
}

private fun formatCountdown(milliseconds: Long): String {
    val totalMinutes = (milliseconds / 60_000L).coerceAtLeast(0L)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "%02dh %02dm".format(hours, minutes)
}
