package com.mdk.cosmetic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/** Humane 100% fur collection once per rabbit per five real minutes; optional 10% death drop. */
public final class RabbitFurShearing {
    private static final String NEXT_SHEAR_AT = "mdkcosmetic.nextRabbitFurShear";
    private static final long COOLDOWN_MILLIS = 5L * 60L * 1000L;
    private RabbitFurShearing() { }

    public static boolean onInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        boolean[] mdk$cancelled={false};
        mdk$handler: {

        if (!(event.getTarget() instanceof Rabbit rabbit)) break mdk$handler;
        if (event.getHand() != InteractionHand.MAIN_HAND) break mdk$handler;
        ItemStack shears = event.getEntity().getMainHandItem();
        if (!shears.is(Items.SHEARS)) break mdk$handler;
        event.setCancellationResult(InteractionResult.SUCCESS);
        mdk$cancelled[0]=(true);
        if (rabbit.level().isClientSide()) break mdk$handler;

        CompoundTag data = com.mdk.cosmetic.platform.PersistentData.of(rabbit);
        long now = System.currentTimeMillis();
        long next = data.getLongOr(NEXT_SHEAR_AT,0L);
        if (next > now && next - now <= COOLDOWN_MILLIS) {
            long remainingMinutes = (next - now + 59999L) / 60000L;
            event.getEntity().sendOverlayMessage(Component.translatable(
                    "message.mdkcosmetic.rabbit_regrowing", remainingMinutes));
            break mdk$handler;
        }
        data.putLong(NEXT_SHEAR_AT, now + COOLDOWN_MILLIS);
        com.mdk.cosmetic.platform.EntityDrops.spawn(rabbit,new ItemStack(MDKCosmetic.RABBIT_FUR.get()));
        rabbit.playSound(SoundEvents.SHEEP_SHEAR, 0.75F, 1.45F);
        if (!event.getEntity().getAbilities().instabuild)
            shears.hurtAndBreak(1, event.getEntity(), EquipmentSlot.MAINHAND);
    
        }
        return mdk$cancelled[0];
    }

    public static void onRabbitDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Rabbit rabbit) || rabbit.level().isClientSide()) return;
        if (rabbit.getRandom().nextFloat() < 0.10F)
            event.getDrops().add(new ItemEntity(rabbit.level(), rabbit.getX(),
                    rabbit.getY() + 0.15D, rabbit.getZ(),
                    new ItemStack(MDKCosmetic.RABBIT_FUR.get())));
    }
}
