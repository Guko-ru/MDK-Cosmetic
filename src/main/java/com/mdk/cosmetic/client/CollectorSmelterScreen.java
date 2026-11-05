package com.mdk.cosmetic.client;

import com.mdk.cosmetic.CollectorSmelterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class CollectorSmelterScreen extends AbstractContainerScreen<CollectorSmelterMenu> {
    public CollectorSmelterScreen(CollectorSmelterMenu menu,Inventory inv,Component title){
        super(menu,inv,title);imageWidth=176;imageHeight=198;inventoryLabelY=99;
    }
    private void fill(GuiGraphics g,int x,int y,int w,int h,int color){g.fill(leftPos+x,topPos+y,leftPos+x+w,topPos+y+h,color);}
    @Override protected void renderBg(GuiGraphics g,float partial,int mx,int my){
        fill(g,0,0,176,198,0xFF191121);fill(g,2,2,172,194,0xFF392748);
        fill(g,6,18,164,77,0xFF251B30);fill(g,6,104,164,90,0xFF24172E);
        for(int i=0;i<39;i++){
            int x,y;if(i==0){x=56;y=34;}else if(i==1){x=56;y=70;}else if(i==2){x=116;y=34;}
            else if(i<30){int a=i-3;x=8+(a%9)*18;y=112+(a/9)*18;}
            else {x=8+(i-30)*18;y=170;}
            fill(g,x-1,y-1,18,18,0xFF9679B4);fill(g,x,y,16,16,0xFF20172C);
        }
        fill(g,82,39,26,8,0xFF190D23);
        fill(g,82,39,Math.min(26,menu.progress()*26/menu.total()),8,0xFFB86FEA);
        if(menu.isLit()){fill(g,58,58,9,9,0xFFFF9D29);fill(g,60,55,5,5,0xFFEBC252);}
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        g.drawCenteredString(font,title,88,6,0xFFFFD8FF);
        g.drawString(font,Component.translatable("container.inventory"),8,99,0xFFDFBDE8,false);
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderBackground(g,mx,my,partial);super.render(g,mx,my,partial);renderTooltip(g,mx,my);
    }
}
