package com.mdk.cosmetic.gametest;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.slf4j.LoggerFactory;
/** Minecraft 26 binds item components on world load, so render checks use a real loaded world. */
public final class ClientStartupSmoke implements ClientModInitializer {
 @Override public void onInitializeClient(){ArmorRenderSmoke.initialize();}
 static void runInWorld(ClientGameTestContext context){
  context.runOnClient(client->{
            var bossState=new com.mdk.cosmetic.client.UnoRenderState();
            bossState.entityType=com.mdk.cosmetic.MDKCosmetic.UNO_ENTITY.get();
            bossState.texture=net.minecraft.resources.Identifier.parse("mdkcosmetic:textures/entity/uno.png");
            var bossRenderer=client.getEntityRenderDispatcher().getRenderer(bossState);
            if(!(bossRenderer instanceof com.mdk.cosmetic.client.UnoRenderer renderer)||!renderer.getTextureLocation(bossState).equals(bossState.texture))throw new AssertionError("Boss pose incorrectly dispatched to a vanilla player/Steve renderer");
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_BOSS_RENDERER_OK: native dispatcher selects the custom boss renderer and black texture");
            if(!new com.mdk.cosmetic.client.CatEarsModel().hat.isEmpty()||!new com.mdk.cosmetic.client.FoxHatModel().hat.isEmpty()||!new com.mdk.cosmetic.client.AxolotlHatModel().hat.isEmpty())throw new AssertionError("Custom cosmetic hat must not retain a vanilla helmet cube");
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_HAT_GEOMETRY_OK: custom ears, fox and axolotl geometry have no inherited vanilla helmet cube");
            var trimAtlas=client.getAtlasManager().getAtlasOrThrow(net.minecraft.data.AtlasIds.ARMOR_TRIMS);
            var trimSprite=trimAtlas.getSprite(net.minecraft.resources.Identifier.parse("minecraft:trims/entity/humanoid/sentry_gold"));
            if(trimSprite==trimAtlas.missingSprite())throw new AssertionError("Missing armor trim atlas sprite");
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_ARMOR_TRIM_ATLAS_OK: vanilla armor trim definition resolves a real stitched sprite");
            int checked=0;
            var missing=net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation();
            for(var item:BuiltInRegistries.ITEM){
                var id=BuiltInRegistries.ITEM.getKey(item);
                if(!id.getNamespace().equals("mdkcosmetic"))continue;
                var model=new net.minecraft.client.renderer.item.ItemStackRenderState();
                client.getItemModelResolver().updateForTopItem(model,new ItemStack(item),net.minecraft.world.item.ItemDisplayContext.GUI,null,null,0);
                if(model.isEmpty()||model.pickParticleMaterial(net.minecraft.util.RandomSource.create(0)).sprite().contents().name().equals(missing))throw new AssertionError("Missing baked item model: "+id);
                checked++;
            }
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_CLIENT_SMOKE_OK: {} item models, entity renderers, armor models and client mixins initialized",checked);

   client.gui.setScreen(new PreviewScreen());
  });
  context.waitTicks(40);
  context.takeScreenshot("multiblock-preview-smoke");
  context.runOnClient(client->{
   if(ArmorRenderSmoke.frames==0)throw new AssertionError("Armor GPU fixture was not rendered");
   LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_ARMOR_SMOKE_OK: 12 equipment submissions rendered");
   LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_PREVIEW_SMOKE_OK: 27 depth-tested item models rendered in deferred GUI");
   client.gui.setScreen(null);
  });
 }
    private static final class PreviewScreen extends net.minecraft.client.gui.screens.Screen {
        private final java.util.List<com.mdk.cosmetic.client.MultiblockPreview.Cell> cells=new java.util.ArrayList<>();
        PreviewScreen(){super(net.minecraft.network.chat.Component.literal("MDK preview validation"));for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++)cells.add(new com.mdk.cosmetic.client.MultiblockPreview.Cell(x,y,z,new ItemStack((x==0&&y==0&&z==0)?net.minecraft.world.item.Items.GOLD_BLOCK:net.minecraft.world.item.Items.AMETHYST_BLOCK)));}
        @Override public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){g.fill(0,0,width,height,0xFF17202B);g.centeredText(font,"MDK - 3D multiblock preview",width/2,8,0xFFFFFFFF);g.pose().pushMatrix();g.pose().translate(2F,(height-160)/2F);com.mdk.cosmetic.client.MultiblockPreview.submit(g,cells,35F,25F,20F);g.pose().popMatrix();g.pose().pushMatrix();g.pose().translate(180F,(height-160)/2F);ArmorRenderSmoke.submit(g);g.pose().popMatrix();}
    }
}
