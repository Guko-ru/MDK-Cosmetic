package com.mdk.cosmetic.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** Short blackout + crimson greeting + camera shake when the End dragon is defeated. */
public final class UnoEncounterOverlay {
    private static long start = -1L;
    private static long blackMs;
    private static long shakeMs;
    private UnoEncounterOverlay() { }

    public static void receive(int blackoutTicks,int cameraShakeTicks) {
        start = Util.getMillis();
        blackMs = Math.min(8000,Math.max(0,blackoutTicks)*50L);
        shakeMs = Math.min(3500,Math.max(0,cameraShakeTicks)*50L);
    }

    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (start < 0L) return;
        long ms=Util.getMillis()-start;
        if (ms>blackMs+350L) {start=-1L;return;}
        Minecraft mc=Minecraft.getInstance();
        if (mc.player==null) return;
        int w=mc.getWindow().getGuiScaledWidth();
        int h=mc.getWindow().getGuiScaledHeight();
        GuiGraphics g=event.getGuiGraphics();
        float fadeIn=Math.min(1F,ms/250F);
        float fadeOut=Math.min(1F,(blackMs-ms)/320F);
        float alpha=Math.max(0F,Math.min(fadeIn,fadeOut));
        // Two seconds of true dark screen, not Minecraft Darkness/Fog effects.
        g.fill(0,0,w,h,((int)(255*alpha)<<24));
        if (ms>=300L && ms<blackMs+220L) {
            float titleAlpha=Math.min(1F,Math.min((ms-300L)/230F,(blackMs+200L-ms)/270F));
            int argb=((int)(Math.max(0,titleAlpha)*255)<<24)|0xFF2020;
            g.pose().pushPose();
            float scale=3.0F;
            g.pose().scale(scale,scale,1F);
            int jitter=(ms<1200L) ? (int)(Math.sin(ms*0.082)*2.5) : 0;
            g.drawCenteredString(mc.font,Component.translatable("title.mdkcosmetic.uno_greeting"),
                    (int)(w/(2*scale))+jitter,(int)(h/(2*scale))-12+jitter,argb);
            g.pose().popPose();
        }
    }

    /** World camera tremor; yaw, pitch and roll are visual only, no player input changed. */
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if(start<0) return;
        long ms=Util.getMillis()-start;
        if(ms<220L || ms>shakeMs+220L) return;
        float strength=(float)Math.min(1D,(shakeMs+220D-ms)/350D);
        float f=(float)ms;
        event.setRoll(event.getRoll()+(float)Math.sin(f*0.061F)*1.3F*strength);
        event.setPitch(event.getPitch()+(float)Math.sin(f*0.077F)*0.8F*strength);
        event.setYaw(event.getYaw()+(float)Math.sin(f*0.052F)*0.65F*strength);
    }
}
