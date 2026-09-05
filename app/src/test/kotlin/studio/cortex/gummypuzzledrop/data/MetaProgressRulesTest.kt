package studio.cortex.gummypuzzledrop.data

import java.util.TimeZone
import java.util.Calendar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MetaProgressRulesTest {
    @Test
    fun `daily selection is deterministic and contains three unique goals`() {
        val daily = DailyProgress(dayIndex = 21_100)

        val first = MetaProgressRules.dailyChallenges(daily)
        val second = MetaProgressRules.dailyChallenges(daily)

        assertEquals(first, second)
        assertEquals(3, first.size)
        assertEquals(3, first.map { it.id }.distinct().size)
    }

    @Test
    fun `daily rollover clears counters and preserves only an active streak`() {
        val active = PlayerProgress(
            daily = DailyProgress(dayIndex = 99, bestScore = 500, gamesPlayed = 2),
            dailyStreak = 4,
            lastDailyCompletionDay = 99,
        )

        val nextDay = MetaProgressRules.normalizeDay(active, 100)
        val missedDay = MetaProgressRules.normalizeDay(active, 102)

        assertEquals(DailyProgress(dayIndex = 100), nextDay.daily)
        assertEquals(4, nextDay.dailyStreak)
        assertEquals(0, missedDay.dailyStreak)
    }

    @Test
    fun `completed daily set increments streak only once per day`() {
        val first = MetaProgressRules.applyCompletedGame(PlayerProgress(), maxedGame(1_000L), 400)
        val repeated = MetaProgressRules.applyCompletedGame(first, maxedGame(2_000L), 400)
        val nextDayFirst = MetaProgressRules.applyCompletedGame(repeated, maxedGame(3_000L), 401)
        val nextDaySecond = MetaProgressRules.applyCompletedGame(nextDayFirst, maxedGame(4_000L), 401)
        val nextDayComplete = MetaProgressRules.applyCompletedGame(nextDaySecond, maxedGame(5_000L), 401)

        assertTrue(MetaProgressRules.dailyChallenges(first.daily).all { it.complete })
        assertEquals(1, first.dailyStreak)
        assertEquals(1, repeated.dailyStreak)
        assertEquals(2, nextDayComplete.dailyStreak)
    }

    @Test
    fun `game results update lifetime totals and keep sorted top ten`() {
        var progress = PlayerProgress()
        repeat(12) { index ->
            progress = MetaProgressRules.applyCompletedGame(
                progress,
                maxedGame(index.toLong()).copy(score = index * 100, level = index + 1),
                dayIndex = 700,
            )
        }

        assertEquals(12, progress.gamesPlayed)
        assertEquals(10, progress.leaderboard.size)
        assertEquals(1_100, progress.leaderboard.first().score)
        assertEquals(200, progress.leaderboard.last().score)
        assertEquals(12, progress.highestLevel)
        assertTrue(progress.totalScore > 0)
    }

    @Test
    fun `achievement progress is derived from durable statistics`() {
        val progress = PlayerProgress(
            highScore = 2_500,
            highestCombo = 3,
            highestLevel = 8,
            gamesPlayed = 1,
            totalCandiesCleared = 99,
            totalPiecesPlaced = 100,
            totalBombsExploded = 10,
            dailyStreak = 3,
        )
        val achievements = MetaProgressRules.achievements(progress).associateBy { it.id }

        assertTrue(achievements.getValue(AchievementId.SUGAR_RUSH).unlocked)
        assertTrue(achievements.getValue(AchievementId.MASTER_DROPPER).unlocked)
        assertFalse(achievements.getValue(AchievementId.GUMMY_COLLECTOR).unlocked)
        assertFalse(achievements.getValue(AchievementId.GUMMY_VETERAN).unlocked)
    }

    @Test
    fun `local day index follows the supplied timezone`() {
        val utc = TimeZone.getTimeZone("UTC")
        val plusTwo = TimeZone.getTimeZone("GMT+02:00")
        val nearMidnightUtc = 86_400_000L - 60_000L

        assertEquals(0, LocalDayClock.dayIndex(nearMidnightUtc, utc))
        assertEquals(1, LocalDayClock.dayIndex(nearMidnightUtc, plusTwo))
    }

    @Test
    fun `daily countdown targets the next local midnight`() {
        val zone = TimeZone.getTimeZone("Europe/Madrid")
        val now = Calendar.getInstance(zone).apply {
            set(2026, Calendar.AUGUST, 20, 23, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        assertEquals(30 * 60_000L, LocalDayClock.millisUntilNextDay(now, zone))
    }

    private fun maxedGame(timestamp: Long) = CompletedGame(
        score = 10_000,
        level = 10,
        maxCombo = 5,
        candiesCleared = 100,
        piecesPlaced = 100,
        bombsExploded = 10,
        playedAtMillis = timestamp,
    )
}
