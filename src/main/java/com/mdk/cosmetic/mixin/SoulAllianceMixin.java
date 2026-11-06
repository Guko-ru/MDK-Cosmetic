package com.mdk.cosmetic.mixin;
@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.entity.Entity.class)
public abstract class SoulAllianceMixin {
 @org.spongepowered.asm.mixin.injection.Inject(method="isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z",at=@org.spongepowered.asm.mixin.injection.At("HEAD"),cancellable=true)
 private void mdk$wardenAlliance(net.minecraft.world.entity.Entity other,org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> result){
  if((Object)this instanceof com.mdk.cosmetic.CollectorSoulEntity&&other instanceof net.minecraft.world.entity.monster.warden.Warden)result.setReturnValue(true);
 }
}
