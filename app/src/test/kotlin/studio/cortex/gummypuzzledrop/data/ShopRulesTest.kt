package studio.cortex.gummypuzzledrop.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import studio.cortex.gummypuzzledrop.game.model.PowerTreat

class ShopRulesTest {
    @Test fun catalogIdsAreUniqueAndDefaultsExist() {
        assertEquals(ShopCatalog.items.size, ShopCatalog.items.map { it.id }.toSet().size)
        assertTrue(ShopCatalog.defaultOwned.all { ShopCatalog.find(it) != null })
    }

    @Test fun purchaseSubtractsPriceAndUnlocksItem() {
        val result = ShopRules.purchase(50, emptySet(), "mint_frost") as PurchaseResult.Purchased
        assertEquals(15, result.balance)
        assertTrue("mint_frost" in result.owned)
        assertTrue(ShopCatalog.defaultOwned.all { it in result.owned })
    }

    @Test fun purchaseNeverCreatesNegativeBalance() {
        assertEquals(PurchaseResult.NotEnoughStars, ShopRules.purchase(4, emptySet(), "heart_glow"))
        assertEquals(PurchaseResult.NotEnoughStars, ShopRules.purchase(-20, emptySet(), "mint_frost"))
    }

    @Test fun ownedItemCannotBeBoughtTwice() {
        assertEquals(
            PurchaseResult.AlreadyOwned,
            ShopRules.purchase(500, setOf("orange_pop"), "orange_pop"),
        )
    }

    @Test fun onlyOwnedKnownItemsCanBeEquipped() {
        assertEquals(
            EquipResult.NotOwned,
            ShopRules.equip(emptySet(), "aqua_dream", ShopCatalog.DEFAULT_BOARD, ShopCatalog.DEFAULT_EFFECT),
        )
        val equipped = ShopRules.equip(
            setOf("sparkle_effect"),
            "sparkle_effect",
            ShopCatalog.DEFAULT_BOARD,
            ShopCatalog.DEFAULT_EFFECT,
        ) as EquipResult.Equipped
        assertEquals("sparkle_effect", equipped.effect)
        assertEquals(ShopCatalog.DEFAULT_BOARD, equipped.board)
    }

    @Test fun runRewardIncludesMilestonesAndIsCapped() {
        assertEquals(3, ShopRules.rewardForRun(0, 0, false))
        assertEquals(33, ShopRules.rewardForRun(1_000, 1, true))
        assertEquals(45, ShopRules.rewardForRun(Int.MAX_VALUE, 99, true))
    }

    @Test fun powerTreatPurchaseAddsInventoryAndSubtractsStars() {
        val result = ShopRules.purchasePowerTreat(30, emptyMap(), PowerTreat.PINK_BOMB)
            as PowerPurchaseResult.Purchased
        assertEquals(12, result.balance)
        assertEquals(1, result.inventory[PowerTreat.PINK_BOMB])
        assertEquals(
            PowerPurchaseResult.NotEnoughStars,
            ShopRules.purchasePowerTreat(2, result.inventory, PowerTreat.SWEET_CLEANUP),
        )
    }

    @Test fun powerTreatConsumptionIsSafeAndNeverNegative() {
        assertEquals(null, ShopRules.consumePowerTreat(emptyMap(), PowerTreat.RAINBOW_POP))
        val after = ShopRules.consumePowerTreat(mapOf(PowerTreat.RAINBOW_POP to 2), PowerTreat.RAINBOW_POP)
        assertEquals(1, after?.get(PowerTreat.RAINBOW_POP))
    }
}
