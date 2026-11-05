package com.mdk.cosmetic.client;

import com.mdk.cosmetic.MDKCosmetic;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Wears the exact 3D item model instead of a lossy baked-armor UV/rotation conversion. */
public final class CollectorHelmetHeadLayer<T extends LivingEntity, M extends EntityModel<T> & HeadedModel>
        extends RenderLayer<T, M> {
    public CollectorHelmetHeadLayer(RenderLayerParent<T, M> parent) { super(parent); }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int packedLight, T entity,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
            float netHeadYaw, float headPitch) {
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!helmet.is(MDKCosmetic.COLLECTOR_HELMET.get())) return;
        pose.pushPose();
        getParentModel().getHead().translateAndRotate(pose);
        // Same transform as vanilla items rendered in the head slot. The item JSON
        // already contains the HEAD transform for this specific 3D helmet.
        CustomHeadLayer.translateToHead(pose, false);
        Minecraft.getInstance().getItemRenderer().renderStatic(entity, helmet,
                ItemDisplayContext.HEAD, false, pose, buffers, entity.level(),
                packedLight, OverlayTexture.NO_OVERLAY, entity.getId());
        pose.popPose();
    }
}
