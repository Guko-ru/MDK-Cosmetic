package com.mdk.cosmetic.gametest.legacy.mixin;
import com.mdk.cosmetic.gametest.legacy.LegacyClientContext;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(InputConstants.class)
public abstract class LegacyTestInputMixin {
 @Inject(method="isKeyDown",at=@At("HEAD"),cancellable=true)
 private static void mdk$heldTestKey(int key,CallbackInfoReturnable<Boolean> result){if(Boolean.getBoolean("mdkcosmetic.clientRegressions")&&LegacyClientContext.HELD.contains(key))result.setReturnValue(true);}
}
