package com.mdk.cosmetic;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.mdk.cosmetic.platform.Events.LivingDeathEvent;

/** Server-authoritative totem consumption, including remote Collector Memory. */
public final class CollectorBackpackTotem {
    private CollectorBackpackTotem() {}
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.isCanceled()) return;
        for (int inv = 0; inv <= 36; inv++) {
            boolean equipped = inv == 36;
            ItemStack backpack = equipped ? CollectorEquipmentData.backpack(player)
                    : player.getInventory().getItem(inv);
            if (!(backpack.getItem() instanceof CollectorBackpackItem)) continue;
            if (!CollectorBackpackData.hasUpgradeItem(backpack, MDKCosmetic.COLLECTOR_BACKPACK_TOTEM.get(), player.registryAccess())) continue;
            if (CollectorBackpackData.read(backpack).getBoolean("TotemDisabled")) continue;
            boolean consumed = false;
            if (CollectorBackpackData.linked(backpack)) {
                CollectorMemoryCoreBlockEntity memory = CollectorBackpackData.remoteCore(backpack, player.getServer());
                if (memory == null) continue;
                for (int i = 0; i < memory.types(); i++) {
                    if (!memory.view(i).is(Items.TOTEM_OF_UNDYING)) continue;
                    consumed = !memory.withdraw(i, 1).isEmpty();
                    break;
                }
            } else {
                List<ItemStack> items = CollectorBackpackData.items(backpack, player.registryAccess());
                for (int i = 0; i < CollectorBackpackData.capacity(backpack); i++) {
                    ItemStack stack = items.get(i);
                    if (!stack.is(Items.TOTEM_OF_UNDYING)) continue;
                    stack.shrink(1);
                    if (stack.isEmpty()) items.set(i, ItemStack.EMPTY);
                    CollectorBackpackData.saveItems(backpack, items, player.registryAccess());
                    if (equipped) CollectorEquipmentData.setBackpack(player, backpack);
                    consumed = true;
                    break;
                }
            }
            if (!consumed) continue;
            event.setCanceled(true);
            player.setHealth(1.0F);
            player.removeAllEffects();
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
            player.level().broadcastEntityEvent(player, (byte) 35);
            return;
        }
    }
}
