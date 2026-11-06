package com.mdk.cosmetic;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.LivingEntity;
import com.mdk.cosmetic.platform.Events.EntityTickEvent;

/** Water-bound, non-teleporting lure. Never sets an on-land movement destination. */
public final class AxolotlHatAttraction {
    private static final double RANGE_SQ = 16D*16D;
    private static final int[][] DELTAS = {{1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{0,1,0},{0,-1,0}};
    private static final Map<Axolotl,BlockPos> WAYPOINTS = new WeakHashMap<>();
    private AxolotlHatAttraction() { }

    public static void onAxolotlTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Axolotl a)) return;
        if (a.level().isClientSide() || !a.isAlive() || a.isNoAi()) return;
        // Never interfere with the axolotl's natural return-to-water behavior.
        if (!a.isInWater()) { WAYPOINTS.remove(a); return; }
        LivingEntity player = HatWearerFinder.closest(a, MDKCosmetic.AXOLOTL_HAT.get(), RANGE_SQ);
        double distance = player == null ? Double.POSITIVE_INFINITY : player.distanceToSqr(a);
        if (player == null) { WAYPOINTS.remove(a); return; }
        // Stop following when close to the person, especially at water's edge.
        if (distance < 3.0D*3.0D) { WAYPOINTS.remove(a); return; }
        final BlockPos start = a.blockPosition();
        if (!water(a,start)) { WAYPOINTS.remove(a); return; }
        BlockPos cached = WAYPOINTS.get(a);
        if (cached != null && water(a,cached) && a.tickCount % 12 != 0
                && cached.distToCenterSqr(a.position()) > 0.6D) {
            a.getNavigation().stop();
            a.getMoveControl().setWantedPosition(cached.getX()+0.5D,
                    cached.getY()+0.5D,cached.getZ()+0.5D,1.1D);
            return;
        }
        if (a.tickCount % 12 != 0 && cached == null) return;

        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Map<BlockPos,BlockPos> previous = new HashMap<>();
        queue.add(start); previous.put(start,start);
        BlockPos target = start;
        double best = start.distToCenterSqr(player.position());
        int visited=0;
        while (!queue.isEmpty() && visited++ < 1400) {
            BlockPos at=queue.removeFirst();
            double score = at.distToCenterSqr(player.position());
            if (score + 0.01D < best) {best=score;target=at;}
            for (int[] d : DELTAS) {
                BlockPos next = at.offset(d[0],d[1],d[2]);
                if (previous.containsKey(next) || !next.closerThan(start,16D)) continue;
                if (!water(a,next)) continue;
                previous.put(next,at); queue.addLast(next);
            }
        }
        if (target.equals(start)) { WAYPOINTS.remove(a); return; }
        // Recover the next strictly-water cell on the route, not a dry shortcut.
        BlockPos waypoint = target;
        while (!previous.get(waypoint).equals(start)) waypoint=previous.get(waypoint);
        if (!water(a,waypoint)) { WAYPOINTS.remove(a); return; }
        WAYPOINTS.put(a,waypoint);
        // Direct aquatic steering avoids amphibious path-finder choosing a dry shore shortcut.
        a.getNavigation().stop();
        a.getMoveControl().setWantedPosition(waypoint.getX()+0.5D,
                waypoint.getY()+0.5D,waypoint.getZ()+0.5D,1.1D);
    }
    private static boolean water(Axolotl a,BlockPos pos) {
        return a.level().getFluidState(pos).is(FluidTags.WATER)
                && a.level().getBlockState(pos).getCollisionShape(a.level(),pos).isEmpty();
    }
}
