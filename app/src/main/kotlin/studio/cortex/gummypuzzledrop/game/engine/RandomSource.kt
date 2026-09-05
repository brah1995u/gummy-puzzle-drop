package studio.cortex.gummypuzzledrop.game.engine

import kotlin.random.Random

fun interface RandomSource {
    fun nextInt(bound: Int): Int
}

class SeededRandomSource(seed: Int) : RandomSource {
    private val random = Random(seed)
    override fun nextInt(bound: Int): Int = random.nextInt(bound)
}

object DefaultRandomSource : RandomSource {
    override fun nextInt(bound: Int): Int = Random.Default.nextInt(bound)
}
