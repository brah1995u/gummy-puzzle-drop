package studio.cortex.gummypuzzledrop.game.engine

import studio.cortex.gummypuzzledrop.game.model.ActivePiece
import studio.cortex.gummypuzzledrop.game.model.Board
import studio.cortex.gummypuzzledrop.game.model.GameConfig

data class RotationResult(val piece: ActivePiece, val kick: Int)

object RotationSystem {
    fun rotateClockwise(piece: ActivePiece, board: Board): RotationResult? {
        if (piece.isBomb) return null
        val rotated = piece.copy(rotation = piece.rotation.clockwise())
        return GameConfig.WALL_KICKS.firstNotNullOfOrNull { kick ->
            val candidate = rotated.moved(dx = kick)
            if (CollisionSystem.isValid(candidate, board)) RotationResult(candidate, kick) else null
        }
    }
}
