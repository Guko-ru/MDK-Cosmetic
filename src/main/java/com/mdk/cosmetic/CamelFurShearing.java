package com.mdk.cosmetic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Humane camel-fur collection; original vanilla mob loot is untouched. */
public final class CamelFurShearing {
    private static final String NEXT = "mdkcosmetic.nextCamelFurShearing";
    private static final long COOLDOWN_MS = 5L * 60L * 1000L;
    private CamelFurShearing() { }

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Camel camel)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack shears = event.getEntity().getMainHandItem();
        if (!shears.is(Items.SHEARS)) return;
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (camel.level().isClientSide()) return;

        CompoundTag data = com.mdk.cosmetic.platform.PersistentData.of(camel);
        long now = System.currentTimeMillis();
        long next = data.getLongOr(NEXT,0L);
        if (next > now && next - now <= COOLDOWN_MS) {
            event.getEntity().sendOverlayMessage(Component.translatable(
                    "message.mdkcosmetic.camel_regrowing", (next - now + 59999L) / 60000L));
            return;
        }
        data.putLong(NEXT, now + COOLDOWN_MS);
        com.mdk.cosmetic.platform.EntityDrops.spawn(camel,new ItemStack(MDKCosmetic.CAMEL_FUR.get()));
        camel.playSound(SoundEvents.SHEEP_SHEAR, 0.7F, 0.9F);
        if (!event.getEntity().getAbilities().instabuild) {
            shears.hurtAndBreak(1, event.getEntity(), EquipmentSlot.MAINHAND);
        }
    }

    public static void onCamelDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Camel camel) || camel.level().isClientSide()) return;
        if (camel.getRandom().nextFloat() < 0.10F) {
            event.getDrops().add(new ItemEntity(camel.level(), camel.getX(), camel.getY() + 0.15D,
                    camel.getZ(), new ItemStack(MDKCosmetic.CAMEL_FUR.get())));
        }
    }
}
