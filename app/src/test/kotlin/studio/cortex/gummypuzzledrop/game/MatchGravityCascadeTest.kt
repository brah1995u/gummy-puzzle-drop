package studio.cortex.gummypuzzledrop.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import studio.cortex.gummypuzzledrop.game.engine.BoardResolutionSystem
import studio.cortex.gummypuzzledrop.game.engine.GravityResolver
import studio.cortex.gummypuzzledrop.game.engine.MatchResolver
import studio.cortex.gummypuzzledrop.game.model.Board
import studio.cortex.gummypuzzledrop.game.model.CandyType
import studio.cortex.gummypuzzledrop.game.model.GridPosition

class MatchGravityCascadeTest {
    private val green = CandyType.GREEN_BEAR
    private val purple = CandyType.PURPLE_BEAR
    private val red = CandyType.RED_BEAR

    @Test
    fun detectsHorizontalThreeAndFourPlus() {
        val three = boardOf((1..3).map { p(it, 8) to green })
        assertEquals(setOf(p(1, 8), p(2, 8), p(3, 8)), MatchResolver.findMatches(three))

        val five = boardOf((2..6).map { p(it, 10) to purple })
        assertEquals(5, MatchResolver.findMatches(five).size)
    }

    @Test
    fun detectsVerticalThreeAndFourPlus() {
        val three = boardOf((9..11).map { p(4, it) to red })
        assertEquals(3, MatchResolver.findMatches(three).size)
        val four = boardOf((8..11).map { p(6, it) to green })
        assertEquals(4, MatchResolver.findMatches(four).size)
    }

    @Test
    fun crossingMatchUnionsCenterAndIndependentMatchesCombine() {
        val cross = Board.from(
            p(2, 8) to green,
            p(3, 8) to green,
            p(4, 8) to green,
            p(3, 7) to green,
            p(3, 9) to green,
        )
        assertEquals(5, MatchResolver.findMatches(cross).size)

        val independent = Board(
            buildMap {
                for (x in 0..2) put(p(x, 12), green)
                for (x in 5..7) put(p(x, 14), red)
            }
        )
        assertEquals(6, MatchResolver.findMatches(independent).size)
    }

    @Test
    fun ignoresDiagonalsPairsAndBombs() {
        val diagonal = Board.from(
            p(1, 9) to green,
            p(2, 10) to green,
            p(3, 11) to green,
            p(5, 15) to red,
            p(6, 15) to red,
            p(0, 14) to CandyType.PINK_BOMB,
            p(0, 15) to CandyType.PINK_BOMB,
            p(0, 13) to CandyType.PINK_BOMB,
        )
        assertTrue(MatchResolver.findMatches(diagonal).isEmpty())
    }

    @Test
    fun gravityFillsSingleAndMultipleHolesPerColumn() {
        val board = Board.from(
            p(0, 10) to green,
            p(0, 13) to purple,
            p(0, 15) to red,
            p(3, 5) to green,
        )
        val result = GravityResolver.apply(board)
        assertEquals(red, result.board[p(0, 15)])
        assertEquals(purple, result.board[p(0, 14)])
        assertEquals(green, result.board[p(0, 13)])
        assertEquals(green, result.board[p(3, 15)])
        assertEquals(4, result.board.cells.size)
    }

    @Test
    fun gravityPreservesOrderAndLeavesEmptyColumnsEmpty() {
        val board = Board.from(p(2, 7) to green, p(2, 11) to red)
        val result = GravityResolver.apply(board).board
        assertEquals(red, result[p(2, 15)])
        assertEquals(green, result[p(2, 14)])
        assertFalse(result.cells.keys.any { it.x == 5 })
    }

    @Test
    fun resolvesTwoStepCascade() {
        val board = Board.from(
            p(0, 15) to green,
            p(1, 15) to green,
            p(2, 15) to green,
            p(0, 14) to red,
            p(1, 13) to red,
            p(2, 12) to red,
        )
        val resolution = BoardResolutionSystem.resolve(board)
        assertEquals(listOf(1, 2), resolution.steps.map { it.combo })
        assertTrue(resolution.board.cells.isEmpty())
    }

    @Test
    fun resolvesThreeStepCascadeAndAwardsBomb() {
        val cells = mutableMapOf<GridPosition, CandyType>()
        for (y in listOf(11, 13, 15)) {
            for (x in 0..2) cells[p(x, y)] = green
        }
        cells[p(0, 14)] = red
        cells[p(0, 12)] = red
        cells[p(0, 10)] = red
        cells[p(0, 9)] = purple
        cells[p(1, 10)] = red
        cells[p(1, 8)] = purple
        cells[p(2, 8)] = red
        cells[p(2, 6)] = purple

        val resolution = BoardResolutionSystem.resolve(Board(cells))
        assertEquals(listOf(1, 2, 3), resolution.steps.map { it.combo })
        assertTrue(resolution.bombEarned)
        assertTrue(resolution.board.cells.isEmpty())
    }

    private fun boardOf(values: List<Pair<GridPosition, CandyType>>) = Board(values.toMap())
    private fun p(x: Int, y: Int) = GridPosition(x, y)
}
