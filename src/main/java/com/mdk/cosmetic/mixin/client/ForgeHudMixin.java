package com.mdk.cosmetic.mixin.client;
import com.mdk.cosmetic.client.ForgeGuiEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.*;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Gui.class) public abstract class ForgeHudMixin {
 @Inject(method="render",at=@At("TAIL")) private void mdk$hud(GuiGraphics graphics,DeltaTracker delta,CallbackInfo ci){ForgeGuiEvent.Post.BUS.post(new ForgeGuiEvent.Post(graphics));}
}
