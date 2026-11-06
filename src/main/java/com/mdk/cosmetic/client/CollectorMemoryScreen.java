package com.mdk.cosmetic.client;

import com.mdk.cosmetic.CollectorMemoryMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Paged, themed index of the contents of the Collector's expanded memory. */
public final class CollectorMemoryScreen extends AbstractContainerScreen<CollectorMemoryMenu> {
    public CollectorMemoryScreen(CollectorMemoryMenu menu,Inventory inv,Component name){
        super(menu,inv,name,176,208); 
        inventoryLabelY=111;
    }
    private void rect(GuiGraphicsExtractor g,int x,int y,int w,int h,int color){
        g.fill(leftPos+x,topPos+y,leftPos+x+w,topPos+y+h,color);
    }
    private void border(GuiGraphicsExtractor g,int x,int y,int w,int h,int color){
        rect(g,x,y,w,1,color);rect(g,x,y+h-1,w,1,color);
        rect(g,x,y,1,h,color);rect(g,x+w-1,y,1,h,color);
    }
    private void renderMachineBackground(GuiGraphicsExtractor g,float partial,int mx,int my){
        rect(g,0,0,176,208,0xFF17101F);border(g,0,0,176,208,0xFFAB73D3);
        rect(g,5,18,166,81,0xFF34243F);
        for(int i=0;i<27;i++){
            int x=8+(i%9)*18,y=22+(i/9)*18;
            rect(g,x-1,y-1,18,18,0xFF86659D);
            rect(g,x,y,16,16,0xFF21162A);
        }
        rect(g,5,119,166,85,0xFF2B2036);
        for(int i=0;i<36;i++){
            int x=8+(i%9)*18,y=(i<27?123+(i/9)*18:181);
            rect(g,x-1,y-1,18,18,0xFF86659D);
            rect(g,x,y,16,16,0xFF21162A);
        }
        rect(g,8,87,27,17,0xFF613B80);border(g,8,87,27,17,0xFFAC7DCE);
        rect(g,141,87,27,17,0xFF613B80);border(g,141,87,27,17,0xFFAC7DCE);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){
        g.centeredText(font,title,88,6,0xFFEACDFB);
        g.centeredText(font,"<",21,91,0xFFFFFFFF);
        g.centeredText(font,">",154,91,0xFFFFFFFF);
        g.centeredText(font,(menu.getPage()+1)+" / "+menu.getPages(),88,91,0xFFD2AEDC);
        g.text(font,Component.translatable("container.inventory"),8,111,0xFFE3D0EB,false);
        // Real quantities are stored as long integers, not 64-item slot stacks.
        for(int i=0;i<27;i++){
            long n=menu.getQuantity(i);
            if(n<=1)continue;
            String count=n>=1_000_000?((n/100_000)/10.0)+"m":n>=1000?(n/1000)+"k":Long.toString(n);
            int x=8+(i%9)*18,y=22+(i/9)*18;
            g.text(font,count,x+16-font.width(count),y+10,0xFFFFE4FF,true);
        }
    }
    @Override public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent mdk$mouse,boolean mdk$doubleClick){double mx=mdk$mouse.x(),my=mdk$mouse.y();int button=mdk$mouse.button();
        if(button==0){int x=(int)mx-leftPos,y=(int)my-topPos;
            if(y>=87&&y<104 && x>=8&&x<35){
                if(minecraft!=null&&minecraft.gameMode!=null)
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0);
                return true;
            }
            if(y>=87&&y<104 && x>=141&&x<168){
                if(minecraft!=null&&minecraft.gameMode!=null)
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1);
                return true;
            }
        }
        return super.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(mx,my,mdk$mouse.buttonInfo()),mdk$doubleClick);
    }
    @Override public void extractBackground(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partial) {}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){
        extractTransparentBackground(g);renderMachineBackground(g,partial,mx,my);super.extractRenderState(g,mx,my,partial);extractTooltip(g,mx,my);
    }
}
