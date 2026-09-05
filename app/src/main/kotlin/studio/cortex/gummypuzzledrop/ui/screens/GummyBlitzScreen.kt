package studio.cortex.gummypuzzledrop.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import studio.cortex.gummypuzzledrop.R
import studio.cortex.gummypuzzledrop.data.LocalDayClock
import studio.cortex.gummypuzzledrop.data.PlayerProgress
import studio.cortex.gummypuzzledrop.game.minigame.BlitzPhase
import studio.cortex.gummypuzzledrop.game.minigame.BlitzReward
import studio.cortex.gummypuzzledrop.game.minigame.GummyBlitzState
import studio.cortex.gummypuzzledrop.game.model.CandyType
import studio.cortex.gummypuzzledrop.game.model.PowerTreat
import studio.cortex.gummypuzzledrop.ui.CandyAqua
import studio.cortex.gummypuzzledrop.ui.CandyCream
import studio.cortex.gummypuzzledrop.ui.CandyDeepPurple
import studio.cortex.gummypuzzledrop.ui.CandyMint
import studio.cortex.gummypuzzledrop.ui.CandyOrange
import studio.cortex.gummypuzzledrop.ui.CandyPurple
import studio.cortex.gummypuzzledrop.ui.components.CandyButton
import studio.cortex.gummypuzzledrop.ui.components.CandyPanel
import studio.cortex.gummypuzzledrop.ui.components.GameLogo
import studio.cortex.gummypuzzledrop.ui.components.GummyNavigationRow
import studio.cortex.gummypuzzledrop.ui.components.PauseIconButton
import studio.cortex.gummypuzzledrop.ui.components.StatPill
import studio.cortex.gummypuzzledrop.ui.gameplay.CandyImage

@Composable
fun GummyBlitzScreen(
    state: GummyBlitzState,
    progress: PlayerProgress,
    reward: BlitzReward?,
    onStart: () -> Unit,
    onTap: (Int) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onHome: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Image(
            painterResource(R.drawable.bg_sprinkles),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(CandyDeepPurple.copy(alpha = .15f), Color(0x995A176D))),
            ),
        )
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GummyNavigationRow(
                onBack = if (state.phase == BlitzPhase.PLAYING) onPause else onHome,
                onHome = onHome,
                buttonSize = 60.dp,
            )
            Spacer(Modifier.height(4.dp))
            GameLogo(compact = true)
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("GUMMY BLITZ", color = CandyCream, fontSize = 25.sp, fontWeight = FontWeight.Black)
                    Text("FIND THE TARGET • BUILD COMBO", color = CandyMint, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
                if (state.phase == BlitzPhase.PLAYING) PauseIconButton(onPause)
            }
            Spacer(Modifier.height(8.dp))
            CandyPanel(Modifier.fillMaxWidth().weight(1f), contentPadding = 14.dp) {
                when (state.phase) {
                    BlitzPhase.READY -> BlitzReady(progress, onStart)
                    BlitzPhase.PLAYING -> BlitzPlaying(state, onTap)
                    BlitzPhase.PAUSED -> BlitzPaused(onResume, onStart)
                    BlitzPhase.FINISHED -> BlitzFinished(state, progress, reward, onStart)
                }
            }
        }
    }
}

@Composable
private fun BlitzReady(progress: PlayerProgress, onStart: () -> Unit) {
    val rewardReady = progress.lastBlitzRewardDay != LocalDayClock.dayIndex()
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            CandyImage(CandyType.GREEN_BEAR, Modifier.size(62.dp))
            CandyImage(CandyType.PURPLE_BEAR, Modifier.size(62.dp))
            CandyImage(CandyType.RED_BEAR, Modifier.size(62.dp))
        }
        Text("20 SECOND SUGAR RUSH", color = CandyCream, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text(
            "Tap only the gummy shown as TARGET.\nCorrect taps build combo. Three misses end the run.",
            modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp),
            color = CandyCream.copy(alpha = .78f),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            if (rewardReady) "DAILY REWARD READY • STARS + POWER TREAT" else "PRACTICE RUN • DAILY REWARD ALREADY CLAIMED",
            color = if (rewardReady) CandyOrange else CandyAqua,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Text("BEST  ${progress.bestBlitzScore}", color = CandyAqua, fontSize = 13.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(16.dp))
        CandyButton(
            "START BLITZ", onStart, Modifier.fillMaxWidth(), orange = true,
            iconRes = R.drawable.gummy_purple_bear,
        )
    }
}

@Composable
private fun BlitzPlaying(state: GummyBlitzState, onTap: (Int) -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatPill("Score", state.score.toString())
            StatPill("Time", ceil(state.remainingMs / 1000.0).toInt().toString())
            StatPill("Lives", "♥".repeat(state.lives))
        }
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp)
                .background(Color(0xFF681A86), RoundedCornerShape(18.dp))
                .border(1.5.dp, CandyAqua, RoundedCornerShape(18.dp)).padding(7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("TARGET", color = CandyAqua, fontSize = 11.sp, fontWeight = FontWeight.Black)
            CandyImage(state.target, Modifier.size(58.dp))
            Text("COMBO ×${state.combo}", color = CandyCream, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
        Column(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.Center) {
            state.cells.chunked(3).forEachIndexed { row, candies ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    candies.forEachIndexed { column, candy ->
                        BlitzCell(candy, row * 3 + column, onTap, Modifier.weight(1f))
                    }
                }
                if (row < 2) Spacer(Modifier.height(8.dp))
            }
        }
        Text("FAST EYES • CLEAN TAPS", color = CandyMint, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun BlitzCell(candy: CandyType, index: Int, onTap: (Int) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier.height(104.dp)
            .background(Brush.verticalGradient(listOf(Color(0xFF9325AE), Color(0xFF57166D))), shape)
            .border(2.dp, CandyCream.copy(alpha = .78f), shape)
            .clickable(role = Role.Button) { onTap(index) },
        contentAlignment = Alignment.Center,
    ) {
        CandyImage(candy, Modifier.size(89.dp))
    }
}

@Composable
private fun BlitzPaused(onResume: () -> Unit, onRestart: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CandyImage(CandyType.PURPLE_BEAR, Modifier.size(112.dp))
        Text("BLITZ PAUSED", color = CandyCream, fontSize = 27.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(16.dp))
        CandyButton(
            "RESUME", onResume, Modifier.fillMaxWidth(), orange = true,
            iconRes = R.drawable.gummy_green_star,
        )
        Spacer(Modifier.height(9.dp))
        CandyButton("RESTART", onRestart, Modifier.fillMaxWidth())
    }
}

@Composable
private fun BlitzFinished(
    state: GummyBlitzState,
    progress: PlayerProgress,
    reward: BlitzReward?,
    onRestart: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painterResource(R.drawable.gummy_victory_bear),
            contentDescription = "Happy gummy bear",
            modifier = Modifier.size(125.dp),
            contentScale = ContentScale.Fit,
        )
        Text("BLITZ COMPLETE!", color = CandyOrange, fontSize = 25.sp, fontWeight = FontWeight.Black)
        Text(state.score.toString(), color = CandyCream, fontSize = 39.sp, fontWeight = FontWeight.Black)
        Text("BEST COMBO ×${state.bestCombo}  •  BEST ${maxOf(progress.bestBlitzScore, state.score)}", color = CandyAqua, fontSize = 11.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        if (reward != null && reward.stars > 0) {
            Text("+${reward.stars} SUGAR STARS", color = CandyCream, fontSize = 15.sp, fontWeight = FontWeight.Black)
            reward.powerTreat?.let {
                Text("BONUS: ${powerTreatTitle(it)}", color = CandyOrange, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        } else {
            Text("PRACTICE COMPLETE • DAILY REWARD TOMORROW", color = CandyMint, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(15.dp))
        CandyButton(
            "PLAY AGAIN", onRestart, Modifier.fillMaxWidth(), orange = true,
            iconRes = R.drawable.gummy_purple_bear,
        )
    }
}

private fun powerTreatTitle(treat: PowerTreat): String = when (treat) {
    PowerTreat.PINK_BOMB -> "PINK BOMB"
    PowerTreat.RAINBOW_POP -> "RAINBOW POP"
    PowerTreat.SWEET_CLEANUP -> "SWEET CLEANUP"
}
