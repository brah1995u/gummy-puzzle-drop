package studio.cortex.gummypuzzledrop.game.model

object GameConfig {
    const val BOARD_COLUMNS = 8
    const val VISIBLE_ROWS = 14
    const val SPAWN_ROWS = 2
    const val BOARD_ROWS = VISIBLE_ROWS + SPAWN_ROWS

    const val LOCK_DELAY_MS = 400L
    const val PIECES_PER_LEVEL = 10
    const val STAR_UNLOCK_LEVEL = 4
    const val HEART_UNLOCK_LEVEL = 7
    const val BOMB_REWARD_COMBO = 3
    const val BOMB_RADIUS = 1
    const val DANGER_VISIBLE_ROWS = 3

    val WALL_KICKS = listOf(0, -1, 1, -2, 2)

    private val FALL_INTERVALS_MS = listOf(
        900L,
        780L,
        680L,
        590L,
        510L,
        440L,
        380L,
        330L,
        290L,
        250L,
    )

    fun fallIntervalMs(level: Int): Long = FALL_INTERVALS_MS[(level - 1).coerceIn(0, FALL_INTERVALS_MS.lastIndex)]

    fun levelForPlacedPieces(placedNormalPieces: Int): Int = placedNormalPieces / PIECES_PER_LEVEL + 1
}
