package studio.cortex.gummypuzzledrop.game.engine

import studio.cortex.gummypuzzledrop.game.model.Board
import studio.cortex.gummypuzzledrop.game.model.CellMovement
import studio.cortex.gummypuzzledrop.game.model.GameConfig
import studio.cortex.gummypuzzledrop.game.model.GridPosition

data class ResolutionStep(
    val combo: Int,
    val cleared: Set<GridPosition>,
    val points: Int,
    val boardAfterClear: Board,
    val boardAfterGravity: Board,
    val movements: List<CellMovement>,
)

data class BoardResolution(
    val board: Board,
    val steps: List<ResolutionStep>,
    val scoreAward: Int,
    val bombEarned: Boolean,
)

object BoardResolutionSystem {
    fun resolve(initial: Board, allowBombReward: Boolean = true): BoardResolution {
        var board = initial
        var combo = 0
        var score = 0
        var bombEarned = false
        val steps = mutableListOf<ResolutionStep>()

        while (true) {
            val matches = MatchResolver.findMatches(board)
            if (matches.isEmpty()) break
            combo++
            val points = ScoringSystem.matchScore(matches.size, combo)
            val cleared = board.without(matches)
            val gravity = GravityResolver.apply(cleared)
            steps += ResolutionStep(combo, matches, points, cleared, gravity.board, gravity.movements)
            board = gravity.board
            score += points
            if (allowBombReward && combo >= GameConfig.BOMB_REWARD_COMBO) bombEarned = true
        }

        return BoardResolution(board, steps, score, bombEarned)
    }
}
