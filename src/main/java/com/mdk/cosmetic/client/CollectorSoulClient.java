package com.mdk.cosmetic.client;

import com.mdk.cosmetic.MDKCosmetic;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid=MDKCosmetic.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class CollectorSoulClient {
    private CollectorSoulClient() {}
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CollectorSoulModel.LAYER,CollectorSoulModel::createBodyLayer);
    }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(MDKCosmetic.COLLECTOR_SOUL_ENTITY.get(), CollectorSoulRenderer::new);
        event.registerEntityRenderer(MDKCosmetic.UNO_ENTITY.get(), UnoRenderer::new);
        event.registerEntityRenderer(MDKCosmetic.COLLECTOR_HEART_ENTITY.get(),
                CollectorHeartRenderer::new);
    }
}
