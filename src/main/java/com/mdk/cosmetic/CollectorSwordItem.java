package com.mdk.cosmetic;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/** Collector's enchantment is intrinsic and restored if the sword is disenchanted. */
public final class CollectorSwordItem extends SwordItem {
    public CollectorSwordItem(ToolMaterial tier, Properties properties) {super(tier, 3, -2.4F, properties);}
    @Override public boolean isFoil(ItemStack stack) {return true;}
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack,level,entity,slot,selected);
        if (level.isClientSide() || !(entity instanceof Player)) return;
        Holder<Enchantment> collector=level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(CollectorEssenceDrops.COLLECTOR);
        if (EnchantmentHelper.getItemEnchantmentLevel(collector,stack)==0) stack.enchant(collector,1);
    }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack=player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResult.FAIL;
        if (!level.isClientSide() && player instanceof ServerPlayer server) {
            CollectorSwordWave.start(server);
            player.getCooldowns().addCooldown(stack,1200); // 60 seconds
        }
        return InteractionResult.SUCCESS;
    }
}
