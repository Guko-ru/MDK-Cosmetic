package com.mdk.cosmetic;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.Llama;
/** Llamas stay leashed, caravan and ridden when appropriate; otherwise follow hats. */
public final class LlamaHatAttractionGoal extends Goal {
 private final Llama llama;private LivingEntity target;private int refresh;
 public LlamaHatAttractionGoal(Llama llama) {this.llama=llama;setFlags(EnumSet.of(Goal.Flag.MOVE,Goal.Flag.LOOK));}
 private boolean safe(){return !llama.isNoAi() && !llama.isLeashed() && !llama.isVehicle()
  && !llama.isPassenger() && llama.getTarget()==null;}
 @Override public boolean canUse(){if(!safe())return false;target=HatWearerFinder.closest(llama,MDKCosmetic.LLAMA_HAT.get(),256);return target!=null;}
 @Override public boolean canContinueToUse(){return safe() && target!=null && HatWearerFinder.eligible(llama,target,MDKCosmetic.LLAMA_HAT.get(),324);}
 @Override public void start(){refresh=0;}
 @Override public void stop(){target=null;llama.getNavigation().stop();}
 @Override public void tick(){if(target==null)return;llama.getLookControl().setLookAt(target,25,25);
  if(llama.distanceToSqr(target)<12.25){llama.getNavigation().stop();return;}
  if(--refresh<=0){refresh=adjustedTickDelay(12);llama.getNavigation().moveTo(target,1.08);}}
}
