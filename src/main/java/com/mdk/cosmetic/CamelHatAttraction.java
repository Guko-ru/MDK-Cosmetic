package com.mdk.cosmetic;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.camel.Camel;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
/** Camel Brain hat lure works for players and hostile mobs alike; does not affect mounted camels. */
public final class CamelHatAttraction {
 private static final Map<Camel,LivingEntity> TARGETS=new WeakHashMap<>();
 private CamelHatAttraction(){}
 public static void onCamelTick(EntityTickEvent.Post event){
  if(!(event.getEntity() instanceof Camel camel) || camel.level().isClientSide() || !camel.isAlive() || camel.isNoAi())return;
  if(camel.isVehicle() || camel.isPassenger() || camel.isLeashed()){clear(camel);return;}
  LivingEntity t=TARGETS.get(camel);
  if(t==null || !HatWearerFinder.eligible(camel,t,MDKCosmetic.CAMEL_HAT.get(),324)){
   if(camel.tickCount%10!=0){clear(camel);return;}
   t=HatWearerFinder.closest(camel,MDKCosmetic.CAMEL_HAT.get(),256);
  }
  if(t==null){clear(camel);return;}
  TARGETS.put(camel,t);
  if(camel.isCamelSitting())camel.standUp();
  camel.getBrain().setMemory(MemoryModuleType.LOOK_TARGET,new EntityTracker(t,true));
  if(camel.distanceToSqr(t)<=12.25){camel.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);camel.getNavigation().stop();return;}
  camel.getBrain().setMemory(MemoryModuleType.WALK_TARGET,new WalkTarget(t.position(),1.05F,3));
 }
 private static void clear(Camel camel){if(TARGETS.remove(camel)!=null){
  camel.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
  camel.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);camel.getNavigation().stop();}}
}
