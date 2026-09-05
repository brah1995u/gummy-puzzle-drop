package studio.cortex.gummypuzzledrop.game.engine

import java.util.ArrayDeque
import studio.cortex.gummypuzzledrop.game.model.PieceShape

class PieceGenerator(private val random: RandomSource = DefaultRandomSource) {
    private val bag = ArrayDeque<PieceShape>()

    fun next(): PieceShape {
        if (bag.isEmpty()) refill()
        return bag.removeFirst()
    }

    fun reset() = bag.clear()

    private fun refill() {
        val shapes = PieceShape.entries.toMutableList()
        for (index in shapes.lastIndex downTo 1) {
            val swapWith = random.nextInt(index + 1)
            val value = shapes[index]
            shapes[index] = shapes[swapWith]
            shapes[swapWith] = value
        }
        bag.addAll(shapes)
    }
}
