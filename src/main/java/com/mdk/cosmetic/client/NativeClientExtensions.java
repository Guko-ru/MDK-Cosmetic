package com.mdk.cosmetic.client;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import com.mdk.cosmetic.platform.CosmeticArmorItem;
public final class NativeClientExtensions {
 private final RegisterClientExtensionsEvent event;
 NativeClientExtensions(RegisterClientExtensionsEvent event){this.event=event;}
 void registerItem(ArmorAppearance appearance,Item...items){event.registerItem(new IClientItemExtensions(){
  private final java.util.Map<HumanoidModel<?>,NativeArmorPoseModel> posed=new java.util.IdentityHashMap<>();
  @Override @SuppressWarnings({"rawtypes","unchecked"}) public Model getGenericArmorModel(ItemStack stack,EquipmentClientInfo.LayerType type,Model original){
   if(!(original instanceof HumanoidModel context))return original;
   var equipped=stack.get(DataComponents.EQUIPPABLE);var slot=equipped==null?EquipmentSlot.HEAD:equipped.slot();
   var selected=appearance.getHumanoidArmorModel(new HumanoidRenderState(),stack,slot,context);
   // The native deferred collector animates each model with its own HumanoidRenderState.
   // Keep custom model visibility: slot-specific vanilla meshes have different empty parts.
   return selected==context?context:posed.computeIfAbsent(context,key->new NativeArmorPoseModel(selected,key));
  }
  @Override public int getDefaultDyeColor(ItemStack stack){return appearance.getDefaultDyeColor(stack);}
  @Override public int getArmorLayerTintColor(ItemStack stack,EquipmentClientInfo.Layer layer,int index,int fallback){
   if(!(stack.getItem() instanceof CosmeticArmorItem armor))return fallback;
   var layers=armor.getMaterial().layers();return appearance.getArmorLayerTintColor(stack,new HumanoidRenderState(),layers.get(Math.min(index,layers.size()-1)),index,IClientItemExtensions.super.getArmorLayerTintColor(stack,layer,index,fallback));
  }
 },items);}
}
