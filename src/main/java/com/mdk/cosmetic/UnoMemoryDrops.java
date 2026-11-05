package com.mdk.cosmetic;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/** One guaranteed Collector's Heart key after the Heart's final death, never on totem revival. */
public final class UnoMemoryDrops {
    private UnoMemoryDrops() {}
    public static void onDrops(LivingDropsEvent event) {
        if(!(event.getEntity() instanceof UnoEntity uno)||uno.level().isClientSide())return;
        event.getDrops().add(new ItemEntity(uno.level(),uno.getX(),uno.getY()+0.3,uno.getZ(),
                new ItemStack(MDKCosmetic.COLLECTOR_BACKPACK_LINK_KEY.get(),1)));
    }
}
