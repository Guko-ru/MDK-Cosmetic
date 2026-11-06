package com.mdk.cosmetic;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
/** Cautious attraction that cannot override bear combat or cub protection. */
public final class PolarBearHatAttractionGoal extends Goal {
 private final PolarBear bear;private LivingEntity target;private int refresh;
 public PolarBearHatAttractionGoal(PolarBear bear){this.bear=bear;setFlags(EnumSet.of(Goal.Flag.MOVE,Goal.Flag.LOOK));}
 private boolean guarded(){return bear.getTarget()!=null || !bear.level().getEntitiesOfClass(PolarBear.class,
  bear.getBoundingBox().inflate(12),other->other!=bear && other.isBaby()).isEmpty();}
 private boolean safe(){return !bear.isNoAi() && !bear.isLeashed() && !bear.isPassenger() && !guarded();}
 @Override public boolean canUse(){if(!safe())return false;target=HatWearerFinder.closest(bear,MDKCosmetic.POLAR_BEAR_HAT.get(),256);return target!=null;}
 @Override public boolean canContinueToUse(){return safe() && target!=null && HatWearerFinder.eligible(bear,target,MDKCosmetic.POLAR_BEAR_HAT.get(),324);}
 @Override public void start(){refresh=0;}
 @Override public void stop(){target=null;bear.getNavigation().stop();}
 @Override public void tick(){if(target==null)return;bear.getLookControl().setLookAt(target,30,30);
  if(bear.distanceToSqr(target)<=8){bear.getNavigation().stop();return;}
  if(--refresh<=0){refresh=adjustedTickDelay(10);bear.getNavigation().moveTo(target,1.05);}}
}
