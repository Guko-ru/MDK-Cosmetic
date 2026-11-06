package com.mdk.cosmetic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/** MDK Cosmetic-only magical fibers; not a canonical vanilla Allay drop. */
public final class AllayFiberHarvest {
    private static final String NEXT_SHEAR_AT = "mdkcosmetic.nextAllayFiberHarvest";
    private static final long COOLDOWN_MILLIS = 5L * 60L * 1000L;

    private AllayFiberHarvest() {}

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Allay allay)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack shears = event.getEntity().getMainHandItem();
        if (!shears.is(Items.SHEARS)) return;
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (allay.level().isClientSide()) return;
        CompoundTag data = com.mdk.cosmetic.platform.PersistentData.of(allay);
        long now = System.currentTimeMillis();
        long next = data.getLongOr(NEXT_SHEAR_AT,0L);
        if (next > now && next - now <= COOLDOWN_MILLIS) {
            long mins = ((next - now + 999L) / 1000L + 59L) / 60L;
            event.getEntity().displayClientMessage(
                Component.translatable("message.mdkcosmetic.allay_regrowing", mins), true);
            return;
        }
        data.putLong(NEXT_SHEAR_AT, now + COOLDOWN_MILLIS);
        com.mdk.cosmetic.platform.EntityDrops.spawn(allay,new ItemStack(MDKCosmetic.ALLAY_FIBERS.get()));
        allay.playSound(SoundEvents.SHEEP_SHEAR, 0.70F, 1.65F);
        if (!event.getEntity().getAbilities().instabuild) {
            shears.hurtAndBreak(1, event.getEntity(), EquipmentSlot.MAINHAND);
        }
    }

    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Allay allay) || allay.level().isClientSide()) return;
        if (allay.getRandom().nextFloat() < 0.10F) {
            event.getDrops().add(new ItemEntity(allay.level(), allay.getX(), allay.getY() + 0.15,
                allay.getZ(), new ItemStack(MDKCosmetic.ALLAY_FIBERS.get())));
        }
    }
}
