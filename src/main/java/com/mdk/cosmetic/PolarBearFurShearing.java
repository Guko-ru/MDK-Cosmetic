package com.mdk.cosmetic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
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

    public static boolean onInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        boolean[] mdk$cancelled={false};
        mdk$handler: {

        if (!(event.getTarget() instanceof PolarBear bear)) break mdk$handler;
        if (event.getHand() != InteractionHand.MAIN_HAND) break mdk$handler;
        ItemStack shears = event.getEntity().getMainHandItem();
        if (!shears.is(Items.SHEARS)) break mdk$handler;
        event.setCancellationResult(InteractionResult.SUCCESS);
        mdk$cancelled[0]=(true);
        if (bear.level().isClientSide()) break mdk$handler;
        CompoundTag data = com.mdk.cosmetic.platform.PersistentData.of(bear);
        long now = System.currentTimeMillis();
        long next = data.getLongOr(NEXT,0L);
        if (next > now && next - now <= COOLDOWN_MS) {
            event.getEntity().sendOverlayMessage(Component.translatable(
                    "message.mdkcosmetic.polar_bear_regrowing", (next - now + 59999L) / 60000L));
            break mdk$handler;
        }
        data.putLong(NEXT, now + COOLDOWN_MS);
        com.mdk.cosmetic.platform.EntityDrops.spawn(bear,new ItemStack(MDKCosmetic.POLAR_BEAR_FUR.get()));
        bear.playSound(SoundEvents.SHEEP_SHEAR, 0.65F, 1.0F);
        if (!event.getEntity().getAbilities().instabuild) {
            shears.hurtAndBreak(1, event.getEntity(), EquipmentSlot.MAINHAND);
        }
    
        }
        return mdk$cancelled[0];
    }

    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof PolarBear bear) || bear.level().isClientSide()) return;
        if (bear.getRandom().nextFloat() < 0.10F) {
            event.getDrops().add(new ItemEntity(bear.level(), bear.getX(), bear.getY()+0.15D,
                    bear.getZ(), new ItemStack(MDKCosmetic.POLAR_BEAR_FUR.get())));
        }
    }
}
