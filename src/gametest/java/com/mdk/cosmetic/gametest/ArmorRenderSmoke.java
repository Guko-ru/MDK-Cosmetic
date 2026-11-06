package com.mdk.cosmetic.gametest;
import com.mdk.cosmetic.MDKCosmetic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.Lighting;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.fabricmc.fabric.impl.client.rendering.ArmorRendererRegistryImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
/** Exercises the production armor renderers with three actors before a single deferred flush. */
public final class ArmorRenderSmoke {
 static int frames;
 static void initialize(){PictureInPictureRendererRegistry.register(ctx->new Renderer());}
 static void submit(GuiGraphicsExtractor g){g.guiRenderState.addPicturesInPictureState(new State(new Matrix3x2f(g.pose()),g.scissorStack.peek()));}
 record State(Matrix3x2f pose,ScreenRectangle scissorArea) implements PictureInPictureRenderState {
  public int x0(){return 0;}public int y0(){return 0;}public int x1(){return 240;}public int y1(){return 160;}public float scale(){return 30F;}
  public ScreenRectangle bounds(){var bounds=new ScreenRectangle(0,0,240,160).transformMaxBounds(pose);return scissorArea==null?bounds:bounds.intersection(scissorArea);}
 }
 static final class Renderer extends PictureInPictureRenderer<State> {
  HumanoidModel<HumanoidRenderState> context;
  
  @Override public Class<State> getRenderStateClass(){return State.class;}
  @Override protected float getTranslateY(int height,int guiScale){return height/2F-15F*guiScale;}
  @Override protected String getTextureLabel(){return "MDK armor validation";}
  @Override protected void renderToTexture(State state,PoseStack pose,net.minecraft.client.renderer.SubmitNodeCollector queue){
   var client=Minecraft.getInstance();if(context==null)context=new HumanoidModel<>(client.getEntityModels().bakeLayer(ModelLayers.PLAYER));
   client.gameRenderer.lighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
   var hats=java.util.List.of(MDKCosmetic.FOX_HAT.get(),MDKCosmetic.CAT_EARS.get(),MDKCosmetic.SAKURA_THINKER.get());var colors=new int[]{0xE95855,0x35BCD4,0xB469E4};
   for(int actor=0;actor<3;actor++){
    pose.pushPose();pose.translate((actor-1)*1.8F,0,0);
    var view=new HumanoidRenderState();view.speedValue=1F;view.ageInTicks=30F;view.yRot=(actor-1)*12F;view.walkAnimationPos=actor*1.2F;view.walkAnimationSpeed=.12F;context.setupAnim(view);
    queue.order(0).submitModel(context,view,pose,RenderTypes.entityCutout(Identifier.parse("mdkcosmetic:textures/entity/uno.png")),0x00F000F0,OverlayTexture.NO_OVERLAY,-1,null,0,null);
    var pieces=java.util.List.of(hats.get(actor),MDKCosmetic.COLLECTOR_CHESTPLATE.get(),MDKCosmetic.COLLECTOR_LEGGINGS.get(),MDKCosmetic.COLLECTOR_BOOTS.get());var slots=new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    for(int i=0;i<pieces.size();i++){var stack=new ItemStack(pieces.get(i));stack.set(DataComponents.DYED_COLOR,new DyedItemColor(colors[actor]));if(actor==1)stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE,true);var renderer=ArmorRendererRegistryImpl.get(stack.getItem());if(renderer==null)throw new AssertionError("Missing production armor renderer: "+stack);renderer.render(pose,queue,stack,view,slots[i],0x00F000F0,context);}
    pose.popPose();
   }
   frames++;
  }
 }
}
