package com.mdk.cosmetic;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.ElderGuardian;
import com.mdk.cosmetic.platform.Events.EntityTickEvent;

/** A Heart is generated once per ocean monument, using an elder guardian as a reliable trigger. */
public final class CollectorHeartWorldEvents {
    private CollectorHeartWorldEvents() {}

    public static void onHurt(com.mdk.cosmetic.platform.Events.LivingDamageEvent.Pre event) {
        if (event.getSource().getEntity() instanceof CollectorHeartEntity heart && heart.isDecoy()) {
            event.setNewDamage(event.getNewDamage() * 0.5F);
        }
    }

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ElderGuardian elder)
                || !(elder.level() instanceof ServerLevel level) || elder.tickCount % 100 != 0) return;
        final String flag = "MDKCollectorHeartSpawnChecked";
        if (com.mdk.cosmetic.platform.PersistentData.of(elder).getBoolean(flag)) return;
        CollectorHeartSpawnData saved = CollectorHeartSpawnData.get(level);
        if (saved.registered(elder.blockPosition())) {
            com.mdk.cosmetic.platform.PersistentData.of(elder).putBoolean(flag, true);
            return;
        }
        if (!level.getEntitiesOfClass(CollectorHeartEntity.class, elder.getBoundingBox().inflate(100),
                h -> !h.isDecoy()).isEmpty()) {
            saved.mark(elder.blockPosition());
            com.mdk.cosmetic.platform.PersistentData.of(elder).putBoolean(flag, true);
            return;
        }
        CollectorHeartEntity heart = MDKCosmetic.COLLECTOR_HEART_ENTITY.get().create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (heart == null) return;
        heart.moveTo(elder.getX(), elder.getY(), elder.getZ(), 0, 0);
        if (!level.addFreshEntity(heart)) return;
        saved.mark(elder.blockPosition());
        com.mdk.cosmetic.platform.PersistentData.of(elder).putBoolean(flag, true);
    }
}
