package com.mdk.cosmetic.mixin.client;
@org.spongepowered.asm.mixin.Mixin(targets="net.minecraft.client.gui.render.GuiRenderer$Draw")
public interface GuiDrawScissorAccessor {
 @org.spongepowered.asm.mixin.gen.Accessor("scissorArea") net.minecraft.client.gui.navigation.ScreenRectangle mdk$getScissorArea();
}
