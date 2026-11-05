package com.mdk.cosmetic.client;

import com.mdk.cosmetic.MDKCosmetic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/** Very subtle (about ten percent) blood-red screen vignette while Greed is active. */
public final class GreedVignetteOverlay {
    private GreedVignetteOverlay() {}
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null || mc.options.hideGui || !mc.player.hasEffect(MDKCosmetic.GREED_AURA)) return;
        GuiGraphics g=event.getGuiGraphics();
        int w=mc.getWindow().getGuiScaledWidth(),h=mc.getWindow().getGuiScaledHeight();
        // Soft transparent tint (8% alpha) plus stronger edges.
        g.fill(0,0,w,h,0x11A51B29);
        int edge=Math.max(14,Math.min(w,h)/14);
        g.fillGradient(0,0,w,edge,0x28A40015,0x00A40015);
        g.fillGradient(0,h-edge,w,h,0x00A40015,0x28A40015);
        g.fill(0,0,edge,h,0x14A40015);
        g.fill(w-edge,0,w,h,0x14A40015);
    }
}
