package studio.cortex.gummypuzzledrop.game.engine

import studio.cortex.gummypuzzledrop.game.model.Board
import studio.cortex.gummypuzzledrop.game.model.CellMovement
import studio.cortex.gummypuzzledrop.game.model.GameConfig
import studio.cortex.gummypuzzledrop.game.model.GridPosition

data class GravityResult(val board: Board, val movements: List<CellMovement>)

object GravityResolver {
    fun apply(board: Board): GravityResult {
        val settled = mutableMapOf<GridPosition, studio.cortex.gummypuzzledrop.game.model.CandyType>()
        val movements = mutableListOf<CellMovement>()
        for (x in 0 until GameConfig.BOARD_COLUMNS) {
            val column = board.cells.entries
                .filter { it.key.x == x }
                .sortedByDescending { it.key.y }
            var targetY = GameConfig.BOARD_ROWS - 1
            for ((from, candy) in column) {
                val to = GridPosition(x, targetY--)
                settled[to] = candy
                if (from != to) movements += CellMovement(from, to, candy)
            }
        }
        return GravityResult(Board(settled), movements)
    }
}
