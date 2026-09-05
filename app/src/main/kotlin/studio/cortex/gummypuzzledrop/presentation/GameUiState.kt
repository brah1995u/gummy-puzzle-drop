package studio.cortex.gummypuzzledrop.presentation

import studio.cortex.gummypuzzledrop.data.AchievementId

enum class AppScreen { MENU, GAMEPLAY, SETTINGS, ACHIEVEMENTS, DAILY, LEADERBOARD, SHOP, BLITZ }

enum class GameOverlay { NONE, TUTORIAL, PAUSE, GAME_OVER }

data class ComboBanner(
    val combo: Int,
    val points: Int,
    val token: Long,
)

data class RunRewards(
    val newAchievements: List<AchievementId> = emptyList(),
    val dailySetCompleted: Boolean = false,
    val starsEarned: Int = 0,
)
