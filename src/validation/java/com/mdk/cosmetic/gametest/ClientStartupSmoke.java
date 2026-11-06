package com.mdk.cosmetic.gametest;



import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.slf4j.LoggerFactory;

/** Client-only startup check, supplied solely by the separate integration-test mod. */
public final class ClientStartupSmoke  {
    public static void initialize(){
        if(!Boolean.getBoolean("mdkcosmetic.clientSmoke"))return;
        int[] readyTicks={0},stage={0};
        boolean[] worldStarted={false};var worldReady=new java.util.concurrent.atomic.AtomicBoolean();
        net.minecraftforge.event.TickEvent.ClientTickEvent.Post.BUS.addListener((net.minecraftforge.event.TickEvent.ClientTickEvent.Post event)->{var client=net.minecraft.client.Minecraft.getInstance();
            if(client.gui.overlay()!=null||(client.level==null&&client.gui.screen()==null)||++readyTicks[0]<20)return;
            if(!worldStarted[0]){worldStarted[0]=true;new Thread(()->{try{new com.mdk.cosmetic.gametest.legacy.LegacyClientContext().worldBuilder().create();worldReady.set(true);}catch(Throwable error){client.execute(()->{throw new AssertionError("Native smoke world failed",error);});}},"MDK smoke world").start();return;}
            if(!worldReady.get())return;
            if(stage[0]==1){
                if(readyTicks[0]==30)net.minecraft.client.Screenshot.grab(client.gameDirectory,"multiblock-preview-smoke.png",client.gameRenderer.mainRenderTarget(),1,message->LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_PREVIEW_IMAGE_READY"));
                if(readyTicks[0]>=50){LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_PREVIEW_SMOKE_OK: 27 depth-tested item models rendered in deferred GUI");client.stop();}return;
            }
            var bossState=new com.mdk.cosmetic.client.UnoRenderState();
            bossState.entityType=com.mdk.cosmetic.MDKCosmetic.UNO_ENTITY.get();
            bossState.texture=net.minecraft.resources.Identifier.parse("mdkcosmetic:textures/entity/uno.png");
            var bossRenderer=client.getEntityRenderDispatcher().getRenderer(bossState);
            if(!(bossRenderer instanceof com.mdk.cosmetic.client.UnoRenderer renderer)||!renderer.getTextureLocation(bossState).equals(bossState.texture))throw new AssertionError("Boss pose incorrectly dispatched to a vanilla player/Steve renderer");
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_BOSS_RENDERER_OK: native dispatcher selects the custom boss renderer and black texture");
            for(var hat:java.util.List.of(com.mdk.cosmetic.MDKCosmetic.FOX_HAT.get(),com.mdk.cosmetic.MDKCosmetic.PANDA_HAT.get())){
                var stack=new ItemStack(hat);var nativeLayer=new com.mdk.cosmetic.platform.ArmorDefinition.Layer(net.minecraft.resources.Identifier.parse("mdkcosmetic:fox_hat"));
                int tint;
                try{
                    var field=com.mdk.cosmetic.client.ForgeArmorRenderer.class.getDeclaredField("appearance");field.setAccessible(true);
                    var appearance=(com.mdk.cosmetic.client.ArmorAppearance)field.get(com.mdk.cosmetic.client.ForgeArmorExtensions.RENDERERS.get(hat));
                    tint=appearance.getArmorLayerTintColor(stack,new net.minecraft.client.renderer.entity.state.HumanoidRenderState(),nativeLayer,0,0xFFA06540);
                }catch(ReflectiveOperationException error){throw new AssertionError(error);}
                if(tint!=0xFFFFFFFF)throw new AssertionError("Non-dyeable armor layer received a dye fallback: "+Integer.toHexString(tint));
            }
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_NATIVE_LAYER_TINT_OK: non-dyeable fox and panda layers preserve native white tint");
            if(!new com.mdk.cosmetic.client.CatEarsModel().hat.isEmpty()||!new com.mdk.cosmetic.client.FoxHatModel().hat.isEmpty()||!new com.mdk.cosmetic.client.AxolotlHatModel().hat.isEmpty())throw new AssertionError("Custom cosmetic hat must not retain a vanilla helmet cube");
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_HAT_GEOMETRY_OK: custom ears, fox and axolotl geometry have no inherited vanilla helmet cube");
            var hat=new com.mdk.cosmetic.client.CatEarsModel();com.mdk.cosmetic.client.ModelVisibility.set(hat,false);hat.head.visible=true;
            var snapshots=new com.mdk.cosmetic.client.ArmorSnapshotModel(hat.root());
            var frozen=new java.util.ArrayList<com.mdk.cosmetic.client.ArmorSnapshotModel.Snapshot>();
            float[] yaws={-.6F,0F,.9F};for(float yaw:yaws){hat.head.yRot=yaw;frozen.add(snapshots.capture());}
            hat.head.yRot=4F;hat.body.visible=true;
            for(int i=0;i<yaws.length;i++){snapshots.setupAnim(frozen.get(i));if(Math.abs(hat.head.yRot-yaws[i])>.00001F||hat.body.visible)throw new AssertionError("Deferred hat draw lost its own pose or visibility");}
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_FORGE_ARMOR_SNAPSHOT_OK: three queued hat poses restore independent geometry and visibility");
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
            stage[0]=1;readyTicks[0]=0;client.gui.setScreen(new PreviewScreen());
        });
    }

    private static final class PreviewScreen extends net.minecraft.client.gui.screens.Screen {
        private final java.util.List<com.mdk.cosmetic.client.MultiblockPreview.Cell> cells=new java.util.ArrayList<>();
        PreviewScreen(){super(net.minecraft.network.chat.Component.literal("MDK preview validation"));for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++)cells.add(new com.mdk.cosmetic.client.MultiblockPreview.Cell(x,y,z,new ItemStack((x==0&&y==0&&z==0)?net.minecraft.world.item.Items.GOLD_BLOCK:net.minecraft.world.item.Items.AMETHYST_BLOCK)));}
        @Override public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){g.fill(0,0,width,height,0xFF17202B);g.centeredText(font,"MDK - 3D multiblock preview",width/2,8,0xFFFFFFFF);g.pose().pushMatrix();g.pose().translate((width-166)/2F,(height-160)/2F);com.mdk.cosmetic.client.MultiblockPreview.submit(g,cells,35F,25F,20F);g.pose().popMatrix();}
    }
}
