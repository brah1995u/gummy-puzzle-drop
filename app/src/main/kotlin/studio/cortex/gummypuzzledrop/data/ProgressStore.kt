package studio.cortex.gummypuzzledrop.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import studio.cortex.gummypuzzledrop.game.model.PowerTreat

private val Context.gummyProgressStore by preferencesDataStore("gummy_puzzle_drop_progress_v1")

class ProgressStore(private val context: Context) {
    private object Keys {
        val highScore = intPreferencesKey("high_score")
        val highestCombo = intPreferencesKey("highest_combo")
        val highestLevel = intPreferencesKey("highest_level")
        val gamesPlayed = intPreferencesKey("games_played")
        val totalScore = longPreferencesKey("total_score")
        val totalCandiesCleared = intPreferencesKey("total_candies_cleared")
        val totalPiecesPlaced = intPreferencesKey("total_pieces_placed")
        val totalBombsExploded = intPreferencesKey("total_bombs_exploded")
        val tutorialSeen = booleanPreferencesKey("tutorial_seen")
        val music = booleanPreferencesKey("music_enabled")
        val sound = booleanPreferencesKey("sound_enabled")
        val vibration = booleanPreferencesKey("vibration_enabled")
        val ghost = booleanPreferencesKey("ghost_enabled")
        val reducedMotion = booleanPreferencesKey("reduced_motion")
        val sugarStars = intPreferencesKey("sugar_stars")
        val ownedCosmetics = stringPreferencesKey("owned_cosmetics")
        val equippedBoard = stringPreferencesKey("equipped_board")
        val equippedEffect = stringPreferencesKey("equipped_effect")
        val powerTreats = stringPreferencesKey("power_treats")
        val bestBlitzScore = intPreferencesKey("best_blitz_score")
        val lastBlitzRewardDay = longPreferencesKey("last_blitz_reward_day")
        val dailyDay = longPreferencesKey("daily_day")
        val dailyBestScore = intPreferencesKey("daily_best_score")
        val dailyCandies = intPreferencesKey("daily_candies")
        val dailyPieces = intPreferencesKey("daily_pieces")
        val dailyGames = intPreferencesKey("daily_games")
        val dailyBestCombo = intPreferencesKey("daily_best_combo")
        val dailyBombs = intPreferencesKey("daily_bombs")
        val dailyStreak = intPreferencesKey("daily_streak")
        val lastDailyCompletionDay = longPreferencesKey("last_daily_completion_day")
        val leaderboard = stringPreferencesKey("local_leaderboard")
    }

    val progress: Flow<PlayerProgress> = context.gummyProgressStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw error
        }
        .map { values ->
            PlayerProgress(
                highScore = (values[Keys.highScore] ?: 0).coerceAtLeast(0),
                highestCombo = (values[Keys.highestCombo] ?: 0).coerceAtLeast(0),
                highestLevel = (values[Keys.highestLevel] ?: 1).coerceAtLeast(1),
                gamesPlayed = (values[Keys.gamesPlayed] ?: 0).coerceAtLeast(0),
                totalScore = (values[Keys.totalScore] ?: 0L).coerceAtLeast(0L),
                totalCandiesCleared = (values[Keys.totalCandiesCleared] ?: 0).coerceAtLeast(0),
                totalPiecesPlaced = (values[Keys.totalPiecesPlaced] ?: 0).coerceAtLeast(0),
                totalBombsExploded = (values[Keys.totalBombsExploded] ?: 0).coerceAtLeast(0),
                tutorialSeen = values[Keys.tutorialSeen] ?: false,
                musicEnabled = values[Keys.music] ?: true,
                soundEnabled = values[Keys.sound] ?: true,
                vibrationEnabled = values[Keys.vibration] ?: true,
                ghostEnabled = values[Keys.ghost] ?: true,
                reducedMotion = values[Keys.reducedMotion] ?: false,
                sugarStars = (values[Keys.sugarStars] ?: 0).coerceAtLeast(0),
                ownedCosmetics = ShopRules.normalizedOwned(decodeCosmetics(values[Keys.ownedCosmetics])),
                equippedBoard = ShopRules.validBoard(values[Keys.equippedBoard].orEmpty()),
                equippedEffect = ShopRules.validEffect(values[Keys.equippedEffect].orEmpty()),
                powerTreats = decodePowerTreats(values[Keys.powerTreats]),
                bestBlitzScore = (values[Keys.bestBlitzScore] ?: 0).coerceAtLeast(0),
                lastBlitzRewardDay = values[Keys.lastBlitzRewardDay] ?: Long.MIN_VALUE,
                daily = DailyProgress(
                    dayIndex = values[Keys.dailyDay] ?: Long.MIN_VALUE,
                    bestScore = (values[Keys.dailyBestScore] ?: 0).coerceAtLeast(0),
                    candiesCleared = (values[Keys.dailyCandies] ?: 0).coerceAtLeast(0),
                    piecesPlaced = (values[Keys.dailyPieces] ?: 0).coerceAtLeast(0),
                    gamesPlayed = (values[Keys.dailyGames] ?: 0).coerceAtLeast(0),
                    bestCombo = (values[Keys.dailyBestCombo] ?: 0).coerceAtLeast(0),
                    bombsExploded = (values[Keys.dailyBombs] ?: 0).coerceAtLeast(0),
                ),
                dailyStreak = (values[Keys.dailyStreak] ?: 0).coerceAtLeast(0),
                lastDailyCompletionDay = values[Keys.lastDailyCompletionDay] ?: Long.MIN_VALUE,
                leaderboard = decodeLeaderboard(values[Keys.leaderboard]),
            )
        }

    suspend fun save(value: PlayerProgress) {
        context.gummyProgressStore.edit { values ->
            values[Keys.highScore] = value.highScore.coerceAtLeast(0)
            values[Keys.highestCombo] = value.highestCombo.coerceAtLeast(0)
            values[Keys.highestLevel] = value.highestLevel.coerceAtLeast(1)
            values[Keys.gamesPlayed] = value.gamesPlayed.coerceAtLeast(0)
            values[Keys.totalScore] = value.totalScore.coerceAtLeast(0L)
            values[Keys.totalCandiesCleared] = value.totalCandiesCleared.coerceAtLeast(0)
            values[Keys.totalPiecesPlaced] = value.totalPiecesPlaced.coerceAtLeast(0)
            values[Keys.totalBombsExploded] = value.totalBombsExploded.coerceAtLeast(0)
            values[Keys.tutorialSeen] = value.tutorialSeen
            values[Keys.music] = value.musicEnabled
            values[Keys.sound] = value.soundEnabled
            values[Keys.vibration] = value.vibrationEnabled
            values[Keys.ghost] = value.ghostEnabled
            values[Keys.reducedMotion] = value.reducedMotion
            values[Keys.sugarStars] = value.sugarStars.coerceAtLeast(0)
            values[Keys.ownedCosmetics] = ShopRules.normalizedOwned(value.ownedCosmetics)
                .sorted()
                .joinToString(",")
            values[Keys.equippedBoard] = ShopRules.validBoard(value.equippedBoard)
            values[Keys.equippedEffect] = ShopRules.validEffect(value.equippedEffect)
            values[Keys.powerTreats] = encodePowerTreats(value.powerTreats)
            values[Keys.bestBlitzScore] = value.bestBlitzScore.coerceAtLeast(0)
            values[Keys.lastBlitzRewardDay] = value.lastBlitzRewardDay
            values[Keys.dailyDay] = value.daily.dayIndex
            values[Keys.dailyBestScore] = value.daily.bestScore.coerceAtLeast(0)
            values[Keys.dailyCandies] = value.daily.candiesCleared.coerceAtLeast(0)
            values[Keys.dailyPieces] = value.daily.piecesPlaced.coerceAtLeast(0)
            values[Keys.dailyGames] = value.daily.gamesPlayed.coerceAtLeast(0)
            values[Keys.dailyBestCombo] = value.daily.bestCombo.coerceAtLeast(0)
            values[Keys.dailyBombs] = value.daily.bombsExploded.coerceAtLeast(0)
            values[Keys.dailyStreak] = value.dailyStreak.coerceAtLeast(0)
            values[Keys.lastDailyCompletionDay] = value.lastDailyCompletionDay
            values[Keys.leaderboard] = encodeLeaderboard(value.leaderboard)
        }
    }

    private fun encodeLeaderboard(entries: List<LeaderboardEntry>): String = entries.joinToString("|") {
        "${it.score}:${it.level}:${it.maxCombo}:${it.playedAtMillis}"
    }

    private fun decodeCosmetics(encoded: String?): Set<String> = encoded
        ?.split(',')
        ?.map(String::trim)
        ?.filter(String::isNotEmpty)
        ?.toSet()
        .orEmpty()

    private fun encodePowerTreats(inventory: Map<PowerTreat, Int>): String =
        ShopRules.normalizedInventory(inventory).entries.joinToString(",") { "${it.key.name}:${it.value}" }

    private fun decodePowerTreats(encoded: String?): Map<PowerTreat, Int> {
        val decoded = encoded
            ?.split(',')
            ?.mapNotNull { entry ->
                val parts = entry.split(':')
                val treat = parts.getOrNull(0)?.let { runCatching { PowerTreat.valueOf(it) }.getOrNull() }
                    ?: return@mapNotNull null
                val count = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
                treat to count
            }
            ?.toMap()
            .orEmpty()
        return ShopRules.normalizedInventory(decoded)
    }

    private fun decodeLeaderboard(encoded: String?): List<LeaderboardEntry> = encoded
        ?.split('|')
        ?.mapNotNull { entry ->
            val parts = entry.split(':')
            if (parts.size != 4) return@mapNotNull null
            val score = parts[0].toIntOrNull() ?: return@mapNotNull null
            val level = parts[1].toIntOrNull() ?: return@mapNotNull null
            val combo = parts[2].toIntOrNull() ?: return@mapNotNull null
            val timestamp = parts[3].toLongOrNull() ?: return@mapNotNull null
            LeaderboardEntry(
                score = score.coerceAtLeast(0),
                level = level.coerceAtLeast(1),
                maxCombo = combo.coerceAtLeast(0),
                playedAtMillis = timestamp.coerceAtLeast(0L),
            )
        }
        ?.sortedWith(compareByDescending<LeaderboardEntry> { it.score }.thenByDescending { it.playedAtMillis })
        ?.take(10)
        .orEmpty()
}
