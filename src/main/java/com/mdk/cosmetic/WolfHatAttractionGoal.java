package com.mdk.cosmetic;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
/** Wild wolves follow wolf hats on players or monsters; dogs only follow their owners. */
public final class WolfHatAttractionGoal extends Goal {
 private final Wolf wolf;private LivingEntity following;private int refresh;
 public WolfHatAttractionGoal(Wolf wolf){this.wolf=wolf;setFlags(EnumSet.of(Goal.Flag.MOVE,Goal.Flag.LOOK));}
 private boolean safe(){return wolf.isAlive() && !wolf.isNoAi() && !wolf.isLeashed()
  && !wolf.isPassenger() && !wolf.isInSittingPose() && wolf.getTarget()==null && !wolf.isAngry();}
 private boolean eligible(LivingEntity entity,double range){return HatWearerFinder.eligible(wolf,entity,
  MDKCosmetic.WOLF_HAT.get(),range) && (!wolf.isTame() || (entity instanceof Player p
  && wolf.getOwnerUUID()!=null && wolf.getOwnerUUID().equals(p.getUUID())));}
 @Override public boolean canUse(){if(!safe())return false;
  following=HatWearerFinder.closest(wolf,MDKCosmetic.WOLF_HAT.get(),256);
  if(following!=null && eligible(following,256))return true;
  following=null;
  if(wolf.isTame())for(Player player:wolf.level().players())if(eligible(player,256)){following=player;return true;}
  return false;}
 @Override public boolean canContinueToUse(){return safe() && following!=null && eligible(following,324);}
 @Override public void start(){refresh=0;}
 @Override public void stop(){following=null;wolf.getNavigation().stop();}
 @Override public void tick(){if(following==null)return;wolf.getLookControl().setLookAt(following,30,30);
  if(wolf.distanceToSqr(following)<7.84){wolf.getNavigation().stop();return;}
  if(--refresh<=0){refresh=adjustedTickDelay(10);wolf.getNavigation().moveTo(following,1.16);}}
}
