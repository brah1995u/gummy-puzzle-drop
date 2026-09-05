package studio.cortex.gummypuzzledrop.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import studio.cortex.gummypuzzledrop.game.engine.BombSystem
import studio.cortex.gummypuzzledrop.game.engine.GameEngine
import studio.cortex.gummypuzzledrop.game.engine.BoardResolutionSystem
import studio.cortex.gummypuzzledrop.game.engine.GravityResolver
import studio.cortex.gummypuzzledrop.game.engine.ScoringSystem
import studio.cortex.gummypuzzledrop.game.engine.SeededRandomSource
import studio.cortex.gummypuzzledrop.game.model.ActivePiece
import studio.cortex.gummypuzzledrop.game.model.Board
import studio.cortex.gummypuzzledrop.game.model.BombPieceSpec
import studio.cortex.gummypuzzledrop.game.model.CandyType
import studio.cortex.gummypuzzledrop.game.model.GameConfig
import studio.cortex.gummypuzzledrop.game.model.GameEvent
import studio.cortex.gummypuzzledrop.game.model.GamePhase
import studio.cortex.gummypuzzledrop.game.model.GridPosition
import studio.cortex.gummypuzzledrop.game.model.NormalPieceSpec
import studio.cortex.gummypuzzledrop.game.model.Rotation
import studio.cortex.gummypuzzledrop.game.model.PieceShape

class ScoringBombEngineTest {
    @Test
    fun scoringUsesApprovedTableExtraCellsComboAndDropBonus() {
        assertEquals(30, ScoringSystem.baseScore(3))
        assertEquals(60, ScoringSystem.baseScore(4))
        assertEquals(100, ScoringSystem.baseScore(5))
        assertEquals(150, ScoringSystem.baseScore(6))
        assertEquals(200, ScoringSystem.baseScore(7))
        assertEquals(250, ScoringSystem.baseScore(8))
        assertEquals(300, ScoringSystem.matchScore(5, 3))
        assertEquals(12, ScoringSystem.hardDropBonus(12))
    }

    @Test
    fun bombAreaClipsAtCenterEdgeAndCorner() {
        assertEquals(9, BombSystem.affectedPositions(p(3, 8)).size)
        assertEquals(6, BombSystem.affectedPositions(p(0, 8)).size)
        assertEquals(4, BombSystem.affectedPositions(p(0, 0)).size)
    }

    @Test
    fun bombRemovalIsFollowedByGravity() {
        val board = Board.from(
            p(0, 15) to CandyType.GREEN_BEAR,
            p(1, 15) to CandyType.PURPLE_BEAR,
            p(2, 15) to CandyType.RED_BEAR,
            p(1, 10) to CandyType.GREEN_STAR,
        )
        val removed = BombSystem.occupiedAffectedPositions(board, p(1, 15))
        val gravity = GravityResolver.apply(board.without(removed)).board
        assertEquals(CandyType.GREEN_STAR, gravity[p(1, 15)])
        assertEquals(1, gravity.cells.size)
    }

    @Test
    fun bombRemovalCanCreateAndResolveACascade() {
        val board = Board.from(
            p(0, 12) to CandyType.RED_BEAR,
            p(1, 11) to CandyType.RED_BEAR,
            p(2, 10) to CandyType.RED_BEAR,
            p(0, 15) to CandyType.GREEN_BEAR,
            p(1, 14) to CandyType.PURPLE_BEAR,
            p(2, 13) to CandyType.GREEN_BEAR,
            p(2, 15) to CandyType.PURPLE_BEAR,
        )
        val exploded = board.without(BombSystem.occupiedAffectedPositions(board, p(1, 14)))
        val afterGravity = GravityResolver.apply(exploded).board
        val cascade = BoardResolutionSystem.resolve(afterGravity, allowBombReward = false)
        assertEquals(1, cascade.steps.size)
        assertEquals(3, cascade.steps.single().cleared.size)
        assertTrue(cascade.board.cells.isEmpty())
        assertFalse(cascade.bombEarned)
    }

    @Test
    fun comboThreeSpawnsEarnedPinkBombBeforeNextNormalPiece() {
        val engine = GameEngine(SeededRandomSource(37))
        val initial = engine.newGame().state
        val cells = mutableMapOf<GridPosition, CandyType>()
        for (y in listOf(11, 13, 15)) {
            for (x in 0..2) cells[p(x, y)] = CandyType.GREEN_BEAR
        }
        cells[p(0, 14)] = CandyType.RED_BEAR
        cells[p(0, 12)] = CandyType.RED_BEAR
        cells[p(0, 10)] = CandyType.RED_BEAR
        cells[p(0, 9)] = CandyType.PURPLE_BEAR
        cells[p(1, 10)] = CandyType.RED_BEAR
        cells[p(1, 8)] = CandyType.PURPLE_BEAR
        cells[p(2, 8)] = CandyType.RED_BEAR
        cells[p(2, 6)] = CandyType.PURPLE_BEAR
        val lockingSpec = NormalPieceSpec(
            PieceShape.O,
            listOf(
                CandyType.GREEN_BEAR,
                CandyType.PURPLE_BEAR,
                CandyType.RED_BEAR,
                CandyType.GREEN_BEAR,
            ),
        )
        val prepared = initial.copy(
            board = Board(cells),
            active = ActivePiece(lockingSpec, p(3, 14)),
            phase = GamePhase.FALLING,
        )
        val result = engine.hardDrop(prepared)
        assertTrue(result.events.contains(GameEvent.BombEarned))
        assertTrue(result.state.active?.isBomb == true)
        assertFalse(result.state.holdAvailable)
    }

    @Test
    fun hardDropLocksImmediatelyAndScoresSkippedRows() {
        val engine = GameEngine(SeededRandomSource(5))
        val start = engine.newGame().state
        val rows = engine.ghost(start)!!.origin.y - start.active!!.origin.y
        val result = engine.hardDrop(start)
        assertEquals(rows, result.events.filterIsInstance<GameEvent.HardDropped>().single().bonus)
        assertTrue(result.events.any { it is GameEvent.PieceLocked })
        assertTrue(result.state.normalPiecesPlaced == 1)
    }

    @Test
    fun naturalLandingUsesLockDelayInsteadOfImmediateLock() {
        val engine = GameEngine(SeededRandomSource(7))
        var state = engine.newGame().state
        val landing = engine.ghost(state)!!
        state = state.copy(active = landing, phase = GamePhase.FALLING, fallAccumulatorMs = 0)
        val touching = engine.advance(state, GameConfig.fallIntervalMs(1)).state
        assertEquals(GamePhase.LOCK_DELAY, touching.phase)
        assertEquals(0, touching.normalPiecesPlaced)
        val almost = engine.advance(touching, GameConfig.LOCK_DELAY_MS - 1).state
        assertEquals(0, almost.normalPiecesPlaced)
        val locked = engine.advance(almost, 1).state
        assertEquals(1, locked.normalPiecesPlaced)
    }

    @Test
    fun firstHoldConsumesNextAndSecondHoldIsBlocked() {
        val engine = GameEngine(SeededRandomSource(11))
        val start = engine.newGame().state
        val original = start.active!!.spec as NormalPieceSpec
        val expectedIncoming = start.next
        val held = engine.hold(start).state
        assertEquals(original, held.hold)
        assertEquals(expectedIncoming, held.active!!.spec)
        assertEquals(Rotation.R0, held.active.rotation)
        assertFalse(held.holdAvailable)
        assertEquals(held, engine.hold(held).state)
    }

    @Test
    fun holdSwapPreservesExactShapeAndCandyComposition() {
        val engine = GameEngine(SeededRandomSource(19))
        val start = engine.newGame().state
        val saved = start.active!!.spec as NormalPieceSpec
        val afterFirstHold = engine.hold(start).state
        val afterLock = engine.hardDrop(afterFirstHold).state
        assertTrue(afterLock.holdAvailable)
        val currentBeforeSwap = afterLock.active!!.spec as NormalPieceSpec
        val swapped = engine.hold(afterLock).state
        assertEquals(saved, swapped.active!!.spec)
        assertEquals(currentBeforeSwap, swapped.hold)
        assertEquals(Rotation.R0, swapped.active.rotation)
    }

    @Test
    fun bombCannotBeHeld() {
        val engine = GameEngine(SeededRandomSource(23))
        val normal = engine.newGame().state
        val bomb = normal.copy(
            active = ActivePiece(BombPieceSpec, p(3, 0)),
            holdAvailable = true,
            phase = GamePhase.FALLING,
        )
        assertEquals(bomb, engine.hold(bomb).state)
    }

    @Test
    fun pauseStopsTimeAndResumeRestoresPhase() {
        val engine = GameEngine(SeededRandomSource(29))
        val start = engine.newGame().state
        val paused = engine.pause(start).state
        assertEquals(GamePhase.PAUSED, paused.phase)
        assertEquals(paused, engine.advance(paused, 10_000).state)
        val resumed = engine.resume(paused).state
        assertEquals(GamePhase.FALLING, resumed.phase)
    }

    @Test
    fun validSpawnStartsAndBlockedSpawnEndsGame() {
        val seed = 31
        val preview = GameEngine(SeededRandomSource(seed)).newGame().state
        assertFalse(preview.isGameOver)
        val blockers = Board(preview.active!!.cells().associate { it.position to CandyType.RED_BEAR })
        val blocked = GameEngine(SeededRandomSource(seed)).newGame(blockers)
        assertTrue(blocked.state.isGameOver)
        assertTrue(blocked.events.contains(GameEvent.GameOver))
    }

    @Test
    fun levelChangesEveryTenLockedNormalPieces() {
        assertEquals(1, GameConfig.levelForPlacedPieces(0))
        assertEquals(1, GameConfig.levelForPlacedPieces(9))
        assertEquals(2, GameConfig.levelForPlacedPieces(10))
        assertEquals(3, GameConfig.levelForPlacedPieces(20))
        assertNotEquals(GameConfig.fallIntervalMs(1), GameConfig.fallIntervalMs(2))
    }

    private fun p(x: Int, y: Int) = GridPosition(x, y)
}
