package com.mdk.cosmetic.client;

import com.mdk.cosmetic.MDKCosmetic;
import com.mdk.cosmetic.client.ClientEvents.EntityRenderersEvent;

public final class CollectorSoulClient {
    private CollectorSoulClient() {}
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CollectorSoulModel.LAYER,CollectorSoulModel::createBodyLayer);
    }
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MDKCosmetic.COLLECTOR_SOUL_ENTITY.get(), CollectorSoulRenderer::new);
        event.registerEntityRenderer(MDKCosmetic.UNO_ENTITY.get(), UnoRenderer::new);
        event.registerEntityRenderer(MDKCosmetic.COLLECTOR_HEART_ENTITY.get(),
                CollectorHeartRenderer::new);
    }
}
