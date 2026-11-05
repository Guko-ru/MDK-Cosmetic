package com.mdk.cosmetic;

import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/** Pure selection rules for multiplayer Uno battles. No dependence on world state or AI goals. */
public final class UnoTargetingRules {
    private UnoTargetingRules() { }
    /**
     * @param candidates players in Uno's current dimension
     * @param distanceSquared squared 3D distance from Uno
     * @param health current HP (ignoring absorption). Two hearts equals 4 HP.
     * @param eligible living players in survival or adventure mode
     * @param maximumDistanceSquared 50 * 50 by default
     * @return closest player with < 4 HP if present, otherwise closest eligible player;
     *         null when none is in range.
     */
    public static <T> T select(Iterable<T> candidates,
            ToDoubleFunction<T> distanceSquared,
            ToDoubleFunction<T> health,
            Predicate<T> eligible,
            double maximumDistanceSquared) {
        T nearest = null;
        T nearestWeak = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        double bestWeakDistance = Double.POSITIVE_INFINITY;
        for (T candidate : candidates) {
            if (!eligible.test(candidate)) continue;
            double distance = distanceSquared.applyAsDouble(candidate);
            if (!Double.isFinite(distance) || distance < 0.0D || distance > maximumDistanceSquared) continue;
            if (distance < bestDistance) {
                nearest = candidate;
                bestDistance = distance;
            }
            if (health.applyAsDouble(candidate) < 4.0D && distance < bestWeakDistance) {
                nearestWeak = candidate;
                bestWeakDistance = distance;
            }
        }
        return nearestWeak != null ? nearestWeak : nearest;
    }
}
