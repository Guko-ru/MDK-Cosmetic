package com.mdk.cosmetic;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Collector ores require the special pickaxe. Silk Touch preserves the ore block; otherwise they drop shards. */
public final class CollectorOres {
    private CollectorOres(){}
    public static boolean isOre(BlockState state) {
        return state.is(MDKCosmetic.COLLECTOR_ORE_STONE.get()) || state.is(MDKCosmetic.COLLECTOR_ORE_DEEPSLATE.get())
            || state.is(MDKCosmetic.COLLECTOR_ORE_NETHERRACK.get()) || state.is(MDKCosmetic.COLLECTOR_ORE_END_STONE.get());
    }
    public static void onDrops(BlockDropsEvent event) {
        if (!isOre(event.getState()) || !(event.getBreaker() instanceof Player player)) return;
        if (!event.getTool().is(MDKCosmetic.COLLECTOR_PICKAXE.get())) return;

        Holder<Enchantment> silkTouch = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.SILK_TOUCH);
        if (EnchantmentHelper.getItemEnchantmentLevel(silkTouch, event.getTool()) > 0) {
            event.getDrops().clear();
            event.getDrops().add(new ItemEntity(player.level(),
                    event.getPos().getX() + 0.5D,
                    event.getPos().getY() + 0.5D,
                    event.getPos().getZ() + 0.5D,
                    new ItemStack(event.getState().getBlock())));
            return;
        }

        if (!player.hasEffect(MDKCosmetic.PRODUCTIVITY_AURA.holder())) return;
        for (var entity : event.getDrops()) {
            if (entity.getItem().is(MDKCosmetic.POWER_ESSENCE.get()))
                entity.getItem().setCount(Math.min(64,entity.getItem().getCount()*2));
        }
    }
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if(isOre(event.getState()) && !event.getEntity().isCreative()
                && !event.getEntity().getMainHandItem().is(MDKCosmetic.COLLECTOR_PICKAXE.get())) event.setNewSpeed(0);
    }
    public static void onBreak(net.neoforged.neoforge.event.level.block.BreakBlockEvent event) {
        if(isOre(event.getState()) && !event.getPlayer().isCreative()
                && !event.getPlayer().getMainHandItem().is(MDKCosmetic.COLLECTOR_PICKAXE.get())) event.setCanceled(true);
    }
}
