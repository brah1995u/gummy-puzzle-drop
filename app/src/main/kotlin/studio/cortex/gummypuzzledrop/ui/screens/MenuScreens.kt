package studio.cortex.gummypuzzledrop.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.cortex.gummypuzzledrop.R
import studio.cortex.gummypuzzledrop.data.LocalDayClock
import studio.cortex.gummypuzzledrop.data.MetaProgressRules
import studio.cortex.gummypuzzledrop.data.PlayerProgress
import studio.cortex.gummypuzzledrop.ui.CandyCream
import studio.cortex.gummypuzzledrop.ui.CandyDeepPurple
import studio.cortex.gummypuzzledrop.ui.CandyMint
import studio.cortex.gummypuzzledrop.ui.components.CandyButton
import studio.cortex.gummypuzzledrop.ui.components.CandyMiniButton
import studio.cortex.gummypuzzledrop.ui.components.CandyPanel
import studio.cortex.gummypuzzledrop.ui.components.CandyToggle
import studio.cortex.gummypuzzledrop.ui.components.GameLogo
import studio.cortex.gummypuzzledrop.ui.components.GummyNavigationRow
import studio.cortex.gummypuzzledrop.ui.components.StatPill
import studio.cortex.gummypuzzledrop.ui.gameplay.CandyImage
import studio.cortex.gummypuzzledrop.game.model.CandyType

@Composable
fun LoadingScreen() {
    Box(Modifier.fillMaxSize()) {
        Image(
            painterResource(R.drawable.bg_candy_land),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = .08f), CandyDeepPurple.copy(alpha = .42f))))
        )
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CandyImage(CandyType.PURPLE_BEAR, Modifier.size(126.dp))
            GameLogo()
        }
    }
}

@Composable
fun MainMenuScreen(
    progress: PlayerProgress,
    onPlay: () -> Unit,
    onDaily: () -> Unit,
    onAchievements: () -> Unit,
    onLeaderboard: () -> Unit,
    onShop: () -> Unit,
    onBlitz: () -> Unit,
    onSettings: () -> Unit,
) {
    val dailyDone = MetaProgressRules.dailyChallenges(progress.daily).count { it.complete }
    val unlocked = MetaProgressRules.achievements(progress).count { it.unlocked }
    val blitzRewardReady = progress.lastBlitzRewardDay != LocalDayClock.dayIndex()
    Box(Modifier.fillMaxSize()) {
        Image(
            painterResource(R.drawable.bg_candy_land),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.White.copy(alpha = .04f),
                        .48f to Color.Transparent,
                        1f to CandyDeepPurple.copy(alpha = .66f),
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))
            GameLogo()
            MenuMascotShowcase(
                reducedMotion = progress.reducedMotion,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatPill("Best", progress.highScore.toString())
                StatPill("Stars", "★${progress.sugarStars}")
                StatPill("Streak", progress.dailyStreak.toString())
            }
            Spacer(Modifier.height(18.dp))
            CandyPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 18.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CandyButton(
                        "PLAY", onPlay, Modifier.fillMaxWidth(), orange = true,
                        iconRes = R.drawable.gummy_green_bear,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        CandyMiniButton(
                            "DAILY", onDaily, Modifier.weight(1f),
                            iconRes = R.drawable.gummy_orange_heart,
                            badge = "$dailyDone/3",
                        )
                        CandyMiniButton(
                            "ACHIEVEMENTS", onAchievements, Modifier.weight(1f),
                            iconRes = R.drawable.gummy_green_star,
                            badge = "$unlocked/10",
                        )
                    }
                    Spacer(Modifier.height(9.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        CandyMiniButton(
                            "LEADERBOARD", onLeaderboard, Modifier.weight(1f),
                            iconRes = R.drawable.gummy_red_bear,
                            badge = if (progress.leaderboard.isEmpty()) "NEW" else "TOP",
                        )
                        CandyMiniButton(
                            "SUGAR SHOP", onShop, Modifier.weight(1f), orange = true,
                            iconRes = R.drawable.gummy_pink_bomb,
                            badge = "★${progress.sugarStars}",
                        )
                    }
                    Spacer(Modifier.height(9.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        CandyMiniButton(
                            "GUMMY BLITZ", onBlitz, Modifier.weight(1f), orange = true,
                            iconRes = R.drawable.gummy_purple_bear,
                            badge = if (blitzRewardReady) "GIFT" else "BEST",
                        )
                        CandyMiniButton(
                            "SETTINGS", onSettings, Modifier.weight(1f),
                            iconRes = R.drawable.ui_music_icon,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "DROP • MATCH • CASCADE",
                color = CandyMint,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MenuMascotShowcase(reducedMotion: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "menuMascots")
    val animatedBob by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(1_700), RepeatMode.Reverse),
        label = "menuMascotBob",
    )
    val bob = if (reducedMotion) 0.dp else animatedBob.dp
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(Modifier.offset(y = bob).size(width = 286.dp, height = 154.dp)) {
            val bubbleShape = RoundedCornerShape(48.dp)
            Box(
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(.84f)
                    .height(96.dp)
                    .shadow(10.dp, bubbleShape, spotColor = CandyDeepPurple.copy(alpha = .65f))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xB8FF87D1), Color(0xC97728A0), Color(0xB84BE6D1)),
                        ),
                        bubbleShape,
                    )
                    .border(1.5.dp, CandyCream.copy(alpha = .75f), bubbleShape),
            )
            Text(
                "SWEET CREW READY!",
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp)
                    .background(CandyDeepPurple.copy(alpha = .86f), RoundedCornerShape(50))
                    .border(1.dp, CandyCream.copy(alpha = .70f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                color = CandyCream,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = .8.sp,
            )
            CandyImage(
                CandyType.GREEN_STAR,
                Modifier.align(Alignment.TopStart).offset(x = 7.dp, y = 23.dp).size(58.dp).rotate(-10f),
            )
            CandyImage(
                CandyType.ORANGE_HEART,
                Modifier.align(Alignment.TopEnd).offset(x = (-7).dp, y = 27.dp).size(57.dp).rotate(10f),
            )
            CandyImage(
                CandyType.GREEN_BEAR,
                Modifier.align(Alignment.BottomStart).offset(x = 39.dp).size(78.dp).rotate(-3f),
            )
            CandyImage(
                CandyType.PURPLE_BEAR,
                Modifier.align(Alignment.BottomCenter).offset(y = (-3).dp).size(102.dp),
            )
            CandyImage(
                CandyType.RED_BEAR,
                Modifier.align(Alignment.BottomEnd).offset(x = (-39).dp).size(78.dp).rotate(3f),
            )
        }
    }
}

@Composable
fun SettingsScreen(
    progress: PlayerProgress,
    onMusic: (Boolean) -> Unit,
    onSound: (Boolean) -> Unit,
    onVibration: (Boolean) -> Unit,
    onGhost: (Boolean) -> Unit,
    onReducedMotion: (Boolean) -> Unit,
    onReplayTutorial: () -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit = onBack,
) {
    Box(Modifier.fillMaxSize()) {
        Image(
            painterResource(R.drawable.bg_sprinkles),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.fillMaxSize().background(CandyDeepPurple.copy(alpha = .20f)))
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GummyNavigationRow(onBack = onBack, onHome = onHome)
            Spacer(Modifier.height(4.dp))
            GameLogo(compact = true)
            Spacer(Modifier.height(14.dp))
            CandyPanel(Modifier.fillMaxWidth().weight(1f), contentPadding = 18.dp) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    Text(
                        "SETTINGS",
                        modifier = Modifier.fillMaxWidth(),
                        color = CandyCream,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                    )
                    SectionLabel("AUDIO")
                    CandyToggle(
                        "Music", "Candy-land ambient soundtrack", "MUS",
                        progress.musicEnabled, onMusic, motionEnabled = !progress.reducedMotion,
                        iconRes = R.drawable.ui_music_icon,
                    )
                    CandyToggle(
                        "Sound effects", "Moves, drops, clears and buttons", "SFX",
                        progress.soundEnabled, onSound, motionEnabled = !progress.reducedMotion,
                        iconRes = R.drawable.ui_sound_icon,
                    )
                    SectionLabel("GAME FEEL")
                    CandyToggle(
                        "Haptics", "Impact feedback for landings and combos", "HAP",
                        progress.vibrationEnabled, onVibration, motionEnabled = !progress.reducedMotion,
                        iconRes = R.drawable.gummy_pink_bomb,
                    )
                    CandyToggle(
                        "Ghost piece", "Show where the active piece will land", "GST",
                        progress.ghostEnabled, onGhost, motionEnabled = !progress.reducedMotion,
                        iconRes = R.drawable.gummy_green_star,
                    )
                    CandyToggle(
                        "Reduce motion", "Calmer transitions and no pulsing danger glow", "RM",
                        progress.reducedMotion, onReducedMotion, motionEnabled = !progress.reducedMotion,
                        iconRes = R.drawable.gummy_orange_heart,
                    )
                    Spacer(Modifier.height(12.dp))
                    CandyButton("REPLAY TUTORIAL", onReplayTutorial, Modifier.fillMaxWidth())
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        modifier = Modifier.fillMaxWidth().padding(top = 15.dp, bottom = 2.dp),
        color = CandyMint,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.1.sp,
    )
    Box(Modifier.fillMaxWidth().height(1.dp).background(CandyCream.copy(alpha = .24f)))
}
