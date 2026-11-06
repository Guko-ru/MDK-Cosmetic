package com.mdk.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import com.mdk.cosmetic.platform.Events.EntityTickEvent;

/** Invites glow squids to swim toward players with the glow-squid hat, without beaching them. */
public final class GlowSquidHatAttraction {
    private static final double RANGE_SQ = 16.0D * 16.0D;
    private GlowSquidHatAttraction() { }

    public static void onGlowSquidTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Squid squid) || squid.getType() != EntityType.GLOW_SQUID) return;
        if (squid.level().isClientSide() || !squid.isAlive() || squid.isNoAi() || !squid.isInWater()) return;
        if (squid.tickCount % 3 != 0) return;
        LivingEntity nearest = HatWearerFinder.closest(squid, MDKCosmetic.GLOW_SQUID_HAT.get(), RANGE_SQ);
        double nearestDist = nearest == null ? Double.POSITIVE_INFINITY : nearest.distanceToSqr(squid);
        if (nearest == null || nearestDist < 3.0D * 3.0D) return;
        Vec3 goal = findWaterDestination(squid, nearest);
        if (goal == null) return;
        Vec3 delta = goal.subtract(squid.position());
        double magnitude = delta.length();
        if (magnitude < 0.3D) return;
        Vec3 step = delta.scale(1.0D / magnitude);
        BlockPos next = BlockPos.containing(squid.position().add(step.scale(0.9D)));
        if (!water(squid, next) || !water(squid, next.above())) return;
        float speed = 0.18F;
        squid.setMovementVector((float) step.x * speed,
                (float) step.y * speed, (float) step.z * speed);
    }

    private static Vec3 findWaterDestination(Squid squid, LivingEntity p) {
        BlockPos center = p.blockPosition();
        BlockPos best = null;
        double bestToPlayer = Double.POSITIVE_INFINITY;
        double bestToSquid = Double.POSITIVE_INFINITY;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                for (int dy = -3; dy <= 1; dy++) {
                    BlockPos b = center.offset(dx, dy, dz);
                    if (!water(squid, b) || !water(squid, b.above())) continue;
                    double ds = b.distToCenterSqr(squid.position());
                    if (ds > RANGE_SQ) continue;
                    double dp = b.distToCenterSqr(p.position());
                    if (dp < bestToPlayer - 0.01D ||
                            (Math.abs(dp - bestToPlayer) < 0.01D && ds < bestToSquid)) {
                        best = b; bestToPlayer = dp; bestToSquid = ds;
                    }
                }
            }
        }
        return best == null ? null : Vec3.atCenterOf(best);
    }
    private static boolean water(Squid s, BlockPos b) {
        return s.level().getFluidState(b).is(FluidTags.WATER)
                && s.level().getBlockState(b).getCollisionShape(s.level(), b).isEmpty();
    }
}
