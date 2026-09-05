package studio.cortex.gummypuzzledrop.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.isActive
import studio.cortex.gummypuzzledrop.presentation.AppScreen
import studio.cortex.gummypuzzledrop.presentation.GameOverlay
import studio.cortex.gummypuzzledrop.presentation.GameViewModel
import studio.cortex.gummypuzzledrop.ui.screens.GameplayScreen
import studio.cortex.gummypuzzledrop.ui.screens.AchievementsScreen
import studio.cortex.gummypuzzledrop.ui.screens.DailyChallengesScreen
import studio.cortex.gummypuzzledrop.ui.screens.LeaderboardScreen
import studio.cortex.gummypuzzledrop.ui.screens.LoadingScreen
import studio.cortex.gummypuzzledrop.ui.screens.MainMenuScreen
import studio.cortex.gummypuzzledrop.ui.screens.SettingsScreen
import studio.cortex.gummypuzzledrop.ui.screens.ShopScreen
import studio.cortex.gummypuzzledrop.ui.screens.GummyBlitzScreen
import studio.cortex.gummypuzzledrop.game.minigame.BlitzPhase

@Composable
fun GummyPuzzleDropApp(model: GameViewModel) {
    val progress by model.progress.collectAsStateWithLifecycle()
    val ready by model.progressReady.collectAsStateWithLifecycle()
    val screen by model.screen.collectAsStateWithLifecycle()
    val game by model.game.collectAsStateWithLifecycle()
    val overlay by model.overlay.collectAsStateWithLifecycle()
    val comboBanner by model.comboBanner.collectAsStateWithLifecycle()
    val runRewards by model.runRewards.collectAsStateWithLifecycle()
    val shopMessage by model.shopMessage.collectAsStateWithLifecycle()
    val blitz by model.blitz.collectAsStateWithLifecycle()
    val blitzReward by model.blitzReward.collectAsStateWithLifecycle()

    val view = LocalView.current
    SideEffect {
        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
        window.statusBarColor = CandyPink.toArgb()
        window.navigationBarColor = CandyDeepPurple.toArgb()
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }

    BackHandler(enabled = screen != AppScreen.MENU) { model.handleBack() }

    LaunchedEffect(screen, overlay, game?.phase) {
        if (screen != AppScreen.GAMEPLAY || overlay != GameOverlay.NONE || game == null) return@LaunchedEffect
        var previous = withFrameNanos { it }
        while (isActive) {
            withFrameNanos { now ->
                val deltaMs = ((now - previous) / 1_000_000L).coerceAtLeast(0L)
                previous = now
                model.advance(deltaMs)
            }
        }
    }

    LaunchedEffect(screen, blitz.phase) {
        if (screen != AppScreen.BLITZ || blitz.phase != BlitzPhase.PLAYING) return@LaunchedEffect
        var previous = withFrameNanos { it }
        while (isActive) {
            withFrameNanos { now ->
                val deltaMs = ((now - previous) / 1_000_000L).coerceAtLeast(0L)
                previous = now
                model.advanceBlitz(deltaMs)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (!ready) {
            LoadingScreen()
        } else {
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    if (progress.reducedMotion) EnterTransition.None togetherWith ExitTransition.None
                    else fadeIn() togetherWith fadeOut()
                },
                label = "screen",
            ) { target ->
                when (target) {
                    AppScreen.MENU -> MainMenuScreen(
                        progress,
                        model::startGame,
                        model::openDaily,
                        model::openAchievements,
                        model::openLeaderboard,
                        model::openShop,
                        model::openBlitz,
                        model::openSettings,
                    )
                    AppScreen.SETTINGS -> SettingsScreen(
                        progress,
                        model::setMusic,
                        model::setSound,
                        model::setVibration,
                        model::setGhost,
                        model::setReducedMotion,
                        model::replayTutorial,
                        model::closeSettings,
                    )
                    AppScreen.ACHIEVEMENTS -> AchievementsScreen(progress, model::closeSettings)
                    AppScreen.DAILY -> DailyChallengesScreen(
                        progress,
                        model::startGame,
                        model::refreshDaily,
                        model::closeSettings,
                    )
                    AppScreen.LEADERBOARD -> LeaderboardScreen(progress, model::startGame, model::closeSettings)
                    AppScreen.SHOP -> ShopScreen(
                        progress,
                        shopMessage,
                        model::buyCosmetic,
                        model::equipCosmetic,
                        model::buyPowerTreat,
                        model::closeSettings,
                    )
                    AppScreen.BLITZ -> GummyBlitzScreen(
                        blitz,
                        progress,
                        blitzReward,
                        model::startBlitz,
                        model::tapBlitz,
                        model::pauseBlitz,
                        model::resumeBlitz,
                        model::closeSettings,
                    )
                    AppScreen.GAMEPLAY -> game?.let { state ->
                        GameplayScreen(
                            state = state,
                            progress = progress,
                            overlay = overlay,
                            comboBanner = comboBanner,
                            runRewards = runRewards,
                            showGhost = progress.ghostEnabled,
                            reducedMotion = progress.reducedMotion,
                            onMove = model::move,
                            onRotate = model::rotate,
                            onHardDrop = model::hardDrop,
                            onHold = model::hold,
                            onPause = model::pause,
                            onResume = model::resume,
                            onRestart = model::restart,
                            onHome = model::goHome,
                            onDismissTutorial = model::dismissTutorial,
                            onDismissCombo = model::dismissComboBanner,
                            onUsePowerTreat = model::usePowerTreat,
                        )
                    } ?: MainMenuScreen(
                        progress,
                        model::startGame,
                        model::openDaily,
                        model::openAchievements,
                        model::openLeaderboard,
                        model::openShop,
                        model::openBlitz,
                        model::openSettings,
                    )
                }
            }
        }
    }
}
