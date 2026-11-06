package com.mdk.cosmetic.client;

import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import com.mdk.cosmetic.client.ClientEvents.RenderGuiEvent;

/** Text is 0.85x vanilla subtitle size and word-wrapped by pixel width into centered lines. */
public final class CollectorDialogueOverlay {
    private static String dialogue="";
    private static int color=0xD49BFF;
    private static long start;
    private static final long DURATION_MS=3400;
    private CollectorDialogueOverlay() {}
    public static void receive(String message,int rgb) {
        dialogue=message;
        color=rgb&0xFFFFFF;
        start=Util.getMillis();
    }
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if(dialogue.isEmpty())return;
        long elapsed=Util.getMillis()-start;
        if(elapsed>=DURATION_MS) {dialogue="";return;}
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null || mc.options.hideGui)return;
        Font font=mc.font;
        GuiGraphics g=event.getGuiGraphics();
        final float scale=0.85F;
        int screenWidth=mc.getWindow().getGuiScaledWidth();
        int screenHeight=mc.getWindow().getGuiScaledHeight();
        int maxWidth=(int)(Math.min(300,screenWidth*0.80)/scale);
        List<FormattedCharSequence> lines=font.split(Component.translatable(dialogue),maxWidth);
        float alpha=Math.min(1F,Math.min(elapsed/260F,(DURATION_MS-elapsed)/350F));
        int argb=((int)(alpha*255)<<24)|color;
        g.pose().pushMatrix();
        g.pose().scale(scale,scale);
        int x=(int)(screenWidth/(scale*2F));
        int y=(int)(screenHeight/(scale*2F))-(lines.size()*12)/2;
        for(FormattedCharSequence line:lines) {
            g.drawCenteredString(font,line,x,y,argb);
            y+=12;
        }
        g.pose().popMatrix();
    }
}
