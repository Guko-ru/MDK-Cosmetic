package com.mdk.cosmetic;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

/** Death insurance: all actually dropped items, including the backpack, are transferred atomically. */
public final class CollectorBackpackDeathVault {
    private CollectorBackpackDeathVault() {}

    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.isCanceled()) return;
        List<ItemEntity> drops = new ArrayList<>(event.getDrops());
        if (drops.isEmpty()) return; // keepInventory: nothing to transfer, never duplicate.
        CollectorMemoryCoreBlockEntity target = null;
        for (ItemEntity dropped : drops) {
            ItemStack bag = dropped.getItem();
            if (!(bag.getItem() instanceof CollectorBackpackItem) || !CollectorBackpackData.linked(bag)) continue;
            if (!CollectorBackpackData.hasUpgradeItem(bag, MDKCosmetic.COLLECTOR_BACKPACK_DEATH_VAULT.get(), player.registryAccess())) continue;
            if (!CollectorBackpackData.deathVaultEnabled(bag)) continue;
            target = CollectorBackpackData.remoteCore(bag, player.getServer());
            if (target != null) break;
        }
        if (target == null) return;
        List<ItemStack> payload = new ArrayList<>();
        for (ItemEntity entity : drops) if (!entity.getItem().isEmpty()) payload.add(entity.getItem().copy());
        if (payload.isEmpty()) return;
        // No item entities are removed unless the existing memory accepted the entire batch.
        if (!target.depositBatch(payload)) return;
        event.getDrops().removeAll(drops);
        CollectorAdvancementEvents.onMemoryInsurance(player);
    }
}
