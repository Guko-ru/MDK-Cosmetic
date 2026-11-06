package com.mdk.cosmetic.mixin.client;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
/** Forge exposes the stack getter but keeps the vanilla stack class private. */
@Mixin(targets="net.minecraft.client.gui.GuiGraphicsExtractor$ScissorStack")
public interface GuiScissorStackAccessor {
 @Invoker("peek") ScreenRectangle mdk$peek();
}
