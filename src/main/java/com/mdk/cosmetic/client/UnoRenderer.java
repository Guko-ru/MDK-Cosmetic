package com.mdk.cosmetic.client;
import com.mdk.cosmetic.UnoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
/** Keeps original combat keyframes, copied player skin, equipped armor and both arm-width models. */
public final class UnoRenderer extends MobRenderer<UnoEntity,PlayerRenderState,UnoModel> {
 private static final ResourceLocation ORIGINAL=ResourceLocation.parse("mdkcosmetic:textures/entity/uno.png");
 private final UnoModel wideModel,slimModel;
 public UnoRenderer(EntityRendererProvider.Context context){
  super(context,new UnoModel(context.bakeLayer(ModelLayers.PLAYER),false),.5F);wideModel=model;slimModel=new UnoModel(context.bakeLayer(ModelLayers.PLAYER_SLIM),true);
  addLayer(new HumanoidArmorLayer<>(this,new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),context.getEquipmentRenderer()));
  addLayer(new ItemInHandLayer<>(this,context.getItemRenderer()));addLayer(new CollectorHelmetHeadLayer<>(this));
 }
 @Override public UnoRenderState createRenderState(){return new UnoRenderState();}
 @Override public void extractRenderState(UnoEntity entity,PlayerRenderState base,float partial){
  super.extractRenderState(entity,base,partial);HumanoidMobRenderer.extractHumanoidRenderState(entity,base,partial);
  var state=(UnoRenderState)base;state.action=entity.getAction();state.actionSeconds=entity.actionSeconds();state.texture=ORIGINAL;state.slim=false;
  state.showHat=state.showJacket=state.showLeftPants=state.showRightPants=state.showLeftSleeve=state.showRightSleeve=true;state.id=entity.getId();
  var connection=Minecraft.getInstance().getConnection();
  if(entity.isSecondPhase()&&connection!=null){var skin=entity.getMimicTarget().map(connection::getPlayerInfo).map(net.minecraft.client.multiplayer.PlayerInfo::getSkin).orElse(null);if(skin!=null){state.skin=skin;state.texture=skin.texture();state.slim=skin.model()==PlayerSkin.Model.SLIM;}}
 }
 @Override public void render(PlayerRenderState base,PoseStack pose,MultiBufferSource buffers,int light){model=((UnoRenderState)base).slim?slimModel:wideModel;super.render(base,pose,buffers,light);}
 @Override public ResourceLocation getTextureLocation(PlayerRenderState state){return ((UnoRenderState)state).texture;}
}
