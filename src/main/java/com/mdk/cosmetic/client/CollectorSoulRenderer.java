package com.mdk.cosmetic.client;

import com.mdk.cosmetic.CollectorSoulEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Full-bright 3D orb with animated concentric fragments, no shadow. */
public final class CollectorSoulRenderer extends MobRenderer<CollectorSoulEntity,net.minecraft.client.renderer.entity.state.LivingEntityRenderState,CollectorSoulModel> {
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(
            "mdkcosmetic","textures/entity/collector_soul.png");
    public CollectorSoulRenderer(EntityRendererProvider.Context context) {
        super(context,new CollectorSoulModel(context.bakeLayer(CollectorSoulModel.LAYER)),0.0F);
    }
    // Center the visible floating soul on its 0.95-block hitbox.
    @Override public Vec3 getRenderOffset(net.minecraft.client.renderer.entity.state.LivingEntityRenderState state) {
        return super.getRenderOffset(state).add(0.0,-0.22,0.0);
    }
    @Override public ResourceLocation getTextureLocation(net.minecraft.client.renderer.entity.state.LivingEntityRenderState state) { return TEXTURE; }
    @Override protected int getBlockLightLevel(CollectorSoulEntity entity,BlockPos pos) { return 15; }
    @Override public net.minecraft.client.renderer.entity.state.LivingEntityRenderState createRenderState(){return new net.minecraft.client.renderer.entity.state.LivingEntityRenderState();}
}
