package com.mdk.cosmetic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Humane cow-fur collection for the Cow Hat content. */
public final class CowFurShearing {
    private static final String NEXT = "mdkcosmetic.nextCowFurShearing";
    private static final long COOLDOWN_MS = 5L * 60L * 1000L;
    private CowFurShearing() { }

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Cow cow) || cow instanceof MushroomCow) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack shears = event.getEntity().getMainHandItem();
        if (!shears.is(Items.SHEARS)) return;
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (cow.level().isClientSide()) return;

        CompoundTag data = com.mdk.cosmetic.platform.PersistentData.of(cow);
        long now = System.currentTimeMillis();
        long next = data.getLong(NEXT);
        if (next > now && next - now <= COOLDOWN_MS) {
            event.getEntity().displayClientMessage(Component.translatable(
                    "message.mdkcosmetic.cow_regrowing", (next - now + 59999L) / 60000L), true);
            return;
        }
        data.putLong(NEXT, now + COOLDOWN_MS);
        com.mdk.cosmetic.platform.EntityDrops.spawn(cow,new ItemStack(MDKCosmetic.COW_FUR.get()));
        cow.playSound(SoundEvents.SHEEP_SHEAR, 0.7F, 0.95F);
        if (!event.getEntity().getAbilities().instabuild) {
            shears.hurtAndBreak(1, event.getEntity(), EquipmentSlot.MAINHAND);
        }
    }

    public static void onCowDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Cow cow) || cow instanceof MushroomCow || cow.level().isClientSide()) return;
        if (cow.getRandom().nextFloat() < 0.10F) {
            event.getDrops().add(new ItemEntity(cow.level(), cow.getX(), cow.getY() + 0.15D,
                    cow.getZ(), new ItemStack(MDKCosmetic.COW_FUR.get())));
        }
    }
}
