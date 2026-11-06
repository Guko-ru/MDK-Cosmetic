package com.mdk.cosmetic.client;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import com.mdk.cosmetic.platform.ArmorDefinition;
public interface ArmorAppearance {
 default HumanoidModel<?> getHumanoidArmorModel(HumanoidRenderState entity,ItemStack stack,EquipmentSlot slot,HumanoidModel<?> original){return original;}
 default int getDefaultDyeColor(ItemStack stack){return 0xFFA06540;}
 default int getArmorLayerTintColor(ItemStack stack,HumanoidRenderState entity,ArmorDefinition.Layer layer,int index,int fallback){return fallback;}
}
