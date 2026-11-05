package com.mdk.cosmetic;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/** Small bounded AABB lookup, not a world or chunk scan. */
public final class HatWearerFinder {
    private HatWearerFinder() { }

    public static boolean eligible(Mob seeker, LivingEntity candidate, Item hat, double rangeSq) {
        return candidate != seeker && candidate.isAlive() && candidate.level() == seeker.level()
                && (candidate instanceof Monster || (candidate instanceof Player player && !player.isSpectator()))
                && candidate.getItemBySlot(EquipmentSlot.HEAD).is(hat)
                && seeker.distanceToSqr(candidate) <= rangeSq
                && seeker.getSensing().hasLineOfSight(candidate);
    }

    public static LivingEntity closest(Mob seeker, Item hat, double rangeSq) {
        double best = rangeSq;
        LivingEntity target = null;
        for (Player player : seeker.level().players()) {
            if (!eligible(seeker,player,hat,best)) continue;
            best = seeker.distanceToSqr(player); target=player;
        }
        for (Monster enemy : seeker.level().getEntitiesOfClass(Monster.class,
                seeker.getBoundingBox().inflate(Math.sqrt(rangeSq)),
                m -> m.isAlive() && m.getItemBySlot(EquipmentSlot.HEAD).is(hat))) {
            if (!eligible(seeker,enemy,hat,best)) continue;
            best = seeker.distanceToSqr(enemy); target=enemy;
        }
        return target;
    }
}
