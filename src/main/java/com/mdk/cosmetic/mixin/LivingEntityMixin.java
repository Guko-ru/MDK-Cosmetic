package com.mdk.cosmetic.mixin;
import com.mdk.cosmetic.platform.Events;
import com.mdk.cosmetic.platform.FabricHooks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
 @Inject(method="dropAllDeathLoot",at=@At("HEAD")) private void mdk$begin(net.minecraft.server.level.ServerLevel level,DamageSource source,CallbackInfo ci){FabricHooks.beginDrops((LivingEntity)(Object)this,source);}
 @Inject(method="dropAllDeathLoot",at=@At("RETURN")) private void mdk$end(net.minecraft.server.level.ServerLevel level,DamageSource source,CallbackInfo ci){FabricHooks.endDrops((LivingEntity)(Object)this);}
 @Inject(method="causeFallDamage",at=@At("HEAD"),cancellable=true) private void mdk$fall(double distance,float multiplier,DamageSource source,CallbackInfoReturnable<Boolean> ci){var event=new Events.LivingFallEvent((LivingEntity)(Object)this);Events.dispatch(event);if(event.isCanceled())ci.setReturnValue(false);}
 @ModifyVariable(method="actuallyHurt",at=@At("HEAD"),argsOnly=true,ordinal=0) private float mdk$damage(float amount,net.minecraft.server.level.ServerLevel damageLevel,DamageSource source,float original){var event=new Events.LivingDamageEvent.Pre((LivingEntity)(Object)this,source,amount);Events.dispatch(event);return event.getNewDamage();}
}
