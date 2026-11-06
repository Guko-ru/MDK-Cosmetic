package com.mdk.cosmetic.gametest;



import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.slf4j.LoggerFactory;

/** Client-only startup check, supplied solely by the separate integration-test mod. */
public final class ClientStartupSmoke  {
    public static void initialize(){
        if(!Boolean.getBoolean("mdkcosmetic.clientSmoke"))return;
        int[] readyTicks={0};
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientTickEvent.Post event)->{var client=net.minecraft.client.Minecraft.getInstance();
            if(client.getOverlay()!=null||client.screen==null||++readyTicks[0]<20)return;
            for(var hat:java.util.List.of(com.mdk.cosmetic.MDKCosmetic.FOX_HAT.get(),com.mdk.cosmetic.MDKCosmetic.PANDA_HAT.get())){
                var stack=new ItemStack(hat);var nativeLayer=new net.minecraft.client.resources.model.EquipmentClientInfo.Layer(net.minecraft.resources.ResourceLocation.parse("mdkcosmetic:fox_hat"));
                int tint=net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(stack).getArmorLayerTintColor(stack,nativeLayer,0,0xFFA06540);
                if(tint!=0xFFFFFFFF)throw new AssertionError("Non-dyeable armor layer received a dye fallback: "+Integer.toHexString(tint));
            }
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_NATIVE_LAYER_TINT_OK: non-dyeable fox and panda layers preserve native white tint");
            if(!new com.mdk.cosmetic.client.CatEarsModel().hat.isEmpty()||!new com.mdk.cosmetic.client.FoxHatModel().hat.isEmpty()||!new com.mdk.cosmetic.client.AxolotlHatModel().hat.isEmpty())throw new AssertionError("Custom cosmetic hat must not retain a vanilla helmet cube");
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_HAT_GEOMETRY_OK: custom ears, fox and axolotl geometry have no inherited vanilla helmet cube");
            int checked=0;
            var missing=client.getModelManager().getMissingModel().getParticleIcon();
            for(var item:BuiltInRegistries.ITEM){
                var id=BuiltInRegistries.ITEM.getKey(item);
                if(!id.getNamespace().equals("mdkcosmetic"))continue;
                var model=new net.minecraft.client.renderer.item.ItemStackRenderState();
                client.getItemModelResolver().updateForTopItem(model,new ItemStack(item),net.minecraft.world.item.ItemDisplayContext.GUI,false,null,null,0);
                if(model.isEmpty()||model.pickParticleIcon(net.minecraft.util.RandomSource.create(0))==missing)throw new AssertionError("Missing baked item model: "+id);
                checked++;
            }
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_CLIENT_SMOKE_OK: {} item models, entity renderers, armor models and client mixins initialized",checked);
            client.stop();
        });
    }
}
