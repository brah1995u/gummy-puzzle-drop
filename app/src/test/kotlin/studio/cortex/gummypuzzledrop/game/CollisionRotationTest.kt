package studio.cortex.gummypuzzledrop.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import studio.cortex.gummypuzzledrop.game.engine.CollisionSystem
import studio.cortex.gummypuzzledrop.game.engine.RotationSystem
import studio.cortex.gummypuzzledrop.game.model.ActivePiece
import studio.cortex.gummypuzzledrop.game.model.Board
import studio.cortex.gummypuzzledrop.game.model.CandyType
import studio.cortex.gummypuzzledrop.game.model.GridPosition
import studio.cortex.gummypuzzledrop.game.model.NormalPieceSpec
import studio.cortex.gummypuzzledrop.game.model.PieceShape
import studio.cortex.gummypuzzledrop.game.model.Rotation

class CollisionRotationTest {
    private val candies = listOf(
        CandyType.GREEN_BEAR,
        CandyType.PURPLE_BEAR,
        CandyType.RED_BEAR,
        CandyType.GREEN_BEAR,
    )

    @Test
    fun collisionsCoverLeftRightFloorAndOccupiedCells() {
        val t = NormalPieceSpec(PieceShape.T, candies)
        assertFalse(CollisionSystem.isValid(ActivePiece(t, GridPosition(-1, 4)), Board()))
        assertFalse(CollisionSystem.isValid(ActivePiece(t, GridPosition(6, 4)), Board()))

        val floorPiece = ActivePiece(t, GridPosition(2, 14))
        assertTrue(CollisionSystem.isValid(floorPiece, Board()))
        assertFalse(CollisionSystem.canMove(floorPiece, Board(), dy = 1))

        val occupied = Board.from(GridPosition(3, 5) to CandyType.RED_BEAR)
        assertFalse(CollisionSystem.isValid(ActivePiece(t, GridPosition(2, 5)), occupied))
    }

    @Test
    fun rotatesClockwiseInOpenSpace() {
        val piece = ActivePiece(NormalPieceSpec(PieceShape.T, candies), GridPosition(2, 4))
        val rotated = RotationSystem.rotateClockwise(piece, Board())
        assertEquals(Rotation.R90, rotated?.piece?.rotation)
        assertEquals(0, rotated?.kick)
    }

    @Test
    fun iPieceKicksAwayFromLeftAndRightWalls() {
        val spec = NormalPieceSpec(PieceShape.I, candies)
        val atLeft = ActivePiece(spec, GridPosition(-2, 4), Rotation.R90)
        val leftRotation = RotationSystem.rotateClockwise(atLeft, Board())
        assertEquals(2, leftRotation?.kick)
        assertTrue(CollisionSystem.isValid(leftRotation!!.piece, Board()))

        val atRight = ActivePiece(spec, GridPosition(5, 4), Rotation.R90)
        val rightRotation = RotationSystem.rotateClockwise(atRight, Board())
        assertEquals(-1, rightRotation?.kick)
        assertTrue(CollisionSystem.isValid(rightRotation!!.piece, Board()))
    }

    @Test
    fun blockedRotationIsRejected() {
        val piece = ActivePiece(NormalPieceSpec(PieceShape.T, candies), GridPosition(2, 5))
        val blockers = Board.from(
            GridPosition(3, 7) to CandyType.RED_BEAR,
            GridPosition(2, 7) to CandyType.RED_BEAR,
            GridPosition(4, 7) to CandyType.RED_BEAR,
            GridPosition(1, 7) to CandyType.RED_BEAR,
            GridPosition(5, 7) to CandyType.RED_BEAR,
        )
        assertNull(RotationSystem.rotateClockwise(piece, blockers))
    }

    @Test
    fun oRotationKeepsFootprintButRotatesCandyPositions() {
        val uniqueEnough = listOf(
            CandyType.GREEN_BEAR,
            CandyType.PURPLE_BEAR,
            CandyType.RED_BEAR,
            CandyType.GREEN_STAR,
        )
        val piece = ActivePiece(NormalPieceSpec(PieceShape.O, uniqueEnough), GridPosition(2, 4))
        val rotated = RotationSystem.rotateClockwise(piece, Board())!!.piece
        assertEquals(piece.cells().map { it.position }.toSet(), rotated.cells().map { it.position }.toSet())
        assertTrue(piece.cells().associate { it.position to it.candy } != rotated.cells().associate { it.position to it.candy })
    }
}
