package studio.cortex.gummypuzzledrop.data

import studio.cortex.gummypuzzledrop.game.model.PowerTreat

data class PlayerProgress(
    val highScore: Int = 0,
    val highestCombo: Int = 0,
    val highestLevel: Int = 1,
    val gamesPlayed: Int = 0,
    val totalScore: Long = 0L,
    val totalCandiesCleared: Int = 0,
    val totalPiecesPlaced: Int = 0,
    val totalBombsExploded: Int = 0,
    val tutorialSeen: Boolean = false,
    val musicEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val ghostEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val sugarStars: Int = 0,
    val ownedCosmetics: Set<String> = ShopCatalog.defaultOwned,
    val equippedBoard: String = ShopCatalog.DEFAULT_BOARD,
    val equippedEffect: String = ShopCatalog.DEFAULT_EFFECT,
    val powerTreats: Map<PowerTreat, Int> = ShopRules.normalizedInventory(emptyMap()),
    val bestBlitzScore: Int = 0,
    val lastBlitzRewardDay: Long = Long.MIN_VALUE,
    val daily: DailyProgress = DailyProgress(),
    val dailyStreak: Int = 0,
    val lastDailyCompletionDay: Long = Long.MIN_VALUE,
    val leaderboard: List<LeaderboardEntry> = emptyList(),
)
