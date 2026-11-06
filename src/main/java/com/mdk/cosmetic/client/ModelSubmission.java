package com.mdk.cosmetic.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.blaze3d.vertex.PoseStack;
public final class ModelSubmission {
 public static void item(ItemStack stack,ItemDisplayContext display,int light,int overlay,PoseStack pose,SubmitNodeCollector queue,int seed,int outline){var client=Minecraft.getInstance();var state=new ItemStackRenderState();client.getItemModelResolver().updateForTopItem(state,stack,display,client.level,null,seed);state.submit(pose,queue,light,overlay,outline);}
}
