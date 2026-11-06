package com.mdk.cosmetic.mixin.client;
import com.mdk.cosmetic.client.CosmeticItemTints;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Registers the codec before asynchronous item model decoding begins. */
@Mixin(ItemTintSources.class) public abstract class CosmeticItemTintSourcesMixin {
 @Inject(method="bootstrap",at=@At("TAIL")) private static void mdk$registerCosmeticSource(CallbackInfo ci){ItemTintSources.ID_MAPPER.put(Identifier.parse("mdkcosmetic:cosmetic_color"),CosmeticItemTints.Source.CODEC);}
}
