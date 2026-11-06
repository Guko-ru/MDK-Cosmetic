package com.mdk.cosmetic.mixin;
@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.entity.item.ItemEntity.class)
public interface PickupTargetMixin extends com.mdk.cosmetic.platform.PickupTarget {
 @org.spongepowered.asm.mixin.gen.Accessor("target") java.util.UUID mdk$getPickupTarget();
}
