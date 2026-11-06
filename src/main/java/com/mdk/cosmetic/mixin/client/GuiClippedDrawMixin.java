package com.mdk.cosmetic.mixin.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import com.mojang.renderpearl.api.commands.RenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Fully clipped GUI draws contain no visible pixels; the new backend rejects a zero-sized scissor. */
@Mixin(GuiRenderer.class)
public abstract class GuiClippedDrawMixin {
 @Inject(method="executeDraw",at=@At("HEAD"),cancellable=true)
 private void mdk$skipFullyClippedDraw(@Coerce Object draw,RenderPass pass,CallbackInfo result){
  var clip=((GuiDrawScissorAccessor)draw).mdk$getScissorArea();if(clip==null)return;
  var window=Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState;
  int scale=window.guiScale;
  if(Math.min(clip.right()*scale,window.width)<=clip.left()*scale||Math.min(clip.bottom()*scale,window.height)<=clip.top()*scale)result.cancel();
 }
}
