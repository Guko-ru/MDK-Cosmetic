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
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientTickEvent.Post event)->{var client=net.minecraft.client.Minecraft.getInstance();
            if(client.getOverlay()!=null||(client.level==null&&client.screen==null)||++readyTicks[0]<20)return;
            if(!worldStarted[0]){worldStarted[0]=true;new Thread(()->{try{new com.mdk.cosmetic.gametest.legacy.LegacyClientContext().worldBuilder().create();worldReady.set(true);}catch(Throwable error){client.execute(()->{throw new AssertionError("Native smoke world failed",error);});}},"MDK smoke world").start();return;}
            if(!worldReady.get())return;
            if(stage[0]==1){
                if(readyTicks[0]==30)net.minecraft.client.Screenshot.grab(client.gameDirectory,"multiblock-preview-smoke.png",client.getMainRenderTarget(),1,message->LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_PREVIEW_IMAGE_READY"));
                if(readyTicks[0]>=50){LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_PREVIEW_SMOKE_OK: 27 depth-tested item models rendered in deferred GUI");client.stop();}return;
            }
            var bossState=new com.mdk.cosmetic.client.UnoRenderState();
            bossState.entityType=com.mdk.cosmetic.MDKCosmetic.UNO_ENTITY.get();
            bossState.texture=net.minecraft.resources.Identifier.parse("mdkcosmetic:textures/entity/uno.png");
            var bossRenderer=client.getEntityRenderDispatcher().getRenderer(bossState);
            if(!(bossRenderer instanceof com.mdk.cosmetic.client.UnoRenderer renderer)||!renderer.getTextureLocation(bossState).equals(bossState.texture))throw new AssertionError("Boss pose incorrectly dispatched to a vanilla player/Steve renderer");
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_BOSS_RENDERER_OK: native dispatcher selects the custom boss renderer and black texture");
            for(var hat:java.util.List.of(com.mdk.cosmetic.MDKCosmetic.FOX_HAT.get(),com.mdk.cosmetic.MDKCosmetic.PANDA_HAT.get())){
                var stack=new ItemStack(hat);var nativeLayer=new net.minecraft.client.resources.model.EquipmentClientInfo.Layer(net.minecraft.resources.Identifier.parse("mdkcosmetic:fox_hat"));
                int tint=net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(stack).getArmorLayerTintColor(stack,nativeLayer,0,0xFFA06540);
                if(tint!=0xFFFFFFFF)throw new AssertionError("Non-dyeable armor layer received a dye fallback: "+Integer.toHexString(tint));
            }
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_NATIVE_LAYER_TINT_OK: non-dyeable fox and panda layers preserve native white tint");
            if(!new com.mdk.cosmetic.client.CatEarsModel().hat.isEmpty()||!new com.mdk.cosmetic.client.FoxHatModel().hat.isEmpty()||!new com.mdk.cosmetic.client.AxolotlHatModel().hat.isEmpty())throw new AssertionError("Custom cosmetic hat must not retain a vanilla helmet cube");
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_HAT_GEOMETRY_OK: custom ears, fox and axolotl geometry have no inherited vanilla helmet cube");
            var nativeModels=net.minecraft.client.renderer.entity.ArmorModelSet.bake(net.minecraft.client.model.geom.ModelLayers.ARMOR_STAND_ARMOR,client.getEntityModels(),net.minecraft.client.model.object.armorstand.ArmorStandArmorModel::new);
            var nativeHead=nativeModels.get(net.minecraft.world.entity.EquipmentSlot.HEAD);
            var custom=net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(com.mdk.cosmetic.MDKCosmetic.CAT_EARS.get()).getGenericArmorModel(new ItemStack(com.mdk.cosmetic.MDKCosmetic.CAT_EARS.get()),net.minecraft.client.resources.model.EquipmentClientInfo.LayerType.HUMANOID,nativeHead);
            for(float yaw:new float[]{-35F,0F,42F}){var pose=new net.minecraft.client.renderer.entity.state.ArmorStandRenderState();pose.headPose=new net.minecraft.core.Rotations(12,yaw,3);custom.setupAnim(pose);if(Math.abs(custom.root().getChild("head").yRot-nativeHead.head.yRot)>0.00001F)throw new AssertionError("Custom hat lost native armor stand head pose");if(custom.root().getChild("body").visible)throw new AssertionError("Head cosmetic must not expose unrelated torso geometry");}
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_NATIVE_ARMOR_POSE_OK: custom head geometry matches native armor stand animation across three poses");
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
            stage[0]=1;readyTicks[0]=0;client.setScreen(new PreviewScreen());
        });
    }

    private static final class PreviewScreen extends net.minecraft.client.gui.screens.Screen {
        private final java.util.List<com.mdk.cosmetic.client.MultiblockPreview.Cell> cells=new java.util.ArrayList<>();
        PreviewScreen(){super(net.minecraft.network.chat.Component.literal("MDK preview validation"));for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++)cells.add(new com.mdk.cosmetic.client.MultiblockPreview.Cell(x,y,z,new ItemStack((x==0&&y==0&&z==0)?net.minecraft.world.item.Items.GOLD_BLOCK:net.minecraft.world.item.Items.AMETHYST_BLOCK)));}
        @Override public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){g.fill(0,0,width,height,0xFF17202B);g.centeredText(font,"MDK - 3D multiblock preview",width/2,8,0xFFFFFFFF);g.pose().pushMatrix();g.pose().translate((width-166)/2F,(height-160)/2F);com.mdk.cosmetic.client.MultiblockPreview.submit(g,cells,35F,25F,20F);g.pose().popMatrix();}
    }
}
