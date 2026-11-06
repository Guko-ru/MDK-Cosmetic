package com.mdk.cosmetic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.mdk.cosmetic.platform.Events.LivingDropsEvent;
import com.mdk.cosmetic.platform.Events.PlayerInteractEvent;

/** Humane five-minute collection of horse fur; retains vanilla drops. */
public final class HorseFurShearing {
    private static final String NEXT = "mdkcosmetic.nextHorseFurShearing";
    private static final long COOLDOWN_MS = 300000L;
    private HorseFurShearing() { }

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Horse animal)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack shears = event.getEntity().getMainHandItem();
        if (!shears.is(Items.SHEARS)) return;
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (animal.level().isClientSide()) return;
        CompoundTag data = com.mdk.cosmetic.platform.PersistentData.of(animal);
        long now = System.currentTimeMillis();
        long next = data.getLong(NEXT);
        if (next > now && next - now <= COOLDOWN_MS) {
            event.getEntity().displayClientMessage(Component.translatable(
                "message.mdkcosmetic.horse_regrowing", (next - now + 59999L) / 60000L), true);
            return;
        }
        data.putLong(NEXT, now + COOLDOWN_MS);
        animal.spawnAtLocation(new ItemStack(MDKCosmetic.HORSE_FUR.get()));
        animal.playSound(SoundEvents.SHEEP_SHEAR, 0.65F, 1.00F);
        if (!event.getEntity().getAbilities().instabuild) {
            shears.hurtAndBreak(1, event.getEntity(), EquipmentSlot.MAINHAND);
        }
    }

    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Horse animal) || animal.level().isClientSide()) return;
        if (animal.getRandom().nextFloat() < 0.10F) {
            event.getDrops().add(new ItemEntity(animal.level(), animal.getX(), animal.getY()+0.15D,
                    animal.getZ(), new ItemStack(MDKCosmetic.HORSE_FUR.get())));
        }
    }
}
