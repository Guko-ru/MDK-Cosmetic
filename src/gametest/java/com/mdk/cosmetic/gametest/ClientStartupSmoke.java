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
        int[] readyTicks={0};
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            if(client.getOverlay()!=null||client.screen==null||++readyTicks[0]<20)return;
            int checked=0;
            var missing=client.getModelManager().getMissingModel();
            for(var item:BuiltInRegistries.ITEM){
                var id=BuiltInRegistries.ITEM.getKey(item);
                if(!id.getNamespace().equals("mdkcosmetic"))continue;
                var model=client.getItemRenderer().getModel(new ItemStack(item),null,null,0);
                if(model==missing)throw new AssertionError("Missing baked item model: "+id);
                checked++;
            }
            LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_CLIENT_SMOKE_OK: {} item models, entity renderers, armor models and client mixins initialized",checked);
            client.stop();
        });
    }
}
