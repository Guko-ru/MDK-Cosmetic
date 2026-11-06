package com.mdk.cosmetic.mixin;
import com.mdk.cosmetic.platform.Events;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(ItemStack.class) public abstract class ItemStackHooksMixin {
 @Inject(method="onCraftedBy",at=@At("TAIL")) private void mdk$craft(Level level,Player player,int count,CallbackInfo ci){if(!level.isClientSide()){var stack=((ItemStack)(Object)this).copy();stack.setCount(count);Events.dispatch(new Events.PlayerEvent.ItemCraftedEvent(player,stack));}}
 @Inject(method="forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",at=@At("TAIL"))
 private void mdk$headAttributes(net.minecraft.world.entity.EquipmentSlot slot,java.util.function.BiConsumer<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>,net.minecraft.world.entity.ai.attributes.AttributeModifier> output,CallbackInfo ci){var event=new Events.ItemAttributeModifierEvent((ItemStack)(Object)this,ItemAttributeModifiers.EMPTY);Events.dispatch(event);event.result(true).forEach(slot,output);}
 @Inject(method="forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V",at=@At("TAIL"))
 private void mdk$headAttributeTooltips(net.minecraft.world.entity.EquipmentSlotGroup slot,java.util.function.BiConsumer<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>,net.minecraft.world.entity.ai.attributes.AttributeModifier> output,CallbackInfo ci){var event=new Events.ItemAttributeModifierEvent((ItemStack)(Object)this,ItemAttributeModifiers.EMPTY);Events.dispatch(event);event.result(true).forEach(slot,output);}
}
