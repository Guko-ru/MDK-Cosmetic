package com.mdk.cosmetic.client;

import com.mdk.cosmetic.CollectorEquipmentMenu;
import com.mdk.cosmetic.MDKCosmetic;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import com.mdk.cosmetic.platform.Networking;

/** Purple Collector equipment hub with one fold-out settings panel, no nested GUI. */
public final class CollectorEquipmentScreen extends AbstractContainerScreen<CollectorEquipmentMenu> {
    private static final int W=352,H=244;
    private int section=-1;
    private boolean draggingSpeed;
    private float scale=1.0f;
    public CollectorEquipmentScreen(CollectorEquipmentMenu menu,Inventory inventory,Component title){
        super(menu,inventory,title);imageWidth=W;imageHeight=H;
        inventoryLabelX=20;inventoryLabelY=147;
    }
    private Component tr(String s){return Component.translatable("screen.mdkcosmetic.equipment_"+s);}
    @Override protected void init(){
        scale=Math.max(0.25f,Math.min(1.0f,Math.min((width-8.0f)/W,(height-8.0f)/H)));
        super.init();
        leftPos=Math.round((width/scale-W)/2);topPos=Math.round((height/scale-H)/2);
    }
    private void rect(GuiGraphics g,int x,int y,int w,int h,int c){g.fill(leftPos+x,topPos+y,leftPos+x+w,topPos+y+h,c);}
    private void frame(GuiGraphics g,int x,int y,int w,int h,int c){rect(g,x,y,w,1,c);rect(g,x,y+h-1,w,1,c);rect(g,x,y,1,h,c);rect(g,x+w-1,y,1,h,c);}
    private void button(GuiGraphics g,int x,int y,int w,int h,boolean enabled,boolean active){
        rect(g,x,y,w,h,enabled?(active?0xFF754CA1:0xFF48315F):0xFF282431);
        frame(g,x,y,w,h,enabled?(active?0xFFDCAFFF:0xFF996FC2):0xFF564A61);
    }
    private void label(GuiGraphics g,Component text,int x,int y,int w,int color){
        g.drawString(font,font.plainSubstrByWidth(text.getString(),w),x,y,color,false);
    }
    @Override protected void renderBg(GuiGraphics g,float pt,int mx,int my){
        rect(g,0,0,W,H,0xFF171120);frame(g,0,0,W,H,0xFFAA7BDA);
        rect(g,6,17,176,119,0xFF241A31);frame(g,6,17,176,119,0xFF735493);
        rect(g,10,142,174,97,0xFF241A31);frame(g,10,142,174,97,0xFF735493);
        if(section>=0){
            rect(g,188,17,157,222,0xFF241A31);
            frame(g,188,17,157,222,0xFF9167B4);
        }
        rect(g,22,41,18,18,0xFF654980);rect(g,23,42,16,16,0xFF191221);
        rect(g,22,87,18,18,0xFF654980);rect(g,23,88,16,16,0xFF191221);
        for(int i=0;i<36;i++){
            int x=20+(i%9)*18;int y=i<27?159+(i/9)*18:217;
            rect(g,x-1,y-1,18,18,0xFF735A90);rect(g,x,y,16,16,0xFF171020);
        }
        for(int i=0;i<5;i++){
            button(g,54,35+i*19,119,17,enabled(i),section==i);
            net.minecraft.world.item.ItemStack icon = new net.minecraft.world.item.ItemStack(switch(i){
                case 0,1 -> MDKCosmetic.COLLECTOR_HELMET.get();
                case 2 -> MDKCosmetic.COLLECTOR_CHESTPLATE.get();
                case 3 -> MDKCosmetic.COLLECTOR_LEGGINGS.get();
                default -> MDKCosmetic.COLLECTOR_BOOTS.get();
            });
            g.renderItem(icon,leftPos+55,topPos+35+i*19);
        }
        if(section==0)button(g,196,77,141,22,menu.fullArmor(),menu.fearMode()==1);
        if(section==1)button(g,196,77,141,22,menu.helmet(),menu.nightVision());
        if(section==2)button(g,196,77,141,22,menu.chest(),menu.flight());
        if(section==3){
            button(g,197,84,26,22,menu.legs(),false);
            button(g,308,84,26,22,menu.legs(),false);
            rect(g,229,93,73,5,0xFF392644);
            rect(g,229,93,menu.speed()*73/10,5,0xFFB685EB);
        }
        if(section==4)button(g,196,77,141,22,menu.boots(),menu.noFall());
    }
    private boolean enabled(int id){return switch(id){case 0->menu.fullArmor();case 1->menu.helmet();case 2->menu.chest();case 3->menu.legs();default->menu.boots();};}
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        label(g,tr("title"),11,5,320,0xFFE2C9FF);
        label(g,tr("backpack"),12,25,145,0xFFE4D4F9);
        label(g,tr("fuel"),12,74,145,0xFFD0B2E9);
        label(g,Component.translatable("container.inventory"),20,147,140,0xFFD3B9E9);
        for(int i=0;i<5;i++)label(g,tr("tab_"+i),75,40+i*19,95,enabled(i)?0xFFECD9FF:0xFF83788F);
        if(section<0)return;
        label(g,tr("heading_"+section),195,28,143,0xFFE5C9FF);
        label(g,tr("desc_"+section),195,48,143,0xFFBCA8D6);
        if(section==0){
            label(g,tr("fear_"+menu.fearMode()),203,84,126,menu.fullArmor()?0xFFF3D8FF:0xFF766782);
        }else if(section==1){
            label(g,tr(menu.nightVision()?"enabled":"disabled"),203,84,126,0xFFE8D6FF);
        }else if(section==2){
            label(g,tr(menu.flight()?"enabled":"disabled"),203,84,126,0xFFE8D6FF);
            label(g,tr("flight_remaining"),196,112,148,0xFFBCA8D6);
            g.drawString(font,Integer.toString(menu.flightRemaining()/20),197,125,0xFFE8D6FF,false);
        }else if(section==3){
            label(g,Component.literal("−"),204,91,15,0xFFFFFFFF);
            label(g,Component.literal("+"),315,91,15,0xFFFFFFFF);
            label(g,Component.literal("x"+String.format(java.util.Locale.ROOT,"%.1f",1.0+menu.speed()/10.0)),251,112,73,0xFFE8D6FF);
        }else if(section==4){
            label(g,tr(menu.noFall()?"enabled":"disabled"),203,84,126,0xFFE8D6FF);
        }
        if(!enabled(section))label(g,tr("requires_piece"),197,167,143,0xFF977FAD);
    }
    private void press(int action){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action);}
    @Override public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent mdk$mouse,boolean mdk$doubleClick){double mx=mdk$mouse.x(),my=mdk$mouse.y();int button=mdk$mouse.button();
        double vx=mx/scale,vy=my/scale;
        int x=(int)vx-leftPos,y=(int)vy-topPos;
        if(button==0){
            if(x>=54&&x<173){for(int i=0;i<5;i++)if(y>=35+i*19&&y<52+i*19){
                section=section==i?-1:i;
                draggingSpeed=false;
                return true;
            }}
            if(section==3&&y>=84&&y<106&&menu.legs()){
                if(x>=197&&x<223){press(4);return true;}
                if(x>=308&&x<334){press(3);return true;}
                if(x>=229&&x<302){
                    draggingSpeed=true;
                    press(100+Math.clamp(Math.round((x-229)*10.0F/73),0,10));
                    return true;
                }
            }
            if(x>=196&&x<337&&y>=77&&y<99&&enabled(section)&&section!=3){press(section==4?5:section);return true;}
        }
        return super.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(vx,vy,mdk$mouse.buttonInfo()),mdk$doubleClick);
    }
    @Override public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent mdk$mouse){double mx=mdk$mouse.x(),my=mdk$mouse.y();int button=mdk$mouse.button();
        draggingSpeed=false;
        return super.mouseReleased(new net.minecraft.client.input.MouseButtonEvent(mx/scale,my/scale,mdk$mouse.buttonInfo()));
    }
    @Override public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent mdk$mouse,double dx,double dy){double mx=mdk$mouse.x(),my=mdk$mouse.y();int button=mdk$mouse.button();
        if(button==0 && draggingSpeed && menu.legs()) {
            int x=(int)(mx/scale)-leftPos;
            int target=Math.clamp(Math.round((x-229)*10.0F/73),0,10);
            if(target!=menu.speed())press(100+target);
            return true;
        }
        return super.mouseDragged(new net.minecraft.client.input.MouseButtonEvent(mx/scale,my/scale,mdk$mouse.buttonInfo()),dx/scale,dy/scale);
    }
    // mdk: unified scaled canvas; native background callbacks must not draw it a second time.
    @Override public void renderBackground(GuiGraphics graphics,int mouseX,int mouseY,float partial) {}

    @Override public void render(GuiGraphics g,int mx,int my,float pt){
        int x=(int)(mx/scale),y=(int)(my/scale);
        renderTransparentBackground(g);
        g.pose().pushMatrix();g.pose().scale(scale,scale);
        renderBg(g,pt,x,y);super.render(g,x,y,pt);
        renderTooltip(g,x,y);
        g.pose().popMatrix();
    }
}
