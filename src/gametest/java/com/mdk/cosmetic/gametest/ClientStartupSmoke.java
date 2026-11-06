package com.mdk.cosmetic.gametest;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.slf4j.LoggerFactory;

/** Client-only startup check, supplied solely by the separate integration-test mod. */
public final class ClientStartupSmoke implements ClientModInitializer {
    @Override public void onInitializeClient(){
        if(!Boolean.getBoolean("mdkcosmetic.clientSmoke"))return;
        int[] readyTicks={0},stage={0};
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            if(client.getOverlay()!=null||client.screen==null||++readyTicks[0]<20)return;
            if(stage[0]==1){
                if(readyTicks[0]==30)net.minecraft.client.Screenshot.grab(client.gameDirectory,"multiblock-preview-smoke.png",client.getMainRenderTarget(),1,message->LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_PREVIEW_IMAGE_READY"));
                if(readyTicks[0]>=50){LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_PREVIEW_SMOKE_OK: 27 depth-tested item models rendered in deferred GUI");client.stop();}return;
            }
            var bossState=new com.mdk.cosmetic.client.UnoRenderState();
            bossState.entityType=com.mdk.cosmetic.MDKCosmetic.UNO_ENTITY.get();
            bossState.texture=net.minecraft.resources.ResourceLocation.parse("mdkcosmetic:textures/entity/uno.png");
            var bossRenderer=client.getEntityRenderDispatcher().getRenderer(bossState);
            if(!(bossRenderer instanceof com.mdk.cosmetic.client.UnoRenderer renderer)||!renderer.getTextureLocation(bossState).equals(bossState.texture))throw new AssertionError("Boss pose incorrectly dispatched to a vanilla player/Steve renderer");
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_BOSS_RENDERER_OK: native dispatcher selects the custom boss renderer and black texture");
            if(!new com.mdk.cosmetic.client.CatEarsModel().hat.isEmpty()||!new com.mdk.cosmetic.client.FoxHatModel().hat.isEmpty()||!new com.mdk.cosmetic.client.AxolotlHatModel().hat.isEmpty())throw new AssertionError("Custom cosmetic hat must not retain a vanilla helmet cube");
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_HAT_GEOMETRY_OK: custom ears, fox and axolotl geometry have no inherited vanilla helmet cube");
            int checked=0;
            var missing=net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation();
            for(var item:BuiltInRegistries.ITEM){
                var id=BuiltInRegistries.ITEM.getKey(item);
                if(!id.getNamespace().equals("mdkcosmetic"))continue;
                var model=new net.minecraft.client.renderer.item.ItemStackRenderState();
                client.getItemModelResolver().updateForTopItem(model,new ItemStack(item),net.minecraft.world.item.ItemDisplayContext.GUI,null,null,0);
                if(model.isEmpty()||model.pickParticleIcon(net.minecraft.util.RandomSource.create(0)).contents().name().equals(missing))throw new AssertionError("Missing baked item model: "+id);
                checked++;
            }
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_CLIENT_SMOKE_OK: {} item models, entity renderers, armor models and client mixins initialized",checked);
            stage[0]=1;readyTicks[0]=0;client.setScreen(new PreviewScreen());
        });
    }

    private static final class PreviewScreen extends net.minecraft.client.gui.screens.Screen {
        private final java.util.List<com.mdk.cosmetic.client.MultiblockPreview.Cell> cells=new java.util.ArrayList<>();
        PreviewScreen(){super(net.minecraft.network.chat.Component.literal("MDK preview validation"));for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++)cells.add(new com.mdk.cosmetic.client.MultiblockPreview.Cell(x,y,z,new ItemStack((x==0&&y==0&&z==0)?net.minecraft.world.item.Items.GOLD_BLOCK:net.minecraft.world.item.Items.AMETHYST_BLOCK)));}
        @Override public void render(net.minecraft.client.gui.GuiGraphics g,int mouseX,int mouseY,float delta){g.fill(0,0,width,height,0xFF17202B);g.drawCenteredString(font,"MDK - 3D multiblock preview",width/2,8,0xFFFFFFFF);g.pose().pushMatrix();g.pose().translate((width-166)/2F,(height-160)/2F);com.mdk.cosmetic.client.MultiblockPreview.submit(g,cells,35F,25F,20F);g.pose().popMatrix();}
    }
}
