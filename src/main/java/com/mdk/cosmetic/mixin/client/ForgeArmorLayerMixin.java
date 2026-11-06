package com.mdk.cosmetic.mixin.client;
import com.mdk.cosmetic.client.ForgeArmorExtensions;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(HumanoidArmorLayer.class)
public abstract class ForgeArmorLayerMixin extends RenderLayer<HumanoidRenderState,HumanoidModel<HumanoidRenderState>> {
 protected ForgeArmorLayerMixin(RenderLayerParent<HumanoidRenderState,HumanoidModel<HumanoidRenderState>> parent){super(parent);}
 @Inject(method="renderArmorPiece",at=@At("HEAD"),cancellable=true)
 private void mdk$customArmor(PoseStack pose,SubmitNodeCollector buffers,ItemStack stack,EquipmentSlot slot,int light,HumanoidRenderState state,CallbackInfo ci){var renderer=ForgeArmorExtensions.RENDERERS.get(stack.getItem());if(renderer!=null){renderer.render(pose,buffers,stack,state,slot,light,getParentModel());ci.cancel();}}
}
