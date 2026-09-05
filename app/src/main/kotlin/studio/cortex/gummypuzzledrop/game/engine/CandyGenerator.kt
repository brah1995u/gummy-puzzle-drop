package studio.cortex.gummypuzzledrop.game.engine

import studio.cortex.gummypuzzledrop.game.model.CandyType
import studio.cortex.gummypuzzledrop.game.model.GameConfig

class CandyGenerator(private val random: RandomSource = DefaultRandomSource) {
    fun poolForLevel(level: Int): List<CandyType> = buildList {
        add(CandyType.GREEN_BEAR)
        add(CandyType.PURPLE_BEAR)
        add(CandyType.RED_BEAR)
        if (level >= GameConfig.STAR_UNLOCK_LEVEL) add(CandyType.GREEN_STAR)
        if (level >= GameConfig.HEART_UNLOCK_LEVEL) add(CandyType.ORANGE_HEART)
    }

    fun generate(level: Int): List<CandyType> {
        val pool = poolForLevel(level)
        val counts = mutableMapOf<CandyType, Int>()
        return List(4) {
            val eligible = pool.filter { counts.getOrDefault(it, 0) < 2 }
            val candy = eligible[random.nextInt(eligible.size)]
            counts[candy] = counts.getOrDefault(candy, 0) + 1
            candy
        }
    }
}
