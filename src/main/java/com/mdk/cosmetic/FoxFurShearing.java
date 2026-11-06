package com.mdk.cosmetic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import com.mdk.cosmetic.platform.Events.PlayerInteractEvent;
import com.mdk.cosmetic.platform.Events.LivingDropsEvent;

/** One fur per peaceful shearing, once every five real-time minutes per fox. */
public final class FoxFurShearing {
    private static final String NEXT_SHEAR_AT = "mdkcosmetic.nextFoxFurShear";
    private static final long COOLDOWN_MILLIS = 5 * 60 * 1000L;
    private FoxFurShearing() {}

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Fox fox)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack shears = event.getEntity().getMainHandItem();
        if (!shears.is(Items.SHEARS)) return;
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (fox.level().isClientSide()) return;

        CompoundTag data = com.mdk.cosmetic.platform.PersistentData.of(fox);
        long now = System.currentTimeMillis();
        long next = data.getLong(NEXT_SHEAR_AT);
        if (next > now && next - now <= COOLDOWN_MILLIS) {
            long seconds = (next-now + 999L)/1000L;
            event.getEntity().displayClientMessage(Component.translatable(
                    "message.mdkcosmetic.fox_regrowing", (seconds+59)/60), true);
            return;
        }
        data.putLong(NEXT_SHEAR_AT, now + COOLDOWN_MILLIS);
        com.mdk.cosmetic.platform.EntityDrops.spawn(fox,new ItemStack(MDKCosmetic.FOX_FUR.get()));
        fox.playSound(SoundEvents.SHEEP_SHEAR, 0.85F, 1.25F);
        if (!event.getEntity().getAbilities().instabuild) {
            shears.hurtAndBreak(1, event.getEntity(), EquipmentSlot.MAINHAND);
        }
    }

    /** 10% independent chance to drop a tuft on any fox death. Does not drop a completed hat. */
    public static void onFoxDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Fox fox) || fox.level().isClientSide()) return;
        if (fox.getRandom().nextFloat() < 0.10F) {
            event.getDrops().add(new ItemEntity(fox.level(), fox.getX(), fox.getY()+0.15, fox.getZ(),
                    new ItemStack(MDKCosmetic.FOX_FUR.get())));
        }
    }
}
