package com.mdk.cosmetic.client;

import com.mdk.cosmetic.CollectorBackpackItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Two original pack models appear only when equipped in the player equipment slot. */
public final class CollectorBackpackLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
    public CollectorBackpackLayer(RenderLayerParent<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> parent){super(parent);}
    @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,AbstractClientPlayer player,
            float limbSwing,float limbSwingAmount,float partial,float age,float headYaw,float headPitch){
        int type=CollectorEquipmentVisuals.get(player.getUUID());
        ItemStack backpack=type==1 ? new ItemStack(com.mdk.cosmetic.MDKCosmetic.COLLECTOR_BACKPACK_BASIC.get())
                : type==2 ? new ItemStack(com.mdk.cosmetic.MDKCosmetic.COLLECTOR_BACKPACK_REINFORCED.get())
                : ItemStack.EMPTY;
        if(backpack.isEmpty())return;
        pose.pushPose();
        getParentModel().body.translateAndRotate(pose);
        pose.translate(0.0D,0.12D,0.29D);
        pose.scale(0.65F,0.75F,0.44F);
        Minecraft.getInstance().getItemRenderer().renderStatic(player,backpack,ItemDisplayContext.NONE,
                false,pose,buffers,player.level(),light,OverlayTexture.NO_OVERLAY,player.getId());
        pose.popPose();
    }
}
