package studio.cortex.gummypuzzledrop.game.engine

import kotlin.math.min
import studio.cortex.gummypuzzledrop.game.model.ActivePiece
import studio.cortex.gummypuzzledrop.game.model.Board
import studio.cortex.gummypuzzledrop.game.model.BombPieceSpec
import studio.cortex.gummypuzzledrop.game.model.CandyType
import studio.cortex.gummypuzzledrop.game.model.EngineResult
import studio.cortex.gummypuzzledrop.game.model.GameConfig
import studio.cortex.gummypuzzledrop.game.model.GameEvent
import studio.cortex.gummypuzzledrop.game.model.GamePhase
import studio.cortex.gummypuzzledrop.game.model.GameState
import studio.cortex.gummypuzzledrop.game.model.GridPosition
import studio.cortex.gummypuzzledrop.game.model.NormalPieceSpec
import studio.cortex.gummypuzzledrop.game.model.ShapeDefinitions
import studio.cortex.gummypuzzledrop.game.model.PowerTreat

class GameEngine(random: RandomSource = DefaultRandomSource) {
    private val pieceGenerator = PieceGenerator(random)
    private val candyGenerator = CandyGenerator(random)

    fun newGame(board: Board = Board()): EngineResult {
        pieceGenerator.reset()
        val first = generateNormalPiece(level = 1)
        val next = generateNormalPiece(level = 1)
        val active = spawn(first)
        val state = GameState(
            board = board,
            active = active,
            next = next,
            phase = if (CollisionSystem.isValid(active, board)) GamePhase.FALLING else GamePhase.GAME_OVER,
        )
        return if (state.isGameOver) EngineResult(state, listOf(GameEvent.GameOver)) else EngineResult(state)
    }

    fun advance(state: GameState, deltaMs: Long): EngineResult {
        if (deltaMs <= 0L || state.phase !in setOf(GamePhase.FALLING, GamePhase.LOCK_DELAY)) {
            return EngineResult(state)
        }
        var current = state
        var remaining = deltaMs
        val events = mutableListOf<GameEvent>()

        while (remaining > 0L && current.phase in setOf(GamePhase.FALLING, GamePhase.LOCK_DELAY)) {
            when (current.phase) {
                GamePhase.FALLING -> {
                    val interval = GameConfig.fallIntervalMs(current.level)
                    val needed = (interval - current.fallAccumulatorMs).coerceAtLeast(0L)
                    val consumed = min(remaining, needed)
                    remaining -= consumed
                    current = current.copy(fallAccumulatorMs = current.fallAccumulatorMs + consumed)
                    if (current.fallAccumulatorMs >= interval) {
                        val stepped = stepDown(current)
                        current = stepped.state
                        events += stepped.events
                    } else {
                        break
                    }
                }

                GamePhase.LOCK_DELAY -> {
                    val active = current.active ?: break
                    if (CollisionSystem.canMove(active, current.board, dy = 1)) {
                        current = current.copy(
                            phase = GamePhase.FALLING,
                            fallAccumulatorMs = 0L,
                            lockAccumulatorMs = 0L,
                        )
                        continue
                    }
                    val needed = (GameConfig.LOCK_DELAY_MS - current.lockAccumulatorMs).coerceAtLeast(0L)
                    val consumed = min(remaining, needed)
                    remaining -= consumed
                    current = current.copy(lockAccumulatorMs = current.lockAccumulatorMs + consumed)
                    if (current.lockAccumulatorMs >= GameConfig.LOCK_DELAY_MS) {
                        val locked = lockActive(current)
                        current = locked.state
                        events += locked.events
                    } else {
                        break
                    }
                }

                else -> break
            }
        }
        return EngineResult(current, events)
    }

    fun moveHorizontal(state: GameState, direction: Int): EngineResult {
        if (direction !in setOf(-1, 1) || !state.acceptsPlayerInput()) return EngineResult(state)
        val active = state.active ?: return EngineResult(state)
        val moved = active.moved(dx = direction)
        if (!CollisionSystem.isValid(moved, state.board)) return EngineResult(state)
        val adjusted = state.afterPlayerManipulation(moved)
        return EngineResult(adjusted, listOf(GameEvent.PieceMoved(active.origin, moved.origin)))
    }

    fun rotateClockwise(state: GameState): EngineResult {
        if (!state.acceptsPlayerInput()) return EngineResult(state)
        val active = state.active ?: return EngineResult(state)
        val rotation = RotationSystem.rotateClockwise(active, state.board) ?: return EngineResult(state)
        val adjusted = state.afterPlayerManipulation(rotation.piece)
        return EngineResult(
            adjusted,
            listOf(GameEvent.PieceRotated(active.rotation, rotation.piece.rotation, rotation.kick)),
        )
    }

    fun hardDrop(state: GameState): EngineResult {
        if (!state.acceptsPlayerInput()) return EngineResult(state)
        val active = state.active ?: return EngineResult(state)
        val rows = CollisionSystem.dropDistance(active, state.board)
        val bonus = ScoringSystem.hardDropBonus(rows)
        val dropped = state.copy(
            active = active.moved(dy = rows),
            score = state.score + bonus,
            fallAccumulatorMs = 0L,
            lockAccumulatorMs = 0L,
        )
        val locked = lockActive(dropped)
        return EngineResult(
            locked.state,
            listOf(GameEvent.HardDropped(rows, bonus)) + locked.events,
        )
    }

    fun hold(state: GameState): EngineResult {
        if (!state.acceptsPlayerInput() || !state.holdAvailable) return EngineResult(state)
        val activeSpec = state.active?.spec as? NormalPieceSpec ?: return EngineResult(state)
        val incoming: NormalPieceSpec
        val next: NormalPieceSpec
        if (state.hold == null) {
            incoming = state.next
            next = generateNormalPiece(state.level)
        } else {
            incoming = state.hold
            next = state.next
        }
        val active = spawn(incoming)
        val heldState = state.copy(
            active = active,
            next = next,
            hold = activeSpec,
            holdAvailable = false,
            phase = GamePhase.FALLING,
            fallAccumulatorMs = 0L,
            lockAccumulatorMs = 0L,
        )
        if (!CollisionSystem.isValid(active, state.board)) {
            return EngineResult(heldState.copy(active = null, phase = GamePhase.GAME_OVER), listOf(GameEvent.HoldUsed, GameEvent.GameOver))
        }
        return EngineResult(heldState, listOf(GameEvent.HoldUsed))
    }

    fun pause(state: GameState): EngineResult {
        if (state.phase !in setOf(GamePhase.FALLING, GamePhase.LOCK_DELAY)) return EngineResult(state)
        return EngineResult(
            state.copy(phase = GamePhase.PAUSED, resumePhase = state.phase),
            listOf(GameEvent.GamePaused),
        )
    }

    fun resume(state: GameState): EngineResult {
        if (state.phase != GamePhase.PAUSED) return EngineResult(state)
        return EngineResult(
            state.copy(phase = state.resumePhase ?: GamePhase.FALLING, resumePhase = null),
            listOf(GameEvent.GameResumed),
        )
    }

    fun ghost(state: GameState): ActivePiece? = state.active?.let { CollisionSystem.landingPiece(it, state.board) }

    fun usePowerTreat(state: GameState, treat: PowerTreat): EngineResult {
        if (!state.acceptsPlayerInput()) return EngineResult(state)
        return when (treat) {
            PowerTreat.PINK_BOMB -> {
                if (state.active?.isBomb == true) return EngineResult(state)
                val bomb = spawn(BombPieceSpec)
                if (!CollisionSystem.isValid(bomb, state.board)) return EngineResult(state)
                EngineResult(
                    state.copy(
                        active = bomb,
                        phase = GamePhase.FALLING,
                        holdAvailable = false,
                        fallAccumulatorMs = 0L,
                        lockAccumulatorMs = 0L,
                    ),
                    listOf(GameEvent.PowerTreatUsed(treat, emptySet())),
                )
            }

            PowerTreat.RAINBOW_POP -> {
                val target = state.board.cells.values
                    .filter(CandyType::participatesInMatches)
                    .groupingBy { it }
                    .eachCount()
                    .entries
                    .sortedWith(compareByDescending<Map.Entry<CandyType, Int>> { it.value }.thenBy { it.key.ordinal })
                    .firstOrNull()
                    ?.key ?: return EngineResult(state)
                resolvePowerClear(state, treat, state.board.cells.filterValues { it == target }.keys)
            }

            PowerTreat.SWEET_CLEANUP -> {
                val dangerBottom = GameConfig.SPAWN_ROWS + GameConfig.DANGER_VISIBLE_ROWS
                val positions = state.board.cells.keys.filterTo(linkedSetOf()) { it.y < dangerBottom }
                if (positions.isEmpty()) return EngineResult(state)
                resolvePowerClear(state, treat, positions)
            }
        }
    }

    private fun stepDown(state: GameState): EngineResult {
        val active = state.active ?: return EngineResult(state)
        val moved = active.moved(dy = 1)
        return if (CollisionSystem.isValid(moved, state.board)) {
            EngineResult(
                state.copy(
                    active = moved,
                    phase = GamePhase.FALLING,
                    fallAccumulatorMs = 0L,
                    lockAccumulatorMs = 0L,
                ),
                listOf(GameEvent.PieceMoved(active.origin, moved.origin)),
            )
        } else {
            EngineResult(
                state.copy(
                    phase = GamePhase.LOCK_DELAY,
                    fallAccumulatorMs = 0L,
                    lockAccumulatorMs = 0L,
                )
            )
        }
    }

    private fun resolvePowerClear(
        state: GameState,
        treat: PowerTreat,
        removed: Set<GridPosition>,
    ): EngineResult {
        val gravity = GravityResolver.apply(state.board.without(removed))
        val events = mutableListOf<GameEvent>(GameEvent.PowerTreatUsed(treat, removed))
        if (gravity.movements.isNotEmpty()) events += GameEvent.GravityApplied(gravity.movements)
        val resolution = applyResolution(
            state.copy(board = gravity.board, phase = GamePhase.RESOLVING, combo = 0),
            allowBombReward = false,
        )
        events += resolution.events
        val active = resolution.state.active
        val phase = if (active != null && CollisionSystem.canMove(active, resolution.state.board, dy = 1)) {
            GamePhase.FALLING
        } else {
            GamePhase.LOCK_DELAY
        }
        return EngineResult(
            resolution.state.copy(
                phase = phase,
                fallAccumulatorMs = 0L,
                lockAccumulatorMs = 0L,
            ),
            events,
        )
    }

    private fun lockActive(state: GameState): EngineResult {
        val active = state.active ?: return EngineResult(state)
        return if (active.isBomb) lockBomb(state, active) else lockNormal(state, active)
    }

    private fun lockNormal(state: GameState, active: ActivePiece): EngineResult {
        val lockedCells = active.cells()
        val board = state.board.withCells(lockedCells)
        val placed = state.normalPiecesPlaced + 1
        val level = GameConfig.levelForPlacedPieces(placed)
        val events = mutableListOf<GameEvent>(GameEvent.PieceLocked(lockedCells, special = false))
        if (level != state.level) events += GameEvent.LevelChanged(level)
        val resolving = state.copy(
            board = board,
            active = null,
            phase = GamePhase.RESOLVING,
            level = level,
            normalPiecesPlaced = placed,
            holdAvailable = true,
            fallAccumulatorMs = 0L,
            lockAccumulatorMs = 0L,
        )
        val resolution = applyResolution(resolving, allowBombReward = true)
        events += resolution.events
        val spawned = if (resolution.bombEarned) spawnBomb(resolution.state) else spawnNextNormal(resolution.state)
        events += spawned.events
        return EngineResult(spawned.state, events)
    }

    private fun lockBomb(state: GameState, active: ActivePiece): EngineResult {
        val center = active.cells().single().position
        val removed = BombSystem.occupiedAffectedPositions(state.board, center)
        val afterExplosion = state.board.without(removed)
        val gravity = GravityResolver.apply(afterExplosion)
        val events = mutableListOf<GameEvent>(
            GameEvent.PieceLocked(active.cells(), special = true),
            GameEvent.BombExploded(center, removed),
        )
        if (gravity.movements.isNotEmpty()) events += GameEvent.GravityApplied(gravity.movements)
        val resolving = state.copy(
            board = gravity.board,
            active = null,
            phase = GamePhase.RESOLVING,
            combo = 0,
            fallAccumulatorMs = 0L,
            lockAccumulatorMs = 0L,
        )
        val resolution = applyResolution(resolving, allowBombReward = false)
        events += resolution.events
        val spawned = spawnNextNormal(resolution.state)
        events += spawned.events
        return EngineResult(spawned.state, events)
    }

    private fun applyResolution(state: GameState, allowBombReward: Boolean): AppliedResolution {
        val resolution = BoardResolutionSystem.resolve(state.board, allowBombReward)
        val events = mutableListOf<GameEvent>()
        for (step in resolution.steps) {
            events += GameEvent.ComboChanged(step.combo)
            events += GameEvent.MatchesCleared(step.cleared, step.combo, step.points)
            if (step.movements.isNotEmpty()) events += GameEvent.GravityApplied(step.movements)
        }
        if (resolution.bombEarned) events += GameEvent.BombEarned
        val lastCombo = resolution.steps.lastOrNull()?.combo ?: 0
        return AppliedResolution(
            state = state.copy(
                board = resolution.board,
                phase = if (lastCombo > 1) GamePhase.CASCADE else GamePhase.RESOLVING,
                score = state.score + resolution.scoreAward,
                combo = lastCombo,
                maxCombo = maxOf(state.maxCombo, lastCombo),
            ),
            events = events,
            bombEarned = resolution.bombEarned,
        )
    }

    private fun spawnBomb(state: GameState): EngineResult {
        val active = spawn(BombPieceSpec)
        return finishSpawn(state, active, next = state.next, holdAvailable = false)
    }

    private fun spawnNextNormal(state: GameState): EngineResult {
        val active = spawn(state.next)
        val next = generateNormalPiece(state.level)
        return finishSpawn(state, active, next, holdAvailable = true)
    }

    private fun finishSpawn(
        state: GameState,
        active: ActivePiece,
        next: NormalPieceSpec,
        holdAvailable: Boolean,
    ): EngineResult {
        val spawned = state.copy(
            active = active,
            next = next,
            phase = GamePhase.FALLING,
            combo = 0,
            holdAvailable = holdAvailable,
            fallAccumulatorMs = 0L,
            lockAccumulatorMs = 0L,
        )
        return if (CollisionSystem.isValid(active, spawned.board)) {
            EngineResult(spawned)
        } else {
            EngineResult(spawned.copy(active = null, phase = GamePhase.GAME_OVER), listOf(GameEvent.GameOver))
        }
    }

    private fun generateNormalPiece(level: Int): NormalPieceSpec =
        NormalPieceSpec(pieceGenerator.next(), candyGenerator.generate(level))

    private fun spawn(spec: studio.cortex.gummypuzzledrop.game.model.PieceSpec): ActivePiece =
        ActivePiece(spec, ShapeDefinitions.spawnOrigin(spec))

    private fun GameState.acceptsPlayerInput(): Boolean =
        phase in setOf(GamePhase.FALLING, GamePhase.LOCK_DELAY) && active != null

    private fun GameState.afterPlayerManipulation(piece: ActivePiece): GameState {
        val canFall = CollisionSystem.canMove(piece, board, dy = 1)
        return copy(
            active = piece,
            phase = if (canFall) GamePhase.FALLING else GamePhase.LOCK_DELAY,
            fallAccumulatorMs = if (canFall) fallAccumulatorMs else 0L,
            lockAccumulatorMs = 0L,
        )
    }

    private data class AppliedResolution(
        val state: GameState,
        val events: List<GameEvent>,
        val bombEarned: Boolean,
    )
}
