package com.mdk.cosmetic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Safe shear: cancels vanilla interaction before the mooshroom turns into a cow. */
public final class MooshroomFurShearing {
    private static final String NEXT = "mdkcosmetic.nextMooshroomFurShearing";
    private static final long COOLDOWN_MS = 5L * 60L * 1000L;
    private MooshroomFurShearing() { }

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof MushroomCow mooshroom)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack shears = event.getEntity().getMainHandItem();
        if (!shears.is(Items.SHEARS)) return;
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (mooshroom.level().isClientSide()) return;
        CompoundTag data = com.mdk.cosmetic.platform.PersistentData.of(mooshroom);
        long now = System.currentTimeMillis();
        long next = data.getLong(NEXT);
        if (next > now && next - now <= COOLDOWN_MS) {
            event.getEntity().displayClientMessage(Component.translatable(
                    "message.mdkcosmetic.mooshroom_regrowing", (next - now + 59999L) / 60000L), true);
            return;
        }
        data.putLong(NEXT, now + COOLDOWN_MS);
        com.mdk.cosmetic.platform.EntityDrops.spawn(mooshroom,new ItemStack(MDKCosmetic.MOOSHROOM_FUR.get()));
        mooshroom.playSound(SoundEvents.SHEEP_SHEAR, 0.65F, 1.05F);
        if (!event.getEntity().getAbilities().instabuild) {
            shears.hurtAndBreak(1, event.getEntity(), EquipmentSlot.MAINHAND);
        }
    }

    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof MushroomCow mooshroom) || mooshroom.level().isClientSide()) return;
        if (mooshroom.getRandom().nextFloat() < 0.10F) {
            event.getDrops().add(new ItemEntity(mooshroom.level(),
                    mooshroom.getX(), mooshroom.getY() + 0.15D, mooshroom.getZ(),
                    new ItemStack(MDKCosmetic.MOOSHROOM_FUR.get())));
        }
    }
}
