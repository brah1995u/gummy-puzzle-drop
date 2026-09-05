package studio.cortex.gummypuzzledrop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.cortex.gummypuzzledrop.data.AchievementId
import studio.cortex.gummypuzzledrop.data.AchievementProgress
import studio.cortex.gummypuzzledrop.data.MetaProgressRules
import studio.cortex.gummypuzzledrop.data.PlayerProgress
import studio.cortex.gummypuzzledrop.game.model.CandyType
import studio.cortex.gummypuzzledrop.ui.CandyAqua
import studio.cortex.gummypuzzledrop.ui.CandyCream
import studio.cortex.gummypuzzledrop.ui.CandyDeepPurple
import studio.cortex.gummypuzzledrop.ui.CandyMint
import studio.cortex.gummypuzzledrop.ui.components.CandyProgressBar
import studio.cortex.gummypuzzledrop.ui.components.StatPill
import studio.cortex.gummypuzzledrop.ui.gameplay.CandyImage

@Composable
fun AchievementsScreen(progress: PlayerProgress, onBack: () -> Unit) {
    val achievements = MetaProgressRules.achievements(progress)
    val unlocked = achievements.count(AchievementProgress::unlocked)
    MetaScreenScaffold(
        title = "ACHIEVEMENTS",
        subtitle = "Permanent milestones — progress never resets",
        onBack = onBack,
    ) {
        LazyColumn(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    StatPill("Unlocked", "$unlocked/${achievements.size}")
                    StatPill("Progress", "${unlocked * 100 / achievements.size}%")
                }
            }
            items(achievements, key = { it.id }) { achievement ->
                AchievementCard(achievement)
            }
            item { Spacer(Modifier.height(2.dp)) }
        }
    }
}

@Composable
private fun AchievementCard(achievement: AchievementProgress) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .background(CandyDeepPurple.copy(alpha = .78f), shape)
            .border(
                1.5.dp,
                if (achievement.unlocked) CandyAqua else CandyCream.copy(alpha = .34f),
                shape,
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CandyImage(
            achievementCandy(achievement.id),
            Modifier.size(52.dp).alpha(if (achievement.unlocked) 1f else .42f),
        )
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    achievement.id.title.uppercase(),
                    modifier = Modifier.weight(1f),
                    color = if (achievement.unlocked) CandyMint else CandyCream,
                    fontSize = 13.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    if (achievement.unlocked) "UNLOCKED" else "${achievement.progress.coerceAtMost(achievement.target)}/${achievement.target}",
                    color = if (achievement.unlocked) CandyAqua else CandyCream.copy(alpha = .70f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            Text(
                achievement.id.description,
                color = CandyCream.copy(alpha = .68f),
                fontSize = 10.sp,
                lineHeight = 13.sp,
            )
            Spacer(Modifier.height(7.dp))
            CandyProgressBar(achievement.fraction, completed = achievement.unlocked)
        }
    }
}

private fun achievementCandy(id: AchievementId): CandyType = when (id) {
    AchievementId.FIRST_DROP, AchievementId.MASTER_DROPPER -> CandyType.GREEN_BEAR
    AchievementId.SUGAR_RUSH, AchievementId.GUMMY_VETERAN -> CandyType.ORANGE_HEART
    AchievementId.CHAIN_REACTION, AchievementId.DAILY_DEVOTION,
    AchievementId.BLITZ_MASTER -> CandyType.GREEN_STAR
    AchievementId.GUMMY_COLLECTOR, AchievementId.LEVEL_CLIMBER -> CandyType.PURPLE_BEAR
    AchievementId.BOMB_SQUAD -> CandyType.PINK_BOMB
}
