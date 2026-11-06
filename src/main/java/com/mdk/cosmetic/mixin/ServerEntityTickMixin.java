package com.mdk.cosmetic.mixin;
import com.mdk.cosmetic.platform.Events;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Dispatch only for actually ticking entities, including passengers; unloaded/inactive chunks stay inactive. */
@Mixin(ServerLevel.class) public abstract class ServerEntityTickMixin {
 @Inject(method="tickNonPassenger",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;tick()V",shift=At.Shift.AFTER))
 private void mdk$tick(Entity entity,CallbackInfo ci){mdk$post(entity);}
 @Inject(method="tickPassenger",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;rideTick()V",shift=At.Shift.AFTER))
 private void mdk$passenger(Entity vehicle,Entity passenger,CallbackInfo ci){mdk$post(passenger);}
 @org.spongepowered.asm.mixin.Unique private static void mdk$post(Entity entity){Events.dispatch(new Events.EntityTickEvent.Post(entity));if(entity instanceof Player player)Events.dispatch(new Events.PlayerTickEvent.Post(player));}
}
