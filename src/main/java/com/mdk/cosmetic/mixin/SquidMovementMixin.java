package com.mdk.cosmetic.mixin;
import com.mdk.cosmetic.platform.SquidMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(net.minecraft.world.entity.animal.squid.Squid.class) public interface SquidMovementMixin extends SquidMovement {
 @Accessor("movementVector") void mdk$setMovement(net.minecraft.world.phys.Vec3 movement);
}
