package com.mdk.cosmetic.mixin;
import com.mdk.cosmetic.platform.ForgeEntityTicks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntity.class)
public abstract class ForgeLivingTickMixin {
 @Inject(method="tick",at=@At("TAIL")) private void mdk$postLivingTick(CallbackInfo ci){MinecraftForge.EVENT_BUS.post(new ForgeEntityTicks.EntityTickEvent.Post((LivingEntity)(Object)this));}
}
