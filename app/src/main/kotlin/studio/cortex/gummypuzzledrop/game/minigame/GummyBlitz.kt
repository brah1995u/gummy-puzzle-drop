package studio.cortex.gummypuzzledrop.game.minigame

import studio.cortex.gummypuzzledrop.game.engine.DefaultRandomSource
import studio.cortex.gummypuzzledrop.game.engine.RandomSource
import studio.cortex.gummypuzzledrop.game.model.CandyType
import studio.cortex.gummypuzzledrop.game.model.PowerTreat

enum class BlitzPhase { READY, PLAYING, PAUSED, FINISHED }

data class GummyBlitzState(
    val phase: BlitzPhase = BlitzPhase.READY,
    val cells: List<CandyType> = emptyList(),
    val target: CandyType = CandyType.GREEN_BEAR,
    val score: Int = 0,
    val combo: Int = 0,
    val bestCombo: Int = 0,
    val lives: Int = 3,
    val correctHits: Int = 0,
    val remainingMs: Long = GummyBlitzEngine.ROUND_MS,
)

data class BlitzTapResult(
    val state: GummyBlitzState,
    val correct: Boolean,
    val points: Int = 0,
)

data class BlitzReward(val stars: Int, val powerTreat: PowerTreat?)

object GummyBlitzRewards {
    fun forScore(score: Int): BlitzReward = BlitzReward(
        stars = (3 + score.coerceAtLeast(0) / 45).coerceAtMost(18),
        powerTreat = when {
            score >= 420 -> PowerTreat.SWEET_CLEANUP
            score >= 260 -> PowerTreat.RAINBOW_POP
            score >= 140 -> PowerTreat.PINK_BOMB
            else -> null
        },
    )
}

class GummyBlitzEngine(private val random: RandomSource = DefaultRandomSource) {
    fun ready(): GummyBlitzState {
        val target = randomCandy()
        return GummyBlitzState(cells = generateCells(target), target = target)
    }

    fun start(): GummyBlitzState = ready().copy(phase = BlitzPhase.PLAYING)

    fun advance(state: GummyBlitzState, deltaMs: Long): GummyBlitzState {
        if (state.phase != BlitzPhase.PLAYING || deltaMs <= 0L) return state
        val remaining = (state.remainingMs - deltaMs).coerceAtLeast(0L)
        return state.copy(
            remainingMs = remaining,
            phase = if (remaining == 0L) BlitzPhase.FINISHED else BlitzPhase.PLAYING,
        )
    }

    fun pause(state: GummyBlitzState): GummyBlitzState =
        if (state.phase == BlitzPhase.PLAYING) state.copy(phase = BlitzPhase.PAUSED) else state

    fun resume(state: GummyBlitzState): GummyBlitzState =
        if (state.phase == BlitzPhase.PAUSED) state.copy(phase = BlitzPhase.PLAYING) else state

    fun tap(state: GummyBlitzState, index: Int): BlitzTapResult {
        if (state.phase != BlitzPhase.PLAYING || index !in state.cells.indices) {
            return BlitzTapResult(state, correct = false)
        }
        if (state.cells[index] != state.target) {
            val lives = (state.lives - 1).coerceAtLeast(0)
            return BlitzTapResult(
                state.copy(
                    lives = lives,
                    combo = 0,
                    phase = if (lives == 0) BlitzPhase.FINISHED else BlitzPhase.PLAYING,
                ),
                correct = false,
            )
        }

        val combo = state.combo + 1
        val points = 12 + (combo - 1).coerceAtMost(8) * 3
        val hits = state.correctHits + 1
        val target = if (hits % 3 == 0) randomCandy() else state.target
        val cells = state.cells.toMutableList().apply { this[index] = randomCandy() }
        ensureTarget(cells, target)
        return BlitzTapResult(
            state.copy(
                cells = cells,
                target = target,
                score = state.score + points,
                combo = combo,
                bestCombo = maxOf(state.bestCombo, combo),
                correctHits = hits,
            ),
            correct = true,
            points = points,
        )
    }

    private fun generateCells(target: CandyType): List<CandyType> =
        MutableList(CELL_COUNT) { randomCandy() }.also { ensureTarget(it, target) }

    private fun ensureTarget(cells: MutableList<CandyType>, target: CandyType) {
        if (target !in cells) cells[random.nextInt(cells.size)] = target
    }

    private fun randomCandy(): CandyType = CANDY_POOL[random.nextInt(CANDY_POOL.size)]

    companion object {
        const val ROUND_MS = 20_000L
        const val CELL_COUNT = 9
        val CANDY_POOL = listOf(
            CandyType.GREEN_BEAR,
            CandyType.PURPLE_BEAR,
            CandyType.RED_BEAR,
            CandyType.GREEN_STAR,
            CandyType.ORANGE_HEART,
        )
    }
}
