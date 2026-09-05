package studio.cortex.gummypuzzledrop.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import studio.cortex.gummypuzzledrop.game.engine.GameEngine
import studio.cortex.gummypuzzledrop.game.engine.SeededRandomSource
import studio.cortex.gummypuzzledrop.game.model.Board
import studio.cortex.gummypuzzledrop.game.model.CandyType
import studio.cortex.gummypuzzledrop.game.model.GameEvent
import studio.cortex.gummypuzzledrop.game.model.GridPosition
import studio.cortex.gummypuzzledrop.game.model.PowerTreat

class PowerTreatEngineTest {
    @Test fun pinkBombReplacesActivePieceWithoutChangingNext() {
        val engine = GameEngine(SeededRandomSource(101))
        val start = engine.newGame().state
        val result = engine.usePowerTreat(start, PowerTreat.PINK_BOMB)
        assertTrue(result.state.active?.isBomb == true)
        assertEquals(start.next, result.state.next)
        assertTrue(result.events.any { it is GameEvent.PowerTreatUsed })
    }

    @Test fun rainbowPopRemovesMostCommonNormalCandyAndAppliesGravity() {
        val engine = GameEngine(SeededRandomSource(102))
        val start = engine.newGame().state.copy(
            board = Board.from(
                p(0, 10) to CandyType.GREEN_BEAR,
                p(1, 12) to CandyType.GREEN_BEAR,
                p(2, 8) to CandyType.RED_BEAR,
            ),
        )
        val result = engine.usePowerTreat(start, PowerTreat.RAINBOW_POP)
        assertEquals(1, result.state.board.cells.size)
        assertEquals(CandyType.RED_BEAR, result.state.board[p(2, 15)])
        val event = result.events.filterIsInstance<GameEvent.PowerTreatUsed>().single()
        assertEquals(2, event.removed.size)
    }

    @Test fun cleanupOnlyClearsSpawnAndDangerRows() {
        val engine = GameEngine(SeededRandomSource(103))
        val start = engine.newGame().state.copy(
            board = Board.from(
                p(0, 1) to CandyType.GREEN_BEAR,
                p(1, 3) to CandyType.PURPLE_BEAR,
                p(2, 14) to CandyType.RED_BEAR,
            ),
        )
        val result = engine.usePowerTreat(start, PowerTreat.SWEET_CLEANUP)
        assertEquals(1, result.state.board.cells.size)
        assertEquals(CandyType.RED_BEAR, result.state.board[p(2, 15)])
        assertEquals(2, result.events.filterIsInstance<GameEvent.PowerTreatUsed>().single().removed.size)
    }

    @Test fun unavailableTreatEffectDoesNotEmitUseEvent() {
        val engine = GameEngine(SeededRandomSource(104))
        val start = engine.newGame().state
        val result = engine.usePowerTreat(start, PowerTreat.RAINBOW_POP)
        assertEquals(start, result.state)
        assertTrue(result.events.isEmpty())
    }

    private fun p(x: Int, y: Int) = GridPosition(x, y)
}
