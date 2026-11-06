package com.mdk.cosmetic.client;
import com.mdk.cosmetic.UnoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.resources.Identifier;
/** Keeps original combat keyframes, copied player skin, equipped armor and both arm-width models. */
public final class UnoRenderer extends MobRenderer<UnoEntity,AvatarRenderState,UnoModel> {
 private static final Identifier ORIGINAL=Identifier.parse("mdkcosmetic:textures/entity/uno.png");
 private final UnoModel wideModel,slimModel;
 private final net.minecraft.client.renderer.item.ItemModelResolver itemModels;
 public UnoRenderer(EntityRendererProvider.Context context){
  super(context,new UnoModel(context.bakeLayer(ModelLayers.PLAYER),false),.5F);itemModels=context.getItemModelResolver();wideModel=model;slimModel=new UnoModel(context.bakeLayer(ModelLayers.PLAYER_SLIM),true);
  addLayer(new HumanoidArmorLayer<>(this,net.minecraft.client.renderer.entity.ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR,context.getModelSet(),part->new HumanoidModel<AvatarRenderState>(part)),context.getEquipmentRenderer()));
  addLayer(new ItemInHandLayer<>(this));addLayer(new CollectorHelmetHeadLayer<>(this));
 }
 @Override public UnoRenderState createRenderState(){return new UnoRenderState();}
 @Override public void extractRenderState(UnoEntity entity,AvatarRenderState base,float partial){
  super.extractRenderState(entity,base,partial);HumanoidMobRenderer.extractHumanoidRenderState(entity,base,partial,itemModels);
  var state=(UnoRenderState)base;state.rightArmPose=state.leftArmPose=HumanoidModel.ArmPose.ITEM;state.action=entity.getAction();state.actionSeconds=entity.actionSeconds();state.texture=ORIGINAL;state.slim=false;
  state.showHat=state.showJacket=state.showLeftPants=state.showRightPants=state.showLeftSleeve=state.showRightSleeve=true;state.id=entity.getId();
  var connection=Minecraft.getInstance().getConnection();
  if(entity.isSecondPhase()&&connection!=null){var skin=entity.getMimicTarget().map(connection::getPlayerInfo).map(net.minecraft.client.multiplayer.PlayerInfo::getSkin).orElse(null);if(skin!=null){state.skin=skin;state.texture=skin.body().texturePath();state.slim=skin.model()==net.minecraft.world.entity.player.PlayerModelType.SLIM;}}
 }
 @Override public void submit(AvatarRenderState base,PoseStack pose,net.minecraft.client.renderer.SubmitNodeCollector buffers,net.minecraft.client.renderer.state.level.CameraRenderState camera){model=((UnoRenderState)base).slim?slimModel:wideModel;super.submit(base,pose,buffers,camera);}
 @Override public Identifier getTextureLocation(AvatarRenderState state){return ((UnoRenderState)state).texture;}
}
