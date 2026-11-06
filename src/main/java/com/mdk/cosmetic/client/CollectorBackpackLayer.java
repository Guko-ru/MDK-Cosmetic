package com.mdk.cosmetic.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
public final class CollectorBackpackLayer extends RenderLayer<AvatarRenderState,PlayerModel> {
 public CollectorBackpackLayer(RenderLayerParent<AvatarRenderState,PlayerModel> parent){super(parent);}
 @Override public void submit(PoseStack pose,SubmitNodeCollector buffers,int light,AvatarRenderState state,float yaw,float pitch){
  var data=(EquipmentRenderData)state;var backpack=data.mdk$getBackpack();if(backpack.isEmpty())return;
  pose.pushPose();getParentModel().body.translateAndRotate(pose);pose.translate(0,.12,.29);pose.scale(.65F,.75F,.44F);
  ModelSubmission.item(backpack,ItemDisplayContext.NONE,light,OverlayTexture.NO_OVERLAY,pose,buffers,data.mdk$getEntityId(),state.outlineColor);pose.popPose();
 }
}
