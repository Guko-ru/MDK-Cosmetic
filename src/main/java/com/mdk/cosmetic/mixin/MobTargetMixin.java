package com.mdk.cosmetic.mixin;
import com.mdk.cosmetic.platform.Events;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Mob.class) public abstract class MobTargetMixin {
 @Inject(method="setTarget",at=@At("HEAD"),cancellable=true) private void mdk$target(LivingEntity target,CallbackInfo ci){var event=new Events.LivingChangeTargetEvent((Mob)(Object)this,target);Events.dispatch(event);if(event.isCanceled())ci.cancel();}
}
