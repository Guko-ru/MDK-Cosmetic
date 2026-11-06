package com.mdk.cosmetic.mixin;
import com.mdk.cosmetic.platform.RegistryContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(BlockBehaviour.Properties.class) public abstract class BlockPropertiesMixin {
 @Inject(method="<init>",at=@At("RETURN")) private void mdk$id(CallbackInfo ci){RegistryContext.applyBlock((BlockBehaviour.Properties)(Object)this);}
 @Inject(method={"ofFullCopy","ofLegacyCopy"},at=@At("RETURN")) private static void mdk$copy(BlockBehaviour block,CallbackInfoReturnable<BlockBehaviour.Properties> ci){RegistryContext.applyBlock(ci.getReturnValue());}
}
