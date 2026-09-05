package studio.cortex.gummypuzzledrop.data

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.floor

data class DailyProgress(
    val dayIndex: Long = Long.MIN_VALUE,
    val bestScore: Int = 0,
    val candiesCleared: Int = 0,
    val piecesPlaced: Int = 0,
    val gamesPlayed: Int = 0,
    val bestCombo: Int = 0,
    val bombsExploded: Int = 0,
)

data class LeaderboardEntry(
    val score: Int,
    val level: Int,
    val maxCombo: Int,
    val playedAtMillis: Long,
)

data class CompletedGame(
    val score: Int,
    val level: Int,
    val maxCombo: Int,
    val candiesCleared: Int,
    val piecesPlaced: Int,
    val bombsExploded: Int,
    val playedAtMillis: Long,
)

enum class DailyMetric { BEST_SCORE, CANDIES, PIECES, GAMES, BEST_COMBO, BOMBS }

data class DailyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val metric: DailyMetric,
    val target: Int,
    val progress: Int,
) {
    val complete: Boolean get() = progress >= target
    val fraction: Float get() = (progress.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
}

enum class AchievementId(
    val title: String,
    val description: String,
) {
    FIRST_DROP("First Drop", "Finish your first game"),
    SUGAR_RUSH("Sugar Rush", "Score 2,500 in one game"),
    CHAIN_REACTION("Chain Reaction", "Reach combo ×3"),
    GUMMY_COLLECTOR("Gummy Collector", "Clear 100 gummies"),
    MASTER_DROPPER("Master Dropper", "Place 100 pieces"),
    BOMB_SQUAD("Bomb Squad", "Explode 10 Pink Bombs"),
    LEVEL_CLIMBER("Level Climber", "Reach level 8"),
    DAILY_DEVOTION("Daily Devotion", "Complete 3 daily sets in a row"),
    GUMMY_VETERAN("Gummy Veteran", "Finish 25 games"),
    BLITZ_MASTER("Blitz Master", "Score 260 in Gummy Blitz"),
}

data class AchievementProgress(
    val id: AchievementId,
    val progress: Int,
    val target: Int,
) {
    val unlocked: Boolean get() = progress >= target
    val fraction: Float get() = (progress.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
}

object LocalDayClock {
    private const val MILLIS_PER_DAY = 86_400_000L

    fun dayIndex(
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault(),
    ): Long = floor((nowMillis + timeZone.getOffset(nowMillis)).toDouble() / MILLIS_PER_DAY).toLong()

    fun millisUntilNextDay(
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault(),
    ): Long {
        val nextMidnight = Calendar.getInstance(timeZone).apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return (nextMidnight - nowMillis).coerceAtLeast(0L)
    }
}

object MetaProgressRules {
    private const val LEADERBOARD_LIMIT = 10

    fun normalizeDay(progress: PlayerProgress, dayIndex: Long): PlayerProgress {
        if (progress.daily.dayIndex == dayIndex) return progress
        val streak = if (progress.lastDailyCompletionDay < dayIndex - 1L) 0 else progress.dailyStreak
        return progress.copy(daily = DailyProgress(dayIndex = dayIndex), dailyStreak = streak)
    }

    fun applyCompletedGame(
        progress: PlayerProgress,
        game: CompletedGame,
        dayIndex: Long,
    ): PlayerProgress {
        val current = normalizeDay(progress, dayIndex)
        val wasDailyComplete = dailyChallenges(current.daily).all(DailyChallenge::complete)
        val daily = current.daily.copy(
            bestScore = maxOf(current.daily.bestScore, game.score),
            candiesCleared = current.daily.candiesCleared + game.candiesCleared,
            piecesPlaced = current.daily.piecesPlaced + game.piecesPlaced,
            gamesPlayed = current.daily.gamesPlayed + 1,
            bestCombo = maxOf(current.daily.bestCombo, game.maxCombo),
            bombsExploded = current.daily.bombsExploded + game.bombsExploded,
        )
        val isDailyComplete = dailyChallenges(daily).all(DailyChallenge::complete)
        val completedForFirstTime = !wasDailyComplete && isDailyComplete && current.lastDailyCompletionDay != dayIndex
        val streak = if (completedForFirstTime) {
            if (current.lastDailyCompletionDay == dayIndex - 1L) current.dailyStreak + 1 else 1
        } else current.dailyStreak
        val completionDay = if (completedForFirstTime) dayIndex else current.lastDailyCompletionDay
        val newEntry = LeaderboardEntry(game.score, game.level, game.maxCombo, game.playedAtMillis)
        val leaderboard = (current.leaderboard + newEntry)
            .sortedWith(compareByDescending<LeaderboardEntry> { it.score }.thenByDescending { it.playedAtMillis })
            .take(LEADERBOARD_LIMIT)

        return current.copy(
            highScore = maxOf(current.highScore, game.score),
            highestCombo = maxOf(current.highestCombo, game.maxCombo),
            highestLevel = maxOf(current.highestLevel, game.level),
            gamesPlayed = current.gamesPlayed + 1,
            totalScore = current.totalScore + game.score,
            totalCandiesCleared = current.totalCandiesCleared + game.candiesCleared,
            totalPiecesPlaced = current.totalPiecesPlaced + game.piecesPlaced,
            totalBombsExploded = current.totalBombsExploded + game.bombsExploded,
            daily = daily,
            dailyStreak = streak,
            lastDailyCompletionDay = completionDay,
            leaderboard = leaderboard,
        )
    }

    fun dailyChallenges(progress: DailyProgress): List<DailyChallenge> {
        val templates = listOf(
            challenge("score", "Sugar Score", "Score 2,000 in one game", DailyMetric.BEST_SCORE, 2_000, progress.bestScore),
            challenge("clear", "Candy Cleanup", "Clear 30 gummies", DailyMetric.CANDIES, 30, progress.candiesCleared),
            challenge("pieces", "Perfect Placement", "Place 18 normal pieces", DailyMetric.PIECES, 18, progress.piecesPlaced),
            challenge("games", "Triple Treat", "Finish 3 games", DailyMetric.GAMES, 3, progress.gamesPlayed),
            challenge("combo", "Cascade Craze", "Reach combo ×2", DailyMetric.BEST_COMBO, 2, progress.bestCombo),
            challenge("bomb", "Pop Star", "Explode 1 Pink Bomb", DailyMetric.BOMBS, 1, progress.bombsExploded),
        )
        val start = Math.floorMod(progress.dayIndex, templates.size.toLong()).toInt()
        return listOf(templates[start], templates[(start + 2) % templates.size], templates[(start + 4) % templates.size])
    }

    fun achievements(progress: PlayerProgress): List<AchievementProgress> = listOf(
        achievement(AchievementId.FIRST_DROP, progress.gamesPlayed, 1),
        achievement(AchievementId.SUGAR_RUSH, progress.highScore, 2_500),
        achievement(AchievementId.CHAIN_REACTION, progress.highestCombo, 3),
        achievement(AchievementId.GUMMY_COLLECTOR, progress.totalCandiesCleared, 100),
        achievement(AchievementId.MASTER_DROPPER, progress.totalPiecesPlaced, 100),
        achievement(AchievementId.BOMB_SQUAD, progress.totalBombsExploded, 10),
        achievement(AchievementId.LEVEL_CLIMBER, progress.highestLevel, 8),
        achievement(AchievementId.DAILY_DEVOTION, progress.dailyStreak, 3),
        achievement(AchievementId.GUMMY_VETERAN, progress.gamesPlayed, 25),
        achievement(AchievementId.BLITZ_MASTER, progress.bestBlitzScore, 260),
    )

    private fun challenge(
        id: String,
        title: String,
        description: String,
        metric: DailyMetric,
        target: Int,
        progress: Int,
    ) = DailyChallenge(id, title, description, metric, target, progress.coerceAtLeast(0))

    private fun achievement(id: AchievementId, progress: Int, target: Int) =
        AchievementProgress(id, progress.coerceAtLeast(0), target)
}
