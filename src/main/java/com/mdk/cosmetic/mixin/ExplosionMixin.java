package com.mdk.cosmetic.mixin;
import com.mdk.cosmetic.platform.Events;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Explosion.class) public abstract class ExplosionMixin {
 @Shadow @Final private Level level;
 @Inject(method="explode",at=@At("TAIL")) private void mdk$blocks(CallbackInfo ci){Events.dispatch(new Events.ExplosionEvent.Detonate(level,((Explosion)(Object)this).getToBlow()));}
}
