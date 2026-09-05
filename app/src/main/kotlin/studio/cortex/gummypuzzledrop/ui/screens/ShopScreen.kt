package studio.cortex.gummypuzzledrop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import studio.cortex.gummypuzzledrop.data.CosmeticSlot
import studio.cortex.gummypuzzledrop.data.PlayerProgress
import studio.cortex.gummypuzzledrop.data.ShopCatalog
import studio.cortex.gummypuzzledrop.data.ShopItem
import studio.cortex.gummypuzzledrop.data.PowerTreatCatalog
import studio.cortex.gummypuzzledrop.data.PowerTreatItem
import studio.cortex.gummypuzzledrop.game.model.CandyType
import studio.cortex.gummypuzzledrop.game.model.PowerTreat
import studio.cortex.gummypuzzledrop.ui.CandyAqua
import studio.cortex.gummypuzzledrop.ui.CandyCream
import studio.cortex.gummypuzzledrop.ui.CandyDeepPurple
import studio.cortex.gummypuzzledrop.ui.CandyMint
import studio.cortex.gummypuzzledrop.ui.CandyOrange
import studio.cortex.gummypuzzledrop.ui.CandyPink
import studio.cortex.gummypuzzledrop.ui.CandyPurple
import studio.cortex.gummypuzzledrop.ui.components.CandyMiniButton
import studio.cortex.gummypuzzledrop.ui.gameplay.CandyImage

@Composable
fun ShopScreen(
    progress: PlayerProgress,
    message: String?,
    onBuy: (String) -> Unit,
    onEquip: (String) -> Unit,
    onBuyPower: (PowerTreat) -> Unit,
    onBack: () -> Unit,
) {
    MetaScreenScaffold(
        title = "SUGAR SHOP",
        subtitle = "COSMETICS + POWER TREATS • EARNED OFFLINE",
        onBack = onBack,
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF681A86).copy(alpha = .86f), RoundedCornerShape(18.dp))
                    .border(1.5.dp, CandyCream.copy(alpha = .70f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("YOUR SUGAR STARS", color = CandyAqua, fontSize = 11.sp, fontWeight = FontWeight.Black)
                Text("★ ${progress.sugarStars}", color = CandyCream, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
            if (message != null) {
                Text(
                    message,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    color = CandyOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
            }
            PowerTreatSection(progress, onBuyPower)
            ShopSection("BOARD THEMES", CosmeticSlot.BOARD, progress, onBuy, onEquip)
            ShopSection("DROP EFFECTS", CosmeticSlot.EFFECT, progress, onBuy, onEquip)
            Spacer(Modifier.height(4.dp))
            Text(
                "Earn stars after every run, plus bonuses for achievements and a full daily set.",
                color = CandyMint,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PowerTreatSection(progress: PlayerProgress, onBuy: (PowerTreat) -> Unit) {
    Text(
        "POWER TREATS • USE IN GAME",
        modifier = Modifier.fillMaxWidth().padding(top = 13.dp, bottom = 5.dp),
        color = CandyOrange,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
    )
    PowerTreatCatalog.items.chunked(2).forEach { pair ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            if (pair.size == 1) Spacer(Modifier.weight(.5f))
            pair.forEach { item ->
                PowerTreatCard(item, progress.powerTreats[item.treat] ?: 0, onBuy, Modifier.weight(1f))
            }
            if (pair.size == 1) Spacer(Modifier.weight(.5f))
        }
        Spacer(Modifier.height(7.dp))
    }
}

@Composable
private fun PowerTreatCard(
    item: PowerTreatItem,
    count: Int,
    onBuy: (PowerTreat) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .background(Color(0xE45C176F), RoundedCornerShape(17.dp))
            .border(1.5.dp, CandyOrange.copy(alpha = .85f), RoundedCornerShape(17.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.fillMaxWidth().height(61.dp)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFFFFA8DC), Color(0xFF8E33BA))),
                    RoundedCornerShape(13.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            CandyImage(powerTreatCandy(item.treat), Modifier.size(54.dp))
            Text(
                "×$count",
                modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)
                    .background(CandyDeepPurple.copy(alpha = .88f), RoundedCornerShape(50)).padding(horizontal = 6.dp, vertical = 2.dp),
                color = CandyCream,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
            )
        }
        Text(
            item.title.uppercase(),
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
            color = CandyCream,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Text(
            item.description,
            modifier = Modifier.fillMaxWidth().height(29.dp),
            color = CandyCream.copy(alpha = .70f),
            fontSize = 8.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        CandyMiniButton(
            text = "BUY ★${item.price}",
            onClick = { onBuy(item.treat) },
            modifier = Modifier.fillMaxWidth(),
            orange = true,
            enabled = count < 99,
        )
    }
}

private fun powerTreatCandy(treat: PowerTreat): CandyType = when (treat) {
    PowerTreat.PINK_BOMB -> CandyType.PINK_BOMB
    PowerTreat.RAINBOW_POP -> CandyType.GREEN_STAR
    PowerTreat.SWEET_CLEANUP -> CandyType.PURPLE_BEAR
}

@Composable
private fun ShopSection(
    title: String,
    slot: CosmeticSlot,
    progress: PlayerProgress,
    onBuy: (String) -> Unit,
    onEquip: (String) -> Unit,
) {
    Text(
        title,
        modifier = Modifier.fillMaxWidth().padding(top = 13.dp, bottom = 5.dp),
        color = CandyMint,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
    )
    ShopCatalog.items.filter { it.slot == slot }.chunked(2).forEach { pair ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            pair.forEach { item ->
                ShopItemCard(item, progress, onBuy, onEquip, Modifier.weight(1f))
            }
            if (pair.size == 1) Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(7.dp))
    }
}

@Composable
private fun ShopItemCard(
    item: ShopItem,
    progress: PlayerProgress,
    onBuy: (String) -> Unit,
    onEquip: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val owned = item.id in progress.ownedCosmetics
    val equipped = when (item.slot) {
        CosmeticSlot.BOARD -> progress.equippedBoard == item.id
        CosmeticSlot.EFFECT -> progress.equippedEffect == item.id
    }
    val colors = previewColors(item.id)
    Column(
        modifier
            .background(Color(0xD84F1369), RoundedCornerShape(17.dp))
            .border(
                1.5.dp,
                if (equipped) CandyAqua else CandyCream.copy(alpha = .55f),
                RoundedCornerShape(17.dp),
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(61.dp)
                .background(Brush.verticalGradient(colors), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center,
        ) {
            CandyImage(previewCandy(item.id), Modifier.size(55.dp))
        }
        Text(
            item.title.uppercase(),
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
            color = CandyCream,
            fontSize = 11.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Text(
            item.description,
            modifier = Modifier.fillMaxWidth().height(29.dp),
            color = CandyCream.copy(alpha = .68f),
            fontSize = 8.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        CandyMiniButton(
            text = when {
                equipped -> "EQUIPPED"
                owned -> "EQUIP"
                else -> "BUY ★${item.price}"
            },
            onClick = { if (owned) onEquip(item.id) else onBuy(item.id) },
            modifier = Modifier.fillMaxWidth(),
            orange = !owned,
            enabled = !equipped,
        )
    }
}

private fun previewCandy(id: String): CandyType = when (id) {
    "mint_frost" -> CandyType.GREEN_BEAR
    "orange_pop" -> CandyType.ORANGE_HEART
    "aqua_dream" -> CandyType.PINK_BOMB
    "sparkle_effect" -> CandyType.GREEN_STAR
    "bubble_ghost" -> CandyType.PURPLE_BEAR
    "heart_glow" -> CandyType.ORANGE_HEART
    "clean_effect" -> CandyType.RED_BEAR
    else -> CandyType.PURPLE_BEAR
}

private fun previewColors(id: String): List<Color> = when (id) {
    "mint_frost" -> listOf(Color(0xFF9CFFDF), Color(0xFF298E83))
    "orange_pop" -> listOf(Color(0xFFFFD34F), Color(0xFFED7027))
    "aqua_dream" -> listOf(Color(0xFF8BEAFF), Color(0xFF594CC5))
    "sparkle_effect" -> listOf(Color(0xFFAAFF5F), CandyPurple)
    "bubble_ghost" -> listOf(CandyAqua, Color(0xFF3D5CC3))
    "heart_glow" -> listOf(Color(0xFFFF9FD8), CandyPink)
    else -> listOf(CandyPurple, CandyDeepPurple)
}
