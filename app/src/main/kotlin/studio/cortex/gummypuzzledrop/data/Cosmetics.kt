package studio.cortex.gummypuzzledrop.data

import studio.cortex.gummypuzzledrop.game.model.PowerTreat

enum class CosmeticSlot { BOARD, EFFECT }

data class ShopItem(
    val id: String,
    val title: String,
    val description: String,
    val price: Int,
    val slot: CosmeticSlot,
)

object ShopCatalog {
    const val DEFAULT_BOARD = "classic_board"
    const val DEFAULT_EFFECT = "clean_effect"
    val defaultOwned: Set<String> = setOf(DEFAULT_BOARD, DEFAULT_EFFECT)

    val items: List<ShopItem> = listOf(
        ShopItem(DEFAULT_BOARD, "Classic Gummy", "Deep berry candy board", 0, CosmeticSlot.BOARD),
        ShopItem("mint_frost", "Mint Frost", "Cool mint and aqua glass", 35, CosmeticSlot.BOARD),
        ShopItem("orange_pop", "Orange Pop", "Warm citrus candy glow", 55, CosmeticSlot.BOARD),
        ShopItem("aqua_dream", "Aqua Dream", "Dreamy blue sugar glass", 75, CosmeticSlot.BOARD),
        ShopItem(DEFAULT_EFFECT, "Clean Drop", "The original crisp candy look", 0, CosmeticSlot.EFFECT),
        ShopItem("sparkle_effect", "Sugar Sparkle", "Tiny star glints on active gummies", 45, CosmeticSlot.EFFECT),
        ShopItem("bubble_ghost", "Bubble Ghost", "Aqua bubble landing preview", 60, CosmeticSlot.EFFECT),
        ShopItem("heart_glow", "Heart Glow", "Pink candy aura while dropping", 85, CosmeticSlot.EFFECT),
    )

    fun find(id: String): ShopItem? = items.firstOrNull { it.id == id }
}

data class PowerTreatItem(
    val treat: PowerTreat,
    val title: String,
    val description: String,
    val price: Int,
)

object PowerTreatCatalog {
    val items: List<PowerTreatItem> = listOf(
        PowerTreatItem(PowerTreat.PINK_BOMB, "Pink Bomb", "Turn the active piece into a 3 × 3 bomb", 18),
        PowerTreatItem(PowerTreat.RAINBOW_POP, "Rainbow Pop", "Remove the most common gummy color", 24),
        PowerTreatItem(PowerTreat.SWEET_CLEANUP, "Sweet Cleanup", "Clear gummies from the danger zone", 28),
    )

    fun find(treat: PowerTreat): PowerTreatItem = items.first { it.treat == treat }
}

sealed interface PurchaseResult {
    data class Purchased(val balance: Int, val owned: Set<String>) : PurchaseResult
    data object AlreadyOwned : PurchaseResult
    data object NotEnoughStars : PurchaseResult
    data object UnknownItem : PurchaseResult
}

sealed interface EquipResult {
    data class Equipped(val board: String, val effect: String) : EquipResult
    data object NotOwned : EquipResult
    data object UnknownItem : EquipResult
}

sealed interface PowerPurchaseResult {
    data class Purchased(val balance: Int, val inventory: Map<PowerTreat, Int>) : PowerPurchaseResult
    data object NotEnoughStars : PowerPurchaseResult
}

object ShopRules {
    fun normalizedOwned(owned: Set<String>): Set<String> =
        (owned + ShopCatalog.defaultOwned).filterTo(linkedSetOf()) { ShopCatalog.find(it) != null }

    fun purchase(balance: Int, owned: Set<String>, itemId: String): PurchaseResult {
        val item = ShopCatalog.find(itemId) ?: return PurchaseResult.UnknownItem
        val safeOwned = normalizedOwned(owned)
        if (item.id in safeOwned) return PurchaseResult.AlreadyOwned
        if (balance.coerceAtLeast(0) < item.price) return PurchaseResult.NotEnoughStars
        return PurchaseResult.Purchased(
            balance = balance.coerceAtLeast(0) - item.price,
            owned = safeOwned + item.id,
        )
    }

    fun equip(
        owned: Set<String>,
        itemId: String,
        currentBoard: String,
        currentEffect: String,
    ): EquipResult {
        val item = ShopCatalog.find(itemId) ?: return EquipResult.UnknownItem
        if (item.id !in normalizedOwned(owned)) return EquipResult.NotOwned
        return when (item.slot) {
            CosmeticSlot.BOARD -> EquipResult.Equipped(item.id, validEffect(currentEffect))
            CosmeticSlot.EFFECT -> EquipResult.Equipped(validBoard(currentBoard), item.id)
        }
    }

    fun rewardForRun(score: Int, achievementCount: Int, dailySetCompleted: Boolean): Int =
        (3 + score.coerceAtLeast(0) / 500 + achievementCount.coerceAtLeast(0) * 8 +
            if (dailySetCompleted) 20 else 0).coerceAtMost(45)

    fun normalizedInventory(inventory: Map<PowerTreat, Int>): Map<PowerTreat, Int> =
        PowerTreat.entries.associateWith { inventory[it]?.coerceIn(0, 99) ?: 0 }

    fun purchasePowerTreat(
        balance: Int,
        inventory: Map<PowerTreat, Int>,
        treat: PowerTreat,
    ): PowerPurchaseResult {
        val item = PowerTreatCatalog.find(treat)
        val safeBalance = balance.coerceAtLeast(0)
        if (safeBalance < item.price) return PowerPurchaseResult.NotEnoughStars
        val normalized = normalizedInventory(inventory)
        return PowerPurchaseResult.Purchased(
            balance = safeBalance - item.price,
            inventory = normalized + (treat to ((normalized[treat] ?: 0) + 1).coerceAtMost(99)),
        )
    }

    fun consumePowerTreat(inventory: Map<PowerTreat, Int>, treat: PowerTreat): Map<PowerTreat, Int>? {
        val normalized = normalizedInventory(inventory)
        val count = normalized[treat] ?: 0
        if (count <= 0) return null
        return normalized + (treat to count - 1)
    }

    fun validBoard(id: String): String = ShopCatalog.find(id)
        ?.takeIf { it.slot == CosmeticSlot.BOARD }
        ?.id ?: ShopCatalog.DEFAULT_BOARD

    fun validEffect(id: String): String = ShopCatalog.find(id)
        ?.takeIf { it.slot == CosmeticSlot.EFFECT }
        ?.id ?: ShopCatalog.DEFAULT_EFFECT
}
