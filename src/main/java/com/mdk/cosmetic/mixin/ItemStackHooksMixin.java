package com.mdk.cosmetic.mixin;
import com.mdk.cosmetic.platform.ForgeGameplayEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(ItemStack.class) public abstract class ItemStackHooksMixin {
 @Inject(method="forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",at=@At("TAIL"))
 private void mdk$headAttributes(net.minecraft.world.entity.EquipmentSlot slot,java.util.function.BiConsumer<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>,net.minecraft.world.entity.ai.attributes.AttributeModifier> output,CallbackInfo ci){var event=new ForgeGameplayEvents.ItemAttributeModifierEvent((ItemStack)(Object)this,ItemAttributeModifiers.EMPTY);ForgeGameplayEvents.ItemAttributeModifierEvent.BUS.post(event);event.result(true).forEach(slot,output);}
 @Inject(method="forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V",at=@At("TAIL"))
 private void mdk$headAttributeTooltips(net.minecraft.world.entity.EquipmentSlotGroup slot,org.apache.commons.lang3.function.TriConsumer<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>,net.minecraft.world.entity.ai.attributes.AttributeModifier,net.minecraft.world.item.component.ItemAttributeModifiers.Display> output,CallbackInfo ci){var event=new ForgeGameplayEvents.ItemAttributeModifierEvent((ItemStack)(Object)this,ItemAttributeModifiers.EMPTY);ForgeGameplayEvents.ItemAttributeModifierEvent.BUS.post(event);event.result(true).forEach(slot,output);}
}
