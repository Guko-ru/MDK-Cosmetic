package com.mdk.cosmetic;

import java.util.Map;
import net.minecraft.world.entity.LivingEntity;
import java.util.WeakHashMap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.animal.allay.Allay;

import com.mdk.cosmetic.platform.Events.EntityTickEvent;

/**
 * Allays use a Brain (not just the vanilla goal selector) for flying navigation.
 * Give nearby allays a temporary WALK_TARGET while the matching helmet is worn.
 * No teleportation, taming or permanent player ownership is involved.
 */
public final class AllayHatAttraction {
    private static final double ACQUIRE_RANGE_SQ = 16.0D * 16.0D;
    private static final double KEEP_RANGE_SQ = 18.0D * 18.0D;
    private static final double STOP_RANGE_SQ = 3.0D * 3.0D;
    private static final Map<Allay, LivingEntity> FOLLOWERS = new WeakHashMap<>();

    private AllayHatAttraction() { }

    public static void onAllayTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Allay allay)) return;
        if (allay.level().isClientSide() || !allay.isAlive() || allay.isNoAi()) return;

        // Never update a whole level of mobs: handle only the current ticked Allay.
        LivingEntity following = FOLLOWERS.get(allay);
        if (following == null || !HatWearerFinder.eligible(allay,following,MDKCosmetic.ALLAY_HAT.get(),KEEP_RANGE_SQ)) {
            following = allay.tickCount % 10 == 0
                    ? HatWearerFinder.closest(allay,MDKCosmetic.ALLAY_HAT.get(),ACQUIRE_RANGE_SQ)
                    : null;
        }

        if (following == null) {
            if (FOLLOWERS.remove(allay) != null) {
                // Remove the memory this ability was controlling; normal Allay AI resumes.
                allay.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
                allay.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
                allay.getNavigation().stop();
            }
            return;
        }

        FOLLOWERS.put(allay, following);
        allay.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(following, true));

        if (allay.distanceToSqr(following) <= STOP_RANGE_SQ) {
            allay.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            allay.getNavigation().stop();
            return;
        }

        // Flying Allay should approach the player's upper body rather than the ground.
        // Reissue after Brain ticks to win over idle wandering targets.
        allay.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                new WalkTarget(following.position().add(0.0D, 1.05D, 0.0D), 1.20F, 2));
    }

}
