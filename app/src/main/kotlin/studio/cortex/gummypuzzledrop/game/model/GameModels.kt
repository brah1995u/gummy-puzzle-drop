package studio.cortex.gummypuzzledrop.game.model

enum class CandyType(val participatesInMatches: Boolean) {
    GREEN_BEAR(true),
    PURPLE_BEAR(true),
    RED_BEAR(true),
    GREEN_STAR(true),
    ORANGE_HEART(true),
    PINK_BOMB(false),
}

enum class PowerTreat {
    PINK_BOMB,
    RAINBOW_POP,
    SWEET_CLEANUP,
}

enum class PieceShape { I, O, T, L, J, S, Z }

enum class Rotation {
    R0,
    R90,
    R180,
    R270;

    fun clockwise(): Rotation = entries[(ordinal + 1) % entries.size]
}

data class GridPosition(val x: Int, val y: Int) {
    operator fun plus(other: GridPosition): GridPosition = GridPosition(x + other.x, y + other.y)
}

data class PieceCell(val position: GridPosition, val candy: CandyType)

sealed interface PieceSpec {
    val candies: List<CandyType>
}

data class NormalPieceSpec(
    val shape: PieceShape,
    override val candies: List<CandyType>,
) : PieceSpec {
    init {
        require(candies.size == 4) { "A normal piece must contain exactly four candies" }
        require(candies.all(CandyType::participatesInMatches)) { "Normal pieces cannot contain specials" }
        require(candies.groupingBy { it }.eachCount().values.all { it <= 2 }) {
            "A normal piece may not contain a candy more than twice"
        }
    }
}

data object BombPieceSpec : PieceSpec {
    override val candies: List<CandyType> = listOf(CandyType.PINK_BOMB)
}

data class ActivePiece(
    val spec: PieceSpec,
    val origin: GridPosition,
    val rotation: Rotation = Rotation.R0,
) {
    val isBomb: Boolean get() = spec === BombPieceSpec

    fun cells(): List<PieceCell> {
        val offsets = when (val value = spec) {
            is NormalPieceSpec -> ShapeDefinitions.offsets(value.shape, rotation)
            BombPieceSpec -> listOf(GridPosition(0, 0))
        }
        return offsets.zip(spec.candies) { offset, candy -> PieceCell(origin + offset, candy) }
    }

    fun moved(dx: Int = 0, dy: Int = 0): ActivePiece = copy(origin = GridPosition(origin.x + dx, origin.y + dy))
}

enum class GamePhase {
    SPAWNING,
    FALLING,
    LOCK_DELAY,
    RESOLVING,
    CASCADE,
    PAUSED,
    GAME_OVER,
}

data class CellMovement(
    val from: GridPosition,
    val to: GridPosition,
    val candy: CandyType,
)

sealed interface GameEvent {
    data class PieceMoved(val from: GridPosition, val to: GridPosition) : GameEvent
    data class PieceRotated(val from: Rotation, val to: Rotation, val kick: Int) : GameEvent
    data class HardDropped(val rows: Int, val bonus: Int) : GameEvent
    data class PieceLocked(val cells: List<PieceCell>, val special: Boolean) : GameEvent
    data class MatchesCleared(
        val positions: Set<GridPosition>,
        val combo: Int,
        val points: Int,
    ) : GameEvent

    data class GravityApplied(val movements: List<CellMovement>) : GameEvent
    data class ComboChanged(val combo: Int) : GameEvent
    data object BombEarned : GameEvent
    data class BombExploded(val center: GridPosition, val removed: Set<GridPosition>) : GameEvent
    data class LevelChanged(val level: Int) : GameEvent
    data object HoldUsed : GameEvent
    data object GamePaused : GameEvent
    data object GameResumed : GameEvent
    data class PowerTreatUsed(val treat: PowerTreat, val removed: Set<GridPosition>) : GameEvent
    data object GameOver : GameEvent
}

data class GameState(
    val board: Board,
    val active: ActivePiece?,
    val next: NormalPieceSpec,
    val hold: NormalPieceSpec? = null,
    val phase: GamePhase = GamePhase.SPAWNING,
    val resumePhase: GamePhase? = null,
    val score: Int = 0,
    val level: Int = 1,
    val normalPiecesPlaced: Int = 0,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val holdAvailable: Boolean = true,
    val fallAccumulatorMs: Long = 0L,
    val lockAccumulatorMs: Long = 0L,
) {
    val isGameOver: Boolean get() = phase == GamePhase.GAME_OVER
    val isPaused: Boolean get() = phase == GamePhase.PAUSED
    val isDanger: Boolean
        get() = board.cells.keys.any {
            it.y in GameConfig.SPAWN_ROWS until (GameConfig.SPAWN_ROWS + GameConfig.DANGER_VISIBLE_ROWS)
        }
}

data class EngineResult(
    val state: GameState,
    val events: List<GameEvent> = emptyList(),
)
