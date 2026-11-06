package com.mdk.cosmetic.mixin;
import com.mdk.cosmetic.platform.Events;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.core.BlockPos;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ServerExplosion.class) public abstract class ExplosionMixin {
 @Inject(method="calculateExplodedPositions",at=@At("RETURN"))
 private void mdk$blocks(CallbackInfoReturnable<List<BlockPos>> ci){Events.dispatch(new Events.ExplosionEvent.Detonate(((ServerExplosion)(Object)this).level(),ci.getReturnValue()));}
}
