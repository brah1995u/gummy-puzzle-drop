package studio.cortex.gummypuzzledrop.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import studio.cortex.gummypuzzledrop.game.engine.SeededRandomSource
import studio.cortex.gummypuzzledrop.game.minigame.BlitzPhase
import studio.cortex.gummypuzzledrop.game.minigame.GummyBlitzEngine
import studio.cortex.gummypuzzledrop.game.minigame.GummyBlitzRewards
import studio.cortex.gummypuzzledrop.game.model.PowerTreat

class GummyBlitzEngineTest {
    @Test fun generatedRoundAlwaysContainsTarget() {
        val state = GummyBlitzEngine(SeededRandomSource(201)).ready()
        assertEquals(9, state.cells.size)
        assertTrue(state.target in state.cells)
    }

    @Test fun correctTapScoresBuildsComboAndKeepsTargetAvailable() {
        val engine = GummyBlitzEngine(SeededRandomSource(202))
        val start = engine.start()
        val result = engine.tap(start, start.cells.indexOf(start.target))
        assertTrue(result.correct)
        assertEquals(12, result.points)
        assertEquals(1, result.state.combo)
        assertTrue(result.state.target in result.state.cells)
    }

    @Test fun threeMissesEndRoundAndResetCombo() {
        val engine = GummyBlitzEngine(SeededRandomSource(203))
        var state = engine.start()
        repeat(3) {
            val wrong = state.cells.indexOfFirst { it != state.target }
            state = engine.tap(state, wrong).state
        }
        assertEquals(0, state.lives)
        assertEquals(BlitzPhase.FINISHED, state.phase)
        assertEquals(0, state.combo)
    }

    @Test fun timerEndsAtZeroWithoutGoingNegative() {
        val engine = GummyBlitzEngine(SeededRandomSource(204))
        val result = engine.advance(engine.start(), 99_000)
        assertEquals(0L, result.remainingMs)
        assertEquals(BlitzPhase.FINISHED, result.phase)
    }

    @Test fun rewardTiersGrantUsefulPowerTreats() {
        assertEquals(null, GummyBlitzRewards.forScore(100).powerTreat)
        assertEquals(PowerTreat.PINK_BOMB, GummyBlitzRewards.forScore(140).powerTreat)
        assertEquals(PowerTreat.RAINBOW_POP, GummyBlitzRewards.forScore(260).powerTreat)
        assertEquals(PowerTreat.SWEET_CLEANUP, GummyBlitzRewards.forScore(420).powerTreat)
        assertEquals(18, GummyBlitzRewards.forScore(9_999).stars)
    }
}
