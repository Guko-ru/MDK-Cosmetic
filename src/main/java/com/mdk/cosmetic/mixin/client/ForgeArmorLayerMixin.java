package com.mdk.cosmetic.mixin.client;
import com.mdk.cosmetic.client.ForgeArmorExtensions;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(HumanoidArmorLayer.class)
public abstract class ForgeArmorLayerMixin extends net.minecraft.client.renderer.entity.layers.RenderLayer<LivingEntity,HumanoidModel<LivingEntity>> {
 protected ForgeArmorLayerMixin(net.minecraft.client.renderer.entity.RenderLayerParent<LivingEntity,HumanoidModel<LivingEntity>> parent){super(parent);}
 @Inject(method="renderArmorPiece",at=@At("HEAD"),cancellable=true)
 @SuppressWarnings("unchecked") private void mdk$customArmor(PoseStack pose,MultiBufferSource buffers,LivingEntity entity,EquipmentSlot slot,int light,HumanoidModel<?> context,CallbackInfo ci){
  var stack=entity.getItemBySlot(slot);var renderer=ForgeArmorExtensions.RENDERERS.get(stack.getItem());
  if(renderer!=null){renderer.render(pose,buffers,stack,entity,slot,light,getParentModel());ci.cancel();}
 }
}
