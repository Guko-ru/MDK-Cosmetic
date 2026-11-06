package com.mdk.cosmetic;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;

/** Like vanilla temptation, now also for a hostile mob wearing the corresponding cosmetic hat. */
public final class HatAttractionGoal extends Goal {
    private static final double START_RANGE_SQ=256.0D, KEEP_RANGE_SQ=324.0D, STOP_SQ=6.76D;
    private final Mob animal;
    private final Item hat;
    private final double speed;
    private LivingEntity following;
    private int refresh;
    public HatAttractionGoal(Mob animal, Item hat, double speed) {
        this.animal=animal;this.hat=hat;this.speed=speed;
        setFlags(EnumSet.of(Goal.Flag.MOVE,Goal.Flag.LOOK));
    }
    private boolean safe() {
        return !animal.isNoAi() && !animal.isLeashed() && !animal.isPassenger() && !animal.isVehicle()
                && animal.getTarget()==null;
    }
    @Override public boolean canUse() {
        if (!safe()) return false;
        following=HatWearerFinder.closest(animal, hat, START_RANGE_SQ);
        return following!=null;
    }
    @Override public boolean canContinueToUse() {
        return safe() && following != null && HatWearerFinder.eligible(animal,following,hat,KEEP_RANGE_SQ);
    }
    @Override public void start() {refresh=0;}
    @Override public void stop() {following=null;animal.getNavigation().stop();}
    @Override public void tick() {
        if (following==null) return;
        animal.getLookControl().setLookAt(following,30.0F,30.0F);
        if (animal.distanceToSqr(following)<=STOP_SQ) {animal.getNavigation().stop();return;}
        if (--refresh<=0) {refresh=adjustedTickDelay(10);animal.getNavigation().moveTo(following,speed);}
    }
}
