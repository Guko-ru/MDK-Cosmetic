package com.mdk.cosmetic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/** Cosmetic fur harvesting. The bear is not transformed or harmed by collection. */
public final class PolarBearFurShearing {
    private static final String NEXT = "mdkcosmetic.nextPolarBearFurShearing";
    private static final long COOLDOWN_MS = 5L * 60L * 1000L;
    private PolarBearFurShearing() { }

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof PolarBear bear)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack shears = event.getEntity().getMainHandItem();
        if (!shears.is(Items.SHEARS)) return;
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (bear.level().isClientSide()) return;
        CompoundTag data = com.mdk.cosmetic.platform.PersistentData.of(bear);
        long now = System.currentTimeMillis();
        long next = data.getLongOr(NEXT,0L);
        if (next > now && next - now <= COOLDOWN_MS) {
            event.getEntity().displayClientMessage(Component.translatable(
                    "message.mdkcosmetic.polar_bear_regrowing", (next - now + 59999L) / 60000L), true);
            return;
        }
        data.putLong(NEXT, now + COOLDOWN_MS);
        com.mdk.cosmetic.platform.EntityDrops.spawn(bear,new ItemStack(MDKCosmetic.POLAR_BEAR_FUR.get()));
        bear.playSound(SoundEvents.SHEEP_SHEAR, 0.65F, 1.0F);
        if (!event.getEntity().getAbilities().instabuild) {
            shears.hurtAndBreak(1, event.getEntity(), EquipmentSlot.MAINHAND);
        }
    }

    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof PolarBear bear) || bear.level().isClientSide()) return;
        if (bear.getRandom().nextFloat() < 0.10F) {
            event.getDrops().add(new ItemEntity(bear.level(), bear.getX(), bear.getY()+0.15D,
                    bear.getZ(), new ItemStack(MDKCosmetic.POLAR_BEAR_FUR.get())));
        }
    }
}
