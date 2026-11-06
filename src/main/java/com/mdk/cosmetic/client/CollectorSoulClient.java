package com.mdk.cosmetic.client;

import com.mdk.cosmetic.MDKCosmetic;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid=MDKCosmetic.MOD_ID,value=Dist.CLIENT)
public final class CollectorSoulClient {
    private CollectorSoulClient() {}
    @net.neoforged.bus.api.SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CollectorSoulModel.LAYER,CollectorSoulModel::createBodyLayer);
    }
    @net.neoforged.bus.api.SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MDKCosmetic.COLLECTOR_SOUL_ENTITY.get(), CollectorSoulRenderer::new);
        event.registerEntityRenderer(MDKCosmetic.UNO_ENTITY.get(), UnoRenderer::new);
        event.registerEntityRenderer(MDKCosmetic.COLLECTOR_HEART_ENTITY.get(),
                CollectorHeartRenderer::new);
    }
}
