package com.mdk.cosmetic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Separate crafting material for the glow-squid hat, independent from vanilla glow ink sacs. */
public final class GlowSquidInkHarvest {
    private static final String NEXT = "mdkcosmetic.nextGlowSquidInkHarvest";
    private static final long COOLDOWN = 5L * 60L * 1000L;
    private GlowSquidInkHarvest() { }

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Squid squid) || squid.getType() != EntityType.GLOW_SQUID) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack tool = event.getEntity().getMainHandItem();
        if (!tool.is(Items.SHEARS)) return;
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (squid.level().isClientSide()) return;
        CompoundTag data = com.mdk.cosmetic.platform.PersistentData.of(squid);
        long now = System.currentTimeMillis();
        long until = data.getLongOr(NEXT,0L);
        if (until > now && until - now <= COOLDOWN) {
            event.getEntity().displayClientMessage(Component.translatable(
                    "message.mdkcosmetic.glow_squid_refilling", (until - now + 59999L) / 60000L), true);
            return;
        }
        data.putLong(NEXT, now + COOLDOWN);
        com.mdk.cosmetic.platform.EntityDrops.spawn(squid,new ItemStack(MDKCosmetic.GLOW_SQUID_INK_CLOT.get()));
        squid.playSound(SoundEvents.SHEEP_SHEAR, 0.65F, 1.05F);
        if (!event.getEntity().getAbilities().instabuild) {
            tool.hurtAndBreak(1, event.getEntity(), EquipmentSlot.MAINHAND);
        }
    }
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Squid squid) || squid.getType() != EntityType.GLOW_SQUID
                || squid.level().isClientSide()) return;
        if (squid.getRandom().nextFloat() < 0.10F) {
            event.getDrops().add(new ItemEntity(squid.level(), squid.getX(), squid.getY() + 0.15D,
                    squid.getZ(), new ItemStack(MDKCosmetic.GLOW_SQUID_INK_CLOT.get())));
        }
    }
}
