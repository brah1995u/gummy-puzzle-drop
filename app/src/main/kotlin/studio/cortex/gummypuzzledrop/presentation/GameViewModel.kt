package studio.cortex.gummypuzzledrop.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.cortex.gummypuzzledrop.data.PlayerProgress
import studio.cortex.gummypuzzledrop.data.ProgressStore
import studio.cortex.gummypuzzledrop.data.CompletedGame
import studio.cortex.gummypuzzledrop.data.LocalDayClock
import studio.cortex.gummypuzzledrop.data.MetaProgressRules
import studio.cortex.gummypuzzledrop.data.EquipResult
import studio.cortex.gummypuzzledrop.data.PurchaseResult
import studio.cortex.gummypuzzledrop.data.PowerPurchaseResult
import studio.cortex.gummypuzzledrop.data.PowerTreatCatalog
import studio.cortex.gummypuzzledrop.data.ShopCatalog
import studio.cortex.gummypuzzledrop.data.ShopRules
import studio.cortex.gummypuzzledrop.feedback.FeedbackCue
import studio.cortex.gummypuzzledrop.feedback.FeedbackEvent
import studio.cortex.gummypuzzledrop.feedback.HapticStrength
import studio.cortex.gummypuzzledrop.game.engine.GameEngine
import studio.cortex.gummypuzzledrop.game.model.EngineResult
import studio.cortex.gummypuzzledrop.game.model.GameEvent
import studio.cortex.gummypuzzledrop.game.model.GamePhase
import studio.cortex.gummypuzzledrop.game.model.GameState
import studio.cortex.gummypuzzledrop.game.model.PowerTreat
import studio.cortex.gummypuzzledrop.game.minigame.BlitzPhase
import studio.cortex.gummypuzzledrop.game.minigame.BlitzReward
import studio.cortex.gummypuzzledrop.game.minigame.GummyBlitzEngine
import studio.cortex.gummypuzzledrop.game.minigame.GummyBlitzRewards
import studio.cortex.gummypuzzledrop.game.minigame.GummyBlitzState

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val store = ProgressStore(application)
    private val engine = GameEngine()
    private val blitzEngine = GummyBlitzEngine()

    private val _progress = MutableStateFlow(PlayerProgress())
    val progress: StateFlow<PlayerProgress> = _progress.asStateFlow()

    private val _progressReady = MutableStateFlow(false)
    val progressReady: StateFlow<Boolean> = _progressReady.asStateFlow()

    private val _screen = MutableStateFlow(AppScreen.MENU)
    val screen: StateFlow<AppScreen> = _screen.asStateFlow()

    private val _game = MutableStateFlow<GameState?>(null)
    val game: StateFlow<GameState?> = _game.asStateFlow()

    private val _overlay = MutableStateFlow(GameOverlay.NONE)
    val overlay: StateFlow<GameOverlay> = _overlay.asStateFlow()

    private val _comboBanner = MutableStateFlow<ComboBanner?>(null)
    val comboBanner: StateFlow<ComboBanner?> = _comboBanner.asStateFlow()

    private val _runRewards = MutableStateFlow(RunRewards())
    val runRewards: StateFlow<RunRewards> = _runRewards.asStateFlow()

    private val _shopMessage = MutableStateFlow<String?>(null)
    val shopMessage: StateFlow<String?> = _shopMessage.asStateFlow()

    private val _blitz = MutableStateFlow(blitzEngine.ready())
    val blitz: StateFlow<GummyBlitzState> = _blitz.asStateFlow()

    private val _blitzReward = MutableStateFlow<BlitzReward?>(null)
    val blitzReward: StateFlow<BlitzReward?> = _blitzReward.asStateFlow()

    private val _feedback = MutableSharedFlow<FeedbackEvent>(extraBufferCapacity = 32)
    val feedback = _feedback.asSharedFlow()

    private var engineState: GameState? = null
    private var comboToken = 0L
    private var resultCommitted = false
    private var sessionCandiesCleared = 0
    private var sessionPiecesPlaced = 0
    private var sessionBombsExploded = 0
    private var blitzResultCommitted = false

    init {
        viewModelScope.launch {
            store.progress.collect { value ->
                val normalized = MetaProgressRules.normalizeDay(value, LocalDayClock.dayIndex())
                _progress.value = normalized
                _progressReady.value = true
                if (normalized != value) store.save(normalized)
            }
        }
    }

    fun startGame() {
        resultCommitted = false
        sessionCandiesCleared = 0
        sessionPiecesPlaced = 0
        sessionBombsExploded = 0
        _runRewards.value = RunRewards()
        _comboBanner.value = null
        val tutorial = !_progress.value.tutorialSeen
        val initial = engine.newGame()
        handle(if (tutorial) engine.pause(initial.state) else initial)
        _screen.value = AppScreen.GAMEPLAY
        _overlay.value = if (tutorial) GameOverlay.TUTORIAL else GameOverlay.NONE
        cue(FeedbackCue.BUTTON)
    }

    fun advance(deltaMs: Long) {
        if (_screen.value != AppScreen.GAMEPLAY || _overlay.value != GameOverlay.NONE) return
        val current = engineState ?: return
        handle(engine.advance(current, deltaMs))
    }

    fun move(direction: Int) {
        if (!acceptsGameplayInput()) return
        val current = engineState ?: return
        val result = engine.moveHorizontal(current, direction)
        if (result.state != current) cue(FeedbackCue.MOVE)
        handle(result)
    }

    fun rotate() {
        if (!acceptsGameplayInput()) return
        val current = engineState ?: return
        val result = engine.rotateClockwise(current)
        if (result.state != current) cue(FeedbackCue.ROTATE)
        handle(result)
    }

    fun hardDrop() {
        if (!acceptsGameplayInput()) return
        val current = engineState ?: return
        val result = engine.hardDrop(current)
        if (result.state != current) cue(FeedbackCue.HARD_DROP, HapticStrength.MEDIUM)
        handle(result)
    }

    fun hold() {
        if (!acceptsGameplayInput()) return
        val current = engineState ?: return
        val result = engine.hold(current)
        if (result.state != current) cue(FeedbackCue.BUTTON)
        handle(result)
    }

    fun pause() {
        val current = engineState ?: return
        val result = engine.pause(current)
        if (result.state != current) {
            handle(result)
            _overlay.value = GameOverlay.PAUSE
            cue(FeedbackCue.BUTTON)
        }
    }

    fun resume() {
        val current = engineState ?: return
        val result = engine.resume(current)
        handle(result)
        if (result.state.phase != GamePhase.GAME_OVER) _overlay.value = GameOverlay.NONE
        cue(FeedbackCue.BUTTON)
    }

    fun restart() = startGame()

    fun goHome() {
        engineState = null
        _game.value = null
        _comboBanner.value = null
        _overlay.value = GameOverlay.NONE
        _screen.value = AppScreen.MENU
        cue(FeedbackCue.BUTTON)
    }

    fun openSettings() {
        _screen.value = AppScreen.SETTINGS
        cue(FeedbackCue.BUTTON)
    }

    fun openAchievements() = openMetaScreen(AppScreen.ACHIEVEMENTS)
    fun openDaily() {
        refreshDaily()
        openMetaScreen(AppScreen.DAILY)
    }
    fun openLeaderboard() = openMetaScreen(AppScreen.LEADERBOARD)
    fun openShop() {
        _shopMessage.value = null
        openMetaScreen(AppScreen.SHOP)
    }

    fun openBlitz() {
        _blitz.value = blitzEngine.ready()
        _blitzReward.value = null
        blitzResultCommitted = false
        _screen.value = AppScreen.BLITZ
        cue(FeedbackCue.BUTTON)
    }

    fun startBlitz() {
        _blitz.value = blitzEngine.start()
        _blitzReward.value = null
        blitzResultCommitted = false
        cue(FeedbackCue.BUTTON)
    }

    fun advanceBlitz(deltaMs: Long) {
        if (_screen.value != AppScreen.BLITZ) return
        val before = _blitz.value
        val after = blitzEngine.advance(before, deltaMs)
        if (after != before) _blitz.value = after
        if (after.phase == BlitzPhase.FINISHED) commitBlitz(after)
    }

    fun tapBlitz(index: Int) {
        val result = blitzEngine.tap(_blitz.value, index)
        if (result.state == _blitz.value) return
        _blitz.value = result.state
        cue(
            if (result.correct) FeedbackCue.POP else FeedbackCue.LAND,
            if (result.correct) HapticStrength.LIGHT else HapticStrength.MEDIUM,
        )
        if (result.state.phase == BlitzPhase.FINISHED) commitBlitz(result.state)
    }

    fun pauseBlitz() {
        _blitz.value = blitzEngine.pause(_blitz.value)
        cue(FeedbackCue.BUTTON)
    }

    fun resumeBlitz() {
        _blitz.value = blitzEngine.resume(_blitz.value)
        cue(FeedbackCue.BUTTON)
    }

    fun closeSettings() {
        _screen.value = AppScreen.MENU
        cue(FeedbackCue.BUTTON)
    }

    fun dismissTutorial() {
        saveProgress(_progress.value.copy(tutorialSeen = true))
        val current = engineState
        if (current?.phase == GamePhase.PAUSED) handle(engine.resume(current))
        _overlay.value = GameOverlay.NONE
        cue(FeedbackCue.BUTTON)
    }

    fun setMusic(enabled: Boolean) = saveProgress(_progress.value.copy(musicEnabled = enabled))
    fun setSound(enabled: Boolean) = saveProgress(_progress.value.copy(soundEnabled = enabled))
    fun setVibration(enabled: Boolean) = saveProgress(_progress.value.copy(vibrationEnabled = enabled))
    fun setGhost(enabled: Boolean) = saveProgress(_progress.value.copy(ghostEnabled = enabled))
    fun setReducedMotion(enabled: Boolean) = saveProgress(_progress.value.copy(reducedMotion = enabled))

    fun buyCosmetic(itemId: String) {
        val before = _progress.value
        val item = ShopCatalog.find(itemId)
        when (val result = ShopRules.purchase(before.sugarStars, before.ownedCosmetics, itemId)) {
            is PurchaseResult.Purchased -> {
                saveProgress(before.copy(sugarStars = result.balance, ownedCosmetics = result.owned))
                _shopMessage.value = "${item?.title.orEmpty().uppercase()} UNLOCKED!"
                cue(FeedbackCue.POP, HapticStrength.MEDIUM)
            }
            PurchaseResult.AlreadyOwned -> _shopMessage.value = "ALREADY IN YOUR COLLECTION"
            PurchaseResult.NotEnoughStars -> {
                val missing = ((item?.price ?: 0) - before.sugarStars).coerceAtLeast(0)
                _shopMessage.value = "EARN $missing MORE SUGAR STARS"
            }
            PurchaseResult.UnknownItem -> _shopMessage.value = "ITEM NOT AVAILABLE"
        }
    }

    fun equipCosmetic(itemId: String) {
        val before = _progress.value
        when (val result = ShopRules.equip(
            before.ownedCosmetics,
            itemId,
            before.equippedBoard,
            before.equippedEffect,
        )) {
            is EquipResult.Equipped -> {
                saveProgress(before.copy(equippedBoard = result.board, equippedEffect = result.effect))
                _shopMessage.value = "${ShopCatalog.find(itemId)?.title.orEmpty().uppercase()} EQUIPPED"
                cue(FeedbackCue.BUTTON)
            }
            EquipResult.NotOwned -> _shopMessage.value = "UNLOCK THIS ITEM FIRST"
            EquipResult.UnknownItem -> _shopMessage.value = "ITEM NOT AVAILABLE"
        }
    }

    fun buyPowerTreat(treat: PowerTreat) {
        val before = _progress.value
        val item = PowerTreatCatalog.find(treat)
        when (val result = ShopRules.purchasePowerTreat(before.sugarStars, before.powerTreats, treat)) {
            is PowerPurchaseResult.Purchased -> {
                saveProgress(before.copy(sugarStars = result.balance, powerTreats = result.inventory))
                _shopMessage.value = "${item.title.uppercase()} ADDED • USE IT IN GAME"
                cue(FeedbackCue.POP, HapticStrength.MEDIUM)
            }
            PowerPurchaseResult.NotEnoughStars -> {
                _shopMessage.value = "EARN ${(item.price - before.sugarStars).coerceAtLeast(0)} MORE SUGAR STARS"
            }
        }
    }

    fun usePowerTreat(treat: PowerTreat) {
        if (!acceptsGameplayInput()) return
        val inventory = ShopRules.consumePowerTreat(_progress.value.powerTreats, treat) ?: return
        val current = engineState ?: return
        val result = engine.usePowerTreat(current, treat)
        if (result.events.none { it is GameEvent.PowerTreatUsed }) return
        saveProgress(_progress.value.copy(powerTreats = inventory))
        handle(result)
        cue(
            if (treat == PowerTreat.PINK_BOMB) FeedbackCue.BOMB else FeedbackCue.COMBO,
            HapticStrength.STRONG,
        )
    }

    fun refreshDaily() {
        val normalized = MetaProgressRules.normalizeDay(_progress.value, LocalDayClock.dayIndex())
        if (normalized != _progress.value) saveProgress(normalized)
    }

    fun replayTutorial() {
        saveProgress(_progress.value.copy(tutorialSeen = false))
        startGame()
    }

    fun dismissComboBanner(token: Long) {
        if (_comboBanner.value?.token == token) _comboBanner.value = null
    }

    fun handleBack() {
        when (_screen.value) {
            AppScreen.MENU -> Unit
            AppScreen.SETTINGS,
            AppScreen.ACHIEVEMENTS,
            AppScreen.DAILY,
            AppScreen.LEADERBOARD,
            AppScreen.SHOP -> closeSettings()
            AppScreen.BLITZ -> if (_blitz.value.phase == BlitzPhase.PLAYING) pauseBlitz() else closeSettings()
            AppScreen.GAMEPLAY -> when (_overlay.value) {
                GameOverlay.NONE -> pause()
                GameOverlay.PAUSE -> resume()
                GameOverlay.TUTORIAL -> dismissTutorial()
                GameOverlay.GAME_OVER -> goHome()
            }
        }
    }

    fun onAppBackground() {
        if (_screen.value == AppScreen.BLITZ) {
            _blitz.value = blitzEngine.pause(_blitz.value)
            return
        }
        if (_screen.value != AppScreen.GAMEPLAY) return
        val current = engineState ?: return
        if (current.phase !in setOf(GamePhase.GAME_OVER, GamePhase.PAUSED)) {
            handle(engine.pause(current))
            if (_overlay.value == GameOverlay.NONE) _overlay.value = GameOverlay.PAUSE
        }
    }

    private fun handle(result: EngineResult) {
        val previousPublished = _game.value
        engineState = result.state
        if (previousPublished == null || !previousPublished.sameVisibleState(result.state)) {
            _game.value = result.state
        }

        result.events.forEach { event ->
            when (event) {
                is GameEvent.PieceLocked -> {
                    if (!event.special) sessionPiecesPlaced++
                    cue(FeedbackCue.LAND, HapticStrength.LIGHT)
                }
                is GameEvent.MatchesCleared -> {
                    sessionCandiesCleared += event.positions.size
                    comboToken++
                    _comboBanner.value = ComboBanner(event.combo, event.points, comboToken)
                    if (event.combo >= 2) cue(FeedbackCue.COMBO, HapticStrength.MEDIUM)
                    else cue(FeedbackCue.POP, HapticStrength.MEDIUM)
                }
                is GameEvent.BombExploded -> {
                    sessionBombsExploded++
                    sessionCandiesCleared += event.removed.size
                    cue(FeedbackCue.BOMB, HapticStrength.STRONG)
                }
                is GameEvent.PowerTreatUsed -> sessionCandiesCleared += event.removed.size
                GameEvent.GameOver -> commitGameOver(result.state)
                else -> Unit
            }
        }

        if (result.state.phase == GamePhase.GAME_OVER) {
            _overlay.value = GameOverlay.GAME_OVER
            commitGameOver(result.state)
        }
    }

    private fun commitGameOver(state: GameState) {
        if (resultCommitted) return
        resultCommitted = true
        val before = _progress.value
        val dayIndex = LocalDayClock.dayIndex()
        val beforeAchievements = MetaProgressRules.achievements(before)
            .filter { it.unlocked }
            .map { it.id }
            .toSet()
        val beforeDailyComplete = MetaProgressRules.dailyChallenges(before.daily).all { it.complete }
        val updated = MetaProgressRules.applyCompletedGame(
            before,
            CompletedGame(
                score = state.score,
                level = state.level,
                maxCombo = state.maxCombo,
                candiesCleared = sessionCandiesCleared,
                piecesPlaced = sessionPiecesPlaced,
                bombsExploded = sessionBombsExploded,
                playedAtMillis = System.currentTimeMillis(),
            ),
            dayIndex,
        )
        val newAchievements = MetaProgressRules.achievements(updated)
            .filter { it.unlocked && it.id !in beforeAchievements }
            .map { it.id }
        val dailyCompleted = !beforeDailyComplete &&
            MetaProgressRules.dailyChallenges(updated.daily).all { it.complete }
        val starsEarned = ShopRules.rewardForRun(state.score, newAchievements.size, dailyCompleted)
        _runRewards.value = RunRewards(newAchievements, dailyCompleted, starsEarned)
        saveProgress(updated.copy(sugarStars = updated.sugarStars + starsEarned))
        cue(FeedbackCue.GAME_OVER, HapticStrength.STRONG)
    }

    private fun openMetaScreen(screen: AppScreen) {
        _screen.value = screen
        cue(FeedbackCue.BUTTON)
    }

    private fun commitBlitz(state: GummyBlitzState) {
        if (blitzResultCommitted) return
        blitzResultCommitted = true
        val before = _progress.value
        val day = LocalDayClock.dayIndex()
        val eligible = before.lastBlitzRewardDay != day
        val reward = if (eligible) GummyBlitzRewards.forScore(state.score) else BlitzReward(0, null)
        var inventory = ShopRules.normalizedInventory(before.powerTreats)
        reward.powerTreat?.let { treat ->
            inventory = inventory + (treat to ((inventory[treat] ?: 0) + 1).coerceAtMost(99))
        }
        saveProgress(
            before.copy(
                bestBlitzScore = maxOf(before.bestBlitzScore, state.score),
                sugarStars = before.sugarStars + reward.stars,
                powerTreats = inventory,
                lastBlitzRewardDay = if (eligible) day else before.lastBlitzRewardDay,
            ),
        )
        _blitzReward.value = reward
        cue(if (state.score >= 140) FeedbackCue.COMBO else FeedbackCue.GAME_OVER, HapticStrength.STRONG)
    }

    private fun saveProgress(value: PlayerProgress) {
        _progress.value = value
        viewModelScope.launch { store.save(value) }
    }

    private fun cue(cue: FeedbackCue, haptic: HapticStrength = HapticStrength.NONE) {
        _feedback.tryEmit(FeedbackEvent(cue, haptic))
    }

    private fun acceptsGameplayInput(): Boolean =
        _screen.value == AppScreen.GAMEPLAY && _overlay.value == GameOverlay.NONE

    private fun GameState.sameVisibleState(other: GameState): Boolean =
        copy(fallAccumulatorMs = 0L, lockAccumulatorMs = 0L) ==
            other.copy(fallAccumulatorMs = 0L, lockAccumulatorMs = 0L)
}
