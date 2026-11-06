package com.mdk.cosmetic.mixin.client;
import com.mdk.cosmetic.MDKCosmetic;
import com.mdk.cosmetic.client.UnoRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;
/** Uno uses a player-shaped pose, but must always use the registered boss renderer. */
@Mixin(EntityRenderDispatcher.class)
public abstract class UnoRendererDispatchMixin {
 @Shadow private Map<EntityType<?>,EntityRenderer<?,?>> renderers;
 @Inject(method="getRenderer(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)Lnet/minecraft/client/renderer/entity/EntityRenderer;",at=@At("HEAD"),cancellable=true)
 private void mdk$bossRenderer(EntityRenderState state,CallbackInfoReturnable<EntityRenderer<?,?>> result){
  if(state instanceof UnoRenderState){var renderer=renderers.get(MDKCosmetic.UNO_ENTITY.get());if(renderer!=null)result.setReturnValue(renderer);}
 }
}
