package com.mdk.cosmetic.client;

import com.mdk.cosmetic.UnoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

/** Vanilla Minecraft player model + actual online player's skin and visible armor in phase II. */
public final class UnoRenderer extends MobRenderer<UnoEntity, UnoModel> {
    private static final ResourceLocation ORIGINAL =
            ResourceLocation.fromNamespaceAndPath("mdkcosmetic", "textures/entity/uno.png");
    private final UnoModel wideModel;
    private final UnoModel slimModel;

    public UnoRenderer(EntityRendererProvider.Context context) {
        super(context, new UnoModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.wideModel = this.model;
        this.slimModel = new UnoModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new CollectorHelmetHeadLayer<>(this));
    }

    private PlayerSkin copiedSkin(UnoEntity uno) {
        if (!uno.isSecondPhase()) return null;
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) return null;
        return uno.getMimicTarget().map(connection::getPlayerInfo)
                .map(PlayerInfo::getSkin).orElse(null);
    }

    @Override
    public void render(UnoEntity entity, float entityYaw, float partialTicks,
            PoseStack stack, MultiBufferSource buffers, int packedLight) {
        PlayerSkin skin = copiedSkin(entity);
        this.model = skin != null && skin.model() == PlayerSkin.Model.SLIM ? slimModel : wideModel;
        super.render(entity, entityYaw, partialTicks, stack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(UnoEntity entity) {
        PlayerSkin skin = copiedSkin(entity);
        return skin != null ? skin.texture() : ORIGINAL;
    }
}
