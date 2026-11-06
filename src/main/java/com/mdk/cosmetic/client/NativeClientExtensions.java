package com.mdk.cosmetic.client;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.EquipmentModel;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import com.mdk.cosmetic.platform.CosmeticArmorItem;
public final class NativeClientExtensions {
 private final RegisterClientExtensionsEvent event;
 NativeClientExtensions(RegisterClientExtensionsEvent event){this.event=event;}
 void registerItem(ArmorAppearance appearance,Item...items){event.registerItem(new IClientItemExtensions(){
  @Override @SuppressWarnings({"rawtypes","unchecked"}) public Model getHumanoidArmorModel(ItemStack stack,EquipmentModel.LayerType type,Model original){
   if(!(original instanceof HumanoidModel context))return original;
   var equipped=stack.get(DataComponents.EQUIPPABLE);var slot=equipped==null?EquipmentSlot.HEAD:equipped.slot();
   var selected=appearance.getHumanoidArmorModel(new HumanoidRenderState(),stack,slot,context);
   if(selected!=context)context.copyPropertiesTo(selected);return selected;
  }
  @Override public int getDefaultDyeColor(ItemStack stack){return appearance.getDefaultDyeColor(stack);}
  @Override public int getArmorLayerTintColor(ItemStack stack,EquipmentModel.Layer layer,int index,int fallback){
   if(!(stack.getItem() instanceof CosmeticArmorItem armor))return fallback;
   var layers=armor.getMaterial().layers();return appearance.getArmorLayerTintColor(stack,new HumanoidRenderState(),layers.get(Math.min(index,layers.size()-1)),index,IClientItemExtensions.super.getArmorLayerTintColor(stack,layer,index,fallback));
  }
 },items);}
}
