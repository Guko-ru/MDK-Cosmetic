package com.mdk.cosmetic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.mdk.cosmetic.platform.Events.LivingDropsEvent;
import com.mdk.cosmetic.platform.Events.PlayerInteractEvent;

/** Peaceful panda-fur harvesting; the panda stays alive. */
public final class PandaFurShearing {
    private static final String NEXT_SHEAR_AT = "mdkcosmetic.nextPandaFurShear";
    private static final long COOLDOWN_MILLIS = 5L * 60L * 1000L;

    private PandaFurShearing() { }

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Panda panda)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack shears = event.getEntity().getMainHandItem();
        if (!shears.is(Items.SHEARS)) return;
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (panda.level().isClientSide()) return;

        CompoundTag data = com.mdk.cosmetic.platform.PersistentData.of(panda);
        long now = System.currentTimeMillis();
        long next = data.getLongOr(NEXT_SHEAR_AT,0L);
        if (next > now && next - now <= COOLDOWN_MILLIS) {
            long remainingSeconds = (next - now + 999L) / 1000L;
            event.getEntity().displayClientMessage(Component.translatable(
                    "message.mdkcosmetic.panda_regrowing", (remainingSeconds + 59L) / 60L), true);
            return;
        }

        data.putLong(NEXT_SHEAR_AT, now + COOLDOWN_MILLIS);
        com.mdk.cosmetic.platform.EntityDrops.spawn(panda,new ItemStack(MDKCosmetic.PANDA_FUR.get()));
        panda.playSound(SoundEvents.SHEEP_SHEAR, 0.85F, 1.10F);
        if (!event.getEntity().getAbilities().instabuild) {
            shears.hurtAndBreak(1, event.getEntity(), EquipmentSlot.MAINHAND);
        }
    }

    /** 10% independent chance to drop one tuft on panda death. */
    public static void onPandaDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Panda panda) || panda.level().isClientSide()) return;
        if (panda.getRandom().nextFloat() < 0.10F) {
            event.getDrops().add(new ItemEntity(panda.level(), panda.getX(), panda.getY() + 0.15,
                    panda.getZ(), new ItemStack(MDKCosmetic.PANDA_FUR.get())));
        }
    }
}
