package com.mdk.cosmetic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.mdk.cosmetic.platform.Events.LivingDropsEvent;
import com.mdk.cosmetic.platform.Events.PlayerInteractEvent;

/** Humane wolf-fur collection; original vanilla mob loot is untouched. */
public final class WolfFurShearing {
    private static final String NEXT = "mdkcosmetic.nextWolfFurShearing";
    private static final long COOLDOWN_MS = 5L * 60L * 1000L;
    private WolfFurShearing() { }

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Wolf wolf)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack shears = event.getEntity().getMainHandItem();
        if (!shears.is(Items.SHEARS)) return;
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (wolf.level().isClientSide()) return;

        CompoundTag data = com.mdk.cosmetic.platform.PersistentData.of(wolf);
        long now = System.currentTimeMillis();
        long next = data.getLongOr(NEXT,0L);
        if (next > now && next - now <= COOLDOWN_MS) {
            event.getEntity().displayClientMessage(Component.translatable(
                    "message.mdkcosmetic.wolf_regrowing", (next - now + 59999L) / 60000L), true);
            return;
        }
        data.putLong(NEXT, now + COOLDOWN_MS);
        com.mdk.cosmetic.platform.EntityDrops.spawn(wolf,new ItemStack(MDKCosmetic.WOLF_FUR.get()));
        wolf.playSound(SoundEvents.SHEEP_SHEAR, 0.7F, 0.9F);
        if (!event.getEntity().getAbilities().instabuild) {
            shears.hurtAndBreak(1, event.getEntity(), EquipmentSlot.MAINHAND);
        }
    }

    public static void onWolfDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf) || wolf.level().isClientSide()) return;
        if (wolf.getRandom().nextFloat() < 0.10F) {
            event.getDrops().add(new ItemEntity(wolf.level(), wolf.getX(), wolf.getY() + 0.15D,
                    wolf.getZ(), new ItemStack(MDKCosmetic.WOLF_FUR.get())));
        }
    }
}
