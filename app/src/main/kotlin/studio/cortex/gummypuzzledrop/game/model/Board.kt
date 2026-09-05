package studio.cortex.gummypuzzledrop.game.model

data class Board(val cells: Map<GridPosition, CandyType> = emptyMap()) {
    init {
        require(cells.keys.all(::isInside)) { "Board contains an out-of-bounds cell" }
    }

    operator fun get(position: GridPosition): CandyType? = cells[position]

    fun isEmpty(position: GridPosition): Boolean = position !in cells

    fun withCells(additions: List<PieceCell>): Board {
        require(additions.map(PieceCell::position).distinct().size == additions.size) { "Piece cells overlap" }
        require(additions.all { isInside(it.position) && isEmpty(it.position) }) { "Cannot place cells on the board" }
        return Board(cells + additions.associate { it.position to it.candy })
    }

    fun without(positions: Set<GridPosition>): Board = Board(cells - positions)

    fun candyAt(x: Int, y: Int): CandyType? = cells[GridPosition(x, y)]

    companion object {
        fun isInside(position: GridPosition): Boolean =
            position.x in 0 until GameConfig.BOARD_COLUMNS && position.y in 0 until GameConfig.BOARD_ROWS

        fun from(vararg cells: Pair<GridPosition, CandyType>): Board = Board(mapOf(*cells))
    }
}
