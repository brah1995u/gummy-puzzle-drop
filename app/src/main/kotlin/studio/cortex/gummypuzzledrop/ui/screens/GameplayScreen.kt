package studio.cortex.gummypuzzledrop.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import studio.cortex.gummypuzzledrop.R
import studio.cortex.gummypuzzledrop.data.PlayerProgress
import studio.cortex.gummypuzzledrop.game.model.BombPieceSpec
import studio.cortex.gummypuzzledrop.game.model.GameState
import studio.cortex.gummypuzzledrop.game.model.NormalPieceSpec
import studio.cortex.gummypuzzledrop.presentation.ComboBanner
import studio.cortex.gummypuzzledrop.presentation.GameOverlay
import studio.cortex.gummypuzzledrop.presentation.RunRewards
import studio.cortex.gummypuzzledrop.ui.CandyAqua
import studio.cortex.gummypuzzledrop.ui.CandyCream
import studio.cortex.gummypuzzledrop.ui.CandyDeepPurple
import studio.cortex.gummypuzzledrop.ui.CandyHotPink
import studio.cortex.gummypuzzledrop.ui.CandyMint
import studio.cortex.gummypuzzledrop.ui.CandyOrange
import studio.cortex.gummypuzzledrop.ui.CandyPurple
import studio.cortex.gummypuzzledrop.ui.InkPurple
import studio.cortex.gummypuzzledrop.ui.components.CandyButton
import studio.cortex.gummypuzzledrop.ui.components.CandyMiniButton
import studio.cortex.gummypuzzledrop.ui.components.CandyPanel
import studio.cortex.gummypuzzledrop.ui.components.CandyPreviewFrame
import studio.cortex.gummypuzzledrop.ui.components.PauseIconButton
import studio.cortex.gummypuzzledrop.ui.components.StatPill
import studio.cortex.gummypuzzledrop.ui.components.TutorialGestureIcon
import studio.cortex.gummypuzzledrop.ui.components.GummyNavigationRow
import studio.cortex.gummypuzzledrop.ui.gameplay.CandyImage
import studio.cortex.gummypuzzledrop.ui.gameplay.GameBoard
import studio.cortex.gummypuzzledrop.ui.gameplay.PiecePreview
import studio.cortex.gummypuzzledrop.game.model.CandyType
import studio.cortex.gummypuzzledrop.game.model.PowerTreat

@Composable
fun GameplayScreen(
    state: GameState,
    progress: PlayerProgress,
    overlay: GameOverlay,
    comboBanner: ComboBanner?,
    runRewards: RunRewards,
    showGhost: Boolean,
    reducedMotion: Boolean,
    onMove: (Int) -> Unit,
    onRotate: () -> Unit,
    onHardDrop: () -> Unit,
    onHold: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit,
    onDismissTutorial: () -> Unit,
    onDismissCombo: (Long) -> Unit,
    onUsePowerTreat: (PowerTreat) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Image(
            painterResource(R.drawable.bg_sprinkles),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0x1875198A), CandyDeepPurple.copy(alpha = .30f))))
        )
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 7.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                StatPill("Score", state.score.toString(), Modifier.weight(1f))
                StatPill("Level", state.level.toString())
                PauseIconButton(onPause)
            }
            Spacer(Modifier.height(8.dp))
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                val sideWidth = (maxWidth * .16f).coerceIn(50.dp, 70.dp)
                val availableBoardWidth = (maxWidth - sideWidth * 2 - 12.dp).coerceAtLeast(120.dp)
                val boardWidth = minOf(availableBoardWidth, maxHeight * (8f / 14f))
                val boardHeight = boardWidth * (14f / 8f)

                Row(
                    Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    SidePreview(
                        label = "HOLD",
                        spec = state.hold,
                        width = sideWidth,
                        enabled = state.holdAvailable && state.active?.spec is NormalPieceSpec,
                        onClick = onHold,
                    )
                    Spacer(Modifier.width(6.dp))
                    GameBoard(
                        state = state,
                        onMove = onMove,
                        onRotate = onRotate,
                        onHardDrop = onHardDrop,
                        showGhost = showGhost,
                        reducedMotion = reducedMotion,
                        boardTheme = progress.equippedBoard,
                        effect = progress.equippedEffect,
                        modifier = Modifier.width(boardWidth).height(boardHeight),
                    )
                    Spacer(Modifier.width(6.dp))
                    SidePreview(
                        label = "NEXT",
                        spec = state.next,
                        width = sideWidth,
                        enabled = false,
                        onClick = {},
                    )
                }
            }
            Text(
                text = if (state.active?.spec === BombPieceSpec) "PINK BOMB — CLEAR 3 × 3" else "DRAG • TAP • SWIPE DOWN",
                modifier = Modifier.fillMaxWidth().padding(top = 3.dp),
                color = if (state.active?.spec === BombPieceSpec) CandyCream else CandyMint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = .7.sp,
                textAlign = TextAlign.Center,
            )
            PowerTreatDock(progress.powerTreats, onUsePowerTreat)
        }

        ComboCallout(comboBanner, onDismissCombo, reducedMotion, Modifier.align(Alignment.Center))

        when (overlay) {
            GameOverlay.NONE -> Unit
            GameOverlay.TUTORIAL -> TutorialOverlay(onDismissTutorial)
            GameOverlay.PAUSE -> PauseOverlay(onResume, onRestart, onHome)
            GameOverlay.GAME_OVER -> RunResultOverlay(state, progress, runRewards, onRestart, onHome)
        }
    }
}

@Composable
private fun PowerTreatDock(
    inventory: Map<PowerTreat, Int>,
    onUse: (PowerTreat) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(top = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        PowerTreat.entries.forEach { treat ->
            val count = inventory[treat] ?: 0
            CandyMiniButton(
                text = "${powerTreatLabel(treat)} ×$count",
                onClick = { onUse(treat) },
                modifier = Modifier.weight(1f).height(46.dp),
                orange = treat == PowerTreat.RAINBOW_POP,
                enabled = count > 0,
                iconRes = powerTreatDrawable(treat),
            )
        }
    }
}

private fun powerTreatLabel(treat: PowerTreat): String = when (treat) {
    PowerTreat.PINK_BOMB -> "BOMB"
    PowerTreat.RAINBOW_POP -> "POP"
    PowerTreat.SWEET_CLEANUP -> "CLEAN"
}

private fun powerTreatDrawable(treat: PowerTreat): Int = when (treat) {
    PowerTreat.PINK_BOMB -> R.drawable.gummy_pink_bomb
    PowerTreat.RAINBOW_POP -> R.drawable.gummy_green_star
    PowerTreat.SWEET_CLEANUP -> R.drawable.gummy_purple_bear
}

@Composable
private fun SidePreview(
    label: String,
    spec: NormalPieceSpec?,
    width: Dp,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val visualEnabled = enabled || label == "NEXT"
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) .94f else 1f,
        animationSpec = tween(85),
        label = "sidePreviewPress",
    )
    Box(
        modifier = Modifier
            .width(width)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        CandyPreviewFrame(
            drawable = if (label == "HOLD") R.drawable.ui_hold_frame else R.drawable.ui_next_frame,
            modifier = Modifier.matchParentSize().alpha(if (visualEnabled) 1f else .55f),
        )
        Column(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                label,
                color = InkPurple.copy(alpha = if (visualEnabled) 1f else .55f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
            )
            PiecePreview(spec, Modifier.fillMaxWidth().height(width))
            if (label == "HOLD") {
                Text(
                    if (enabled) "TAP" else if (spec == null) "EMPTY" else "USED",
                    color = InkPurple.copy(alpha = if (enabled) .78f else .42f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ComboCallout(
    banner: ComboBanner?,
    onDismiss: (Long) -> Unit,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(banner?.token) {
        val value = banner ?: return@LaunchedEffect
        delay(920)
        onDismiss(value.token)
    }
    AnimatedVisibility(
        visible = banner != null,
        modifier = modifier,
        enter = if (reducedMotion) EnterTransition.None else fadeIn() + scaleIn(initialScale = .62f),
        exit = if (reducedMotion) ExitTransition.None else fadeOut() + scaleOut(targetScale = 1.18f),
    ) {
        banner?.let { value ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (value.combo >= 3) "BOMB READY!" else if (value.combo == 2) "SWEET CASCADE!" else "GUMMY POP!",
                    color = if (value.combo >= 3) CandyOrange else CandyCream,
                    fontSize = if (value.combo >= 3) 29.sp else 25.sp,
                    fontWeight = FontWeight.Black,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(InkPurple, androidx.compose.ui.geometry.Offset(0f, 5f), 4f),
                    ),
                )
                Text(
                    "COMBO ×${value.combo}   +${value.points}",
                    color = CandyAqua,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = Shadow(InkPurple, androidx.compose.ui.geometry.Offset(0f, 3f), 2f),
                    ),
                )
            }
        }
    }
}

@Composable
private fun TutorialOverlay(onDismiss: () -> Unit) {
    ModalScrim {
        CandyPanel(Modifier.fillMaxWidth(.90f), contentPadding = 18.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("HOW TO PLAY", color = CandyCream, fontSize = 27.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(14.dp))
                TutorialLine(0, "Drag left or right to move")
                TutorialLine(1, "Tap the board to rotate")
                TutorialLine(2, "Swipe down fast to drop")
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CandyImage(CandyType.GREEN_BEAR, Modifier.size(38.dp))
                    CandyImage(CandyType.GREEN_BEAR, Modifier.size(38.dp))
                    CandyImage(CandyType.GREEN_BEAR, Modifier.size(38.dp))
                    Text(
                        "Match 3 identical gummies",
                        modifier = Modifier.padding(start = 10.dp).weight(1f),
                        color = CandyCream,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(8.dp))
                CandyButton(
                    "LET'S PLAY", onDismiss, Modifier.fillMaxWidth(), orange = true,
                    iconRes = R.drawable.gummy_green_bear,
                )
            }
        }
    }
}

@Composable
private fun TutorialLine(icon: Int, text: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TutorialGestureIcon(icon)
        Text(
            text,
            modifier = Modifier.padding(start = 12.dp),
            color = CandyCream,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun PauseOverlay(onResume: () -> Unit, onRestart: () -> Unit, onHome: () -> Unit) {
    ModalScrim {
        CandyPanel(Modifier.fillMaxWidth(.86f), contentPadding = 20.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GummyNavigationRow(onBack = onResume, onHome = onHome)
                Spacer(Modifier.height(8.dp))
                Text("PAUSED", color = CandyCream, fontSize = 31.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(18.dp))
                CandyButton(
                    "RESUME", onResume, Modifier.fillMaxWidth(), orange = true,
                    iconRes = R.drawable.gummy_green_star,
                )
                Spacer(Modifier.height(10.dp))
                CandyButton("RESTART", onRestart, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun RunResultOverlay(
    state: GameState,
    progress: PlayerProgress,
    rewards: RunRewards,
    onRestart: () -> Unit,
    onHome: () -> Unit,
) {
    val victory = rewards.dailySetCompleted
    ModalScrim {
        CandyPanel(Modifier.fillMaxWidth(.90f), contentPadding = 16.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GummyNavigationRow(
                    onBack = onHome,
                    onHome = onHome,
                    buttonSize = 58.dp,
                )
                Spacer(Modifier.height(4.dp))
                Image(
                    painterResource(if (victory) R.drawable.gummy_victory_bear else R.drawable.gummy_loss_bear),
                    contentDescription = if (victory) "Happy crowned gummy bear" else "Sad purple gummy bear",
                    modifier = Modifier.size(if (victory) 118.dp else 105.dp),
                    contentScale = ContentScale.Fit,
                )
                Text(
                    if (victory) "DAILY VICTORY!" else "GAME OVER",
                    color = if (victory) CandyOrange else CandyCream,
                    fontSize = if (victory) 28.sp else 30.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
                Text(
                    if (victory) "ALL 3 SWEET GOALS COMPLETE" else "THE BOARD IS FULL — TRY ANOTHER DROP",
                    color = CandyAqua,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(9.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatPill("Score", state.score.toString())
                    StatPill("Best", maxOf(progress.highScore, state.score).toString())
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 7.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CandyImage(CandyType.GREEN_STAR, Modifier.size(27.dp))
                    Text(
                        "+${rewards.starsEarned} SUGAR STARS  •  COMBO ×${state.maxCombo}",
                        color = CandyCream,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
                rewards.newAchievements.firstOrNull()?.let { achievement ->
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "NEW ACHIEVEMENT: ${achievement.title.uppercase()}" +
                            if (rewards.newAchievements.size > 1) " +${rewards.newAchievements.size - 1}" else "",
                        color = CandyOrange,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                    )
                }
                if (rewards.dailySetCompleted) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "DAILY SET COMPLETE • STREAK ${progress.dailyStreak}",
                        color = CandyAqua,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(11.dp))
                CandyButton(
                    "PLAY AGAIN", onRestart, Modifier.fillMaxWidth(), orange = true,
                    iconRes = R.drawable.gummy_green_bear,
                )
            }
        }
    }
}

@Composable
private fun ModalScrim(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(CandyDeepPurple.copy(alpha = .74f))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
