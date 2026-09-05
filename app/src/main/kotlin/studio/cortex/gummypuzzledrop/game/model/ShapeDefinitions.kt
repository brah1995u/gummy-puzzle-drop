package studio.cortex.gummypuzzledrop.game.model

object ShapeDefinitions {
    private val definitions: Map<PieceShape, Map<Rotation, List<GridPosition>>> = mapOf(
        PieceShape.I to rotations(
            listOf(p(0, 1), p(1, 1), p(2, 1), p(3, 1)),
            listOf(p(2, 0), p(2, 1), p(2, 2), p(2, 3)),
            listOf(p(3, 2), p(2, 2), p(1, 2), p(0, 2)),
            listOf(p(1, 3), p(1, 2), p(1, 1), p(1, 0)),
        ),
        PieceShape.O to rotations(
            listOf(p(1, 0), p(2, 0), p(1, 1), p(2, 1)),
            listOf(p(2, 0), p(2, 1), p(1, 0), p(1, 1)),
            listOf(p(2, 1), p(1, 1), p(2, 0), p(1, 0)),
            listOf(p(1, 1), p(1, 0), p(2, 1), p(2, 0)),
        ),
        PieceShape.T to rotations(
            listOf(p(1, 0), p(0, 1), p(1, 1), p(2, 1)),
            listOf(p(2, 1), p(1, 0), p(1, 1), p(1, 2)),
            listOf(p(1, 2), p(2, 1), p(1, 1), p(0, 1)),
            listOf(p(0, 1), p(1, 2), p(1, 1), p(1, 0)),
        ),
        PieceShape.L to rotations(
            listOf(p(2, 0), p(0, 1), p(1, 1), p(2, 1)),
            listOf(p(2, 2), p(1, 0), p(1, 1), p(1, 2)),
            listOf(p(0, 2), p(2, 1), p(1, 1), p(0, 1)),
            listOf(p(0, 0), p(1, 2), p(1, 1), p(1, 0)),
        ),
        PieceShape.J to rotations(
            listOf(p(0, 0), p(0, 1), p(1, 1), p(2, 1)),
            listOf(p(2, 0), p(1, 0), p(1, 1), p(1, 2)),
            listOf(p(2, 2), p(2, 1), p(1, 1), p(0, 1)),
            listOf(p(0, 2), p(1, 2), p(1, 1), p(1, 0)),
        ),
        PieceShape.S to rotations(
            listOf(p(1, 0), p(2, 0), p(0, 1), p(1, 1)),
            listOf(p(2, 1), p(2, 2), p(1, 0), p(1, 1)),
            listOf(p(1, 2), p(0, 2), p(2, 1), p(1, 1)),
            listOf(p(0, 1), p(0, 0), p(1, 2), p(1, 1)),
        ),
        PieceShape.Z to rotations(
            listOf(p(0, 0), p(1, 0), p(1, 1), p(2, 1)),
            listOf(p(2, 0), p(2, 1), p(1, 1), p(1, 2)),
            listOf(p(2, 2), p(1, 2), p(1, 1), p(0, 1)),
            listOf(p(0, 2), p(0, 1), p(1, 1), p(1, 0)),
        ),
    )

    fun offsets(shape: PieceShape, rotation: Rotation): List<GridPosition> =
        definitions.getValue(shape).getValue(rotation)

    fun spawnOrigin(spec: PieceSpec): GridPosition = when (spec) {
        BombPieceSpec -> GridPosition((GameConfig.BOARD_COLUMNS - 1) / 2, 0)
        is NormalPieceSpec -> {
            val cells = offsets(spec.shape, Rotation.R0)
            val minX = cells.minOf(GridPosition::x)
            val maxX = cells.maxOf(GridPosition::x)
            val minY = cells.minOf(GridPosition::y)
            val width = maxX - minX + 1
            GridPosition((GameConfig.BOARD_COLUMNS - width) / 2 - minX, -minY)
        }
    }

    fun bounds(shape: PieceShape, rotation: Rotation): Pair<GridPosition, GridPosition> {
        val cells = offsets(shape, rotation)
        return GridPosition(cells.minOf { it.x }, cells.minOf { it.y }) to
            GridPosition(cells.maxOf { it.x }, cells.maxOf { it.y })
    }

    private fun rotations(
        r0: List<GridPosition>,
        r90: List<GridPosition>,
        r180: List<GridPosition>,
        r270: List<GridPosition>,
    ): Map<Rotation, List<GridPosition>> = mapOf(
        Rotation.R0 to r0,
        Rotation.R90 to r90,
        Rotation.R180 to r180,
        Rotation.R270 to r270,
    )

    private fun p(x: Int, y: Int) = GridPosition(x, y)
}
