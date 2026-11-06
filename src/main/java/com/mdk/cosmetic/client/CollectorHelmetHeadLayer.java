package com.mdk.cosmetic.client;
import com.mdk.cosmetic.MDKCosmetic;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
public final class CollectorHelmetHeadLayer<S extends LivingEntityRenderState,M extends EntityModel<S> & HeadedModel> extends RenderLayer<S,M> {
 public CollectorHelmetHeadLayer(RenderLayerParent<S,M> parent){super(parent);}
 @Override public void submit(PoseStack pose,SubmitNodeCollector buffers,int light,S state,float yaw,float pitch){
  var data=(EquipmentRenderData)state;var helmet=data.mdk$getHelmet();if(!helmet.is(MDKCosmetic.COLLECTOR_HELMET.get()))return;
  pose.pushPose();getParentModel().getHead().translateAndRotate(pose);CustomHeadLayer.translateToHead(pose,CustomHeadLayer.Transforms.DEFAULT);
  ModelSubmission.item(helmet,ItemDisplayContext.HEAD,light,OverlayTexture.NO_OVERLAY,pose,buffers,data.mdk$getEntityId(),state.outlineColor);pose.popPose();
 }
}
