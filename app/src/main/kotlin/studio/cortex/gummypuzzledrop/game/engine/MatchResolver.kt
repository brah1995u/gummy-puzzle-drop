package studio.cortex.gummypuzzledrop.game.engine

import studio.cortex.gummypuzzledrop.game.model.Board
import studio.cortex.gummypuzzledrop.game.model.CandyType
import studio.cortex.gummypuzzledrop.game.model.GameConfig
import studio.cortex.gummypuzzledrop.game.model.GridPosition

object MatchResolver {
    fun findMatches(board: Board): Set<GridPosition> = buildSet {
        for (y in 0 until GameConfig.BOARD_ROWS) {
            scanLine((0 until GameConfig.BOARD_COLUMNS).map { x -> GridPosition(x, y) }, board, this)
        }
        for (x in 0 until GameConfig.BOARD_COLUMNS) {
            scanLine((0 until GameConfig.BOARD_ROWS).map { y -> GridPosition(x, y) }, board, this)
        }
    }

    private fun scanLine(
        positions: List<GridPosition>,
        board: Board,
        matches: MutableSet<GridPosition>,
    ) {
        var runStart = 0
        while (runStart < positions.size) {
            val candy = board[positions[runStart]]
            if (candy == null || !candy.participatesInMatches) {
                runStart++
                continue
            }
            var runEnd = runStart + 1
            while (runEnd < positions.size && board[positions[runEnd]] == candy) runEnd++
            if (runEnd - runStart >= 3) {
                for (index in runStart until runEnd) matches += positions[index]
            }
            runStart = runEnd
        }
    }
}
