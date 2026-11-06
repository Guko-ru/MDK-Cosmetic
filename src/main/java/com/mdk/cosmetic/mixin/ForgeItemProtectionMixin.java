package com.mdk.cosmetic.mixin;
import com.mdk.cosmetic.platform.ForgeGameplayEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Entity.class) public abstract class ForgeItemProtectionMixin {
 @Inject(method="isInvulnerableToBase",at=@At("RETURN"),cancellable=true) private void mdk$relicProtection(DamageSource source,CallbackInfoReturnable<Boolean> ci){
  var event=new ForgeGameplayEvents.EntityInvulnerabilityCheckEvent((Entity)(Object)this,source,ci.getReturnValue());ForgeGameplayEvents.EntityInvulnerabilityCheckEvent.BUS.post(event);ci.setReturnValue(event.isInvulnerable());
 }
}
