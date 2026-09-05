package studio.cortex.gummypuzzledrop.game.engine

import studio.cortex.gummypuzzledrop.game.model.Board
import studio.cortex.gummypuzzledrop.game.model.GameConfig
import studio.cortex.gummypuzzledrop.game.model.GridPosition

object BombSystem {
    fun affectedPositions(center: GridPosition): Set<GridPosition> = buildSet {
        for (y in center.y - GameConfig.BOMB_RADIUS..center.y + GameConfig.BOMB_RADIUS) {
            for (x in center.x - GameConfig.BOMB_RADIUS..center.x + GameConfig.BOMB_RADIUS) {
                GridPosition(x, y).takeIf(Board::isInside)?.let(::add)
            }
        }
    }

    fun occupiedAffectedPositions(board: Board, center: GridPosition): Set<GridPosition> =
        affectedPositions(center).filterTo(mutableSetOf()) { it in board.cells }
}
