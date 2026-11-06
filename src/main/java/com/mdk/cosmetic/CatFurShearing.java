package com.mdk.cosmetic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.mdk.cosmetic.platform.Events.LivingDropsEvent;
import com.mdk.cosmetic.platform.Events.PlayerInteractEvent;

/** Cat and ocelot share a single fur ingredient and each individual mob has its own timer. */
public final class CatFurShearing {
    private static final String NEXT_SHEAR_AT = "mdkcosmetic.nextCatFurShear";
    private static final long COOLDOWN_MILLIS = 5L * 60L * 1000L;
    private CatFurShearing() { }

    private static boolean isFeline(LivingEntity entity) {
        return entity instanceof Cat || entity instanceof Ocelot;
    }

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof LivingEntity animal) || !isFeline(animal)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack shears = event.getEntity().getMainHandItem();
        if (!shears.is(Items.SHEARS)) return;
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (animal.level().isClientSide()) return;

        CompoundTag data = com.mdk.cosmetic.platform.PersistentData.of(animal);
        long now = System.currentTimeMillis();
        long next = data.getLong(NEXT_SHEAR_AT);
        if (next > now && next - now <= COOLDOWN_MILLIS) {
            long seconds = (next - now + 999L) / 1000L;
            event.getEntity().displayClientMessage(Component.translatable(
                    "message.mdkcosmetic.cat_regrowing", (seconds + 59L) / 60L), true);
            return;
        }
        data.putLong(NEXT_SHEAR_AT, now + COOLDOWN_MILLIS);
        animal.spawnAtLocation(new ItemStack(MDKCosmetic.CAT_FUR.get()));
        animal.playSound(SoundEvents.SHEEP_SHEAR, 0.85F, 1.2F);
        if (!event.getEntity().getAbilities().instabuild) {
            shears.hurtAndBreak(1, event.getEntity(), EquipmentSlot.MAINHAND);
        }
    }

    /** Independent 10% drop chance for either a cat or an ocelot. */
    public static void onCatDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof LivingEntity animal) || !isFeline(animal)) return;
        if (animal.level().isClientSide()) return;
        if (animal.getRandom().nextFloat() < 0.10F) {
            event.getDrops().add(new ItemEntity(animal.level(), animal.getX(), animal.getY() + 0.15,
                    animal.getZ(), new ItemStack(MDKCosmetic.CAT_FUR.get())));
        }
    }
}
