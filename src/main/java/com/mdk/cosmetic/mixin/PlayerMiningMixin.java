package com.mdk.cosmetic.mixin;
import com.mdk.cosmetic.platform.Events;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Player.class) public abstract class PlayerMiningMixin {
 @Inject(method="getDestroySpeed",at=@At("RETURN"),cancellable=true) private void mdk$speed(BlockState state,CallbackInfoReturnable<Float> ci){var event=new Events.PlayerEvent.BreakSpeed((Player)(Object)this,state,ci.getReturnValue());Events.dispatch(event);ci.setReturnValue(event.getNewSpeed());}
}
