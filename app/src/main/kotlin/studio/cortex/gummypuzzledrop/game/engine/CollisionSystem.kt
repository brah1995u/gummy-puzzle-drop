package studio.cortex.gummypuzzledrop.game.engine

import studio.cortex.gummypuzzledrop.game.model.ActivePiece
import studio.cortex.gummypuzzledrop.game.model.Board

object CollisionSystem {
    fun isValid(piece: ActivePiece, board: Board): Boolean {
        val positions = piece.cells().map { it.position }
        return positions.distinct().size == positions.size &&
            positions.all { Board.isInside(it) && board.isEmpty(it) }
    }

    fun canMove(piece: ActivePiece, board: Board, dx: Int = 0, dy: Int = 0): Boolean =
        isValid(piece.moved(dx, dy), board)

    fun dropDistance(piece: ActivePiece, board: Board): Int {
        var distance = 0
        while (canMove(piece, board, dy = distance + 1)) distance++
        return distance
    }

    fun landingPiece(piece: ActivePiece, board: Board): ActivePiece = piece.moved(dy = dropDistance(piece, board))
}
