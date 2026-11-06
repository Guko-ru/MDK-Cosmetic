package com.mdk.cosmetic.client;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
public final class ForgeArmorExtensions {
 public static final Map<Item,ForgeArmorRenderer> RENDERERS=new IdentityHashMap<>();
 public interface Appearance {
  default HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity,ItemStack stack,EquipmentSlot slot,HumanoidModel<?> context){return context;}
  default int getDefaultDyeColor(ItemStack stack){return 0xFFA06540;}
  default int getArmorLayerTintColor(ItemStack stack,LivingEntity entity,ArmorMaterial.Layer layer,int index,int fallback){return fallback;}
 }
 public static final class Registration {
  public void registerItem(Appearance appearance,Item... items){var renderer=new ForgeArmorRenderer(appearance);for(var item:items)RENDERERS.put(item,renderer);}
 }
}
