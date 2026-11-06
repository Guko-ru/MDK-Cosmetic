package com.mdk.cosmetic.mixin.client;
import com.mdk.cosmetic.client.*;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntityRenderer.class) public abstract class EquipmentExtractionMixin {
 @Inject(method="extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",at=@At("TAIL"))
 private void mdk$equipment(LivingEntity entity,LivingEntityRenderState state,float partial,CallbackInfo ci){((EquipmentRenderData)state).mdk$setEquipment(entity.getItemBySlot(EquipmentSlot.HEAD).copy(),entity instanceof Player?CollectorEquipmentVisuals.clientBackpack(entity.getUUID()):ItemStack.EMPTY,entity.getId());}
}
