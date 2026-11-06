package com.mdk.cosmetic.client;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;
/** Renders a real 3D scene inside the deferred GUI pipeline, including depth-tested item models. */
public final class MultiblockPreview {
 public record Cell(int x,int y,int z,ItemStack item) {}
 public static void register(net.minecraftforge.client.event.RegisterPictureInPictureRendererEvent event){event.register(new Renderer());}
 public static void submit(GuiGraphicsExtractor graphics,List<Cell> cells,float yaw,float elevation,float unit){graphics.getRenderState().addPicturesInPictureState(new State(List.copyOf(cells),yaw,elevation,unit,new Matrix3x2f(graphics.pose()),((com.mdk.cosmetic.mixin.client.GuiScissorStackAccessor)(Object)graphics.getScissorStack()).mdk$peek()));}
 public record State(List<Cell> cells,float yaw,float elevation,float unit,Matrix3x2f pose,ScreenRectangle scissorArea) implements PictureInPictureRenderState {
  public int x0(){return 0;}public int y0(){return 0;}public int x1(){return 166;}public int y1(){return 160;}public float scale(){return 1F;}
  public ScreenRectangle bounds(){var bounds=new ScreenRectangle(0,0,166,160).transformMaxBounds(pose);return scissorArea==null?bounds:bounds.intersection(scissorArea);}
 }
 public static final class Renderer extends PictureInPictureRenderer<State> {
  
  @Override public Class<State> getRenderStateClass(){return State.class;}
  @Override protected float getTranslateY(int height,int guiScale){return height/2F;}
  @Override protected String getTextureLabel(){return "MDK multiblock preview";}
  @Override protected void renderToTexture(State state,PoseStack pose,net.minecraft.client.renderer.SubmitNodeCollector queue){
   var client=Minecraft.getInstance();client.gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_3D);
   pose.scale(1,1,-1);pose.translate(0,-3,0);pose.mulPose(Axis.XP.rotationDegrees(state.elevation()));pose.mulPose(Axis.YP.rotationDegrees(state.yaw()));pose.scale(state.unit(),-state.unit(),state.unit());
   
   for(var cell:state.cells()){pose.pushPose();try{pose.translate(cell.x(),cell.y(),cell.z());ModelSubmission.item(cell.item(),ItemDisplayContext.NONE,0x00F000F0,OverlayTexture.NO_OVERLAY,pose,queue,0,0);}finally{pose.popPose();}}
   
  }
 }
}
