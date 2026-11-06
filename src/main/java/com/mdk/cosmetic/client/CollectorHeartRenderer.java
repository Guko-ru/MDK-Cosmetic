package com.mdk.cosmetic.client;

import com.mdk.cosmetic.MDKCosmetic;
import net.minecraft.client.renderer.entity.ElderGuardianRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Guardian;

/** Distinct original texture on Minecraft's underwater guardian model and animation rig. */
public final class CollectorHeartRenderer extends ElderGuardianRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            MDKCosmetic.MOD_ID, "textures/entity/collector_heart.png");
    public CollectorHeartRenderer(EntityRendererProvider.Context ctx) { super(ctx); }
    @Override public ResourceLocation getTextureLocation(Guardian entity) { return TEXTURE; }
}
