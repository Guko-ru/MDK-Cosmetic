package com.mdk.cosmetic.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
public final class CollectorBackpackLayer extends RenderLayer<PlayerRenderState,PlayerModel> {
 public CollectorBackpackLayer(RenderLayerParent<PlayerRenderState,PlayerModel> parent){super(parent);}
 @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,PlayerRenderState state,float yaw,float pitch){
  var data=(EquipmentRenderData)state;var backpack=data.mdk$getBackpack();if(backpack.isEmpty())return;
  pose.pushPose();getParentModel().body.translateAndRotate(pose);pose.translate(0,.12,.29);pose.scale(.65F,.75F,.44F);
  Minecraft.getInstance().getItemRenderer().renderStatic(backpack,ItemDisplayContext.NONE,light,OverlayTexture.NO_OVERLAY,pose,buffers,Minecraft.getInstance().level,data.mdk$getEntityId());pose.popPose();
 }
}
