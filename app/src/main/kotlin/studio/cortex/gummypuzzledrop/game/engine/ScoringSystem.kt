package studio.cortex.gummypuzzledrop.game.engine

object ScoringSystem {
    fun baseScore(removed: Int): Int = when (removed) {
        in Int.MIN_VALUE..2 -> 0
        3 -> 30
        4 -> 60
        5 -> 100
        6 -> 150
        7 -> 200
        else -> 200 + (removed - 7) * 50
    }

    fun matchScore(removed: Int, combo: Int): Int = baseScore(removed) * combo.coerceAtLeast(1)

    fun hardDropBonus(skippedRows: Int): Int = skippedRows.coerceAtLeast(0)
}
