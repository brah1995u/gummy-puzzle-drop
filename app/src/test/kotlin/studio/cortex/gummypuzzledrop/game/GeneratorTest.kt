package studio.cortex.gummypuzzledrop.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import studio.cortex.gummypuzzledrop.game.engine.CandyGenerator
import studio.cortex.gummypuzzledrop.game.engine.PieceGenerator
import studio.cortex.gummypuzzledrop.game.engine.SeededRandomSource
import studio.cortex.gummypuzzledrop.game.model.CandyType
import studio.cortex.gummypuzzledrop.game.model.PieceShape

class GeneratorTest {
    @Test
    fun sevenBagContainsEveryShapeExactlyOnceAndRegenerates() {
        val generator = PieceGenerator(SeededRandomSource(41))
        val draws = List(21) { generator.next() }
        draws.chunked(7).forEach { bag ->
            assertEquals(7, bag.size)
            assertEquals(PieceShape.entries.toSet(), bag.toSet())
            assertTrue(bag.groupingBy { it }.eachCount().values.all { it == 1 })
        }
    }

    @Test
    fun candyPoolUnlocksOnlyAtApprovedLevels() {
        val generator = CandyGenerator(SeededRandomSource(9))
        assertEquals(
            setOf(CandyType.GREEN_BEAR, CandyType.PURPLE_BEAR, CandyType.RED_BEAR),
            generator.poolForLevel(1).toSet(),
        )
        assertTrue(CandyType.GREEN_STAR in generator.poolForLevel(4))
        assertFalse(CandyType.ORANGE_HEART in generator.poolForLevel(6))
        assertTrue(CandyType.ORANGE_HEART in generator.poolForLevel(7))
        assertFalse(CandyType.PINK_BOMB in generator.poolForLevel(100))
    }

    @Test
    fun generatedPieceNeverContainsMoreThanTwoEqualCandies() {
        val generator = CandyGenerator(SeededRandomSource(17))
        for (level in 1..12) {
            repeat(250) {
                val candies = generator.generate(level)
                assertEquals(4, candies.size)
                assertTrue(candies.groupingBy { it }.eachCount().values.all { count -> count <= 2 })
                assertTrue(candies.all { it in generator.poolForLevel(level) })
            }
        }
    }
}
