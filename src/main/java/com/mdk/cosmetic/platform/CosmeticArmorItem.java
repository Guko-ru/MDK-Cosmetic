package com.mdk.cosmetic.platform;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;
/** Component-driven armor; preserves explicit original durability rather than replacing it with a material default. */
public final class CosmeticArmorItem extends Item {
 private final ArmorDefinition material;
 private final ArmorType type;
 public CosmeticArmorItem(ArmorDefinition material,ArmorType type,Properties properties){super(properties(material,type,properties));this.material=material;this.type=type;}
 private static Properties properties(ArmorDefinition material,ArmorType type,Properties properties){var original=((ItemPropertiesAccess)properties).mdk$getComponents().build().get(DataComponents.MAX_DAMAGE);properties.humanoidArmor(material.nativeMaterial(),type);if(!material.enchantable())properties.component(DataComponents.ENCHANTABLE,null);if(original!=null)properties.durability(original);return properties;}
 public ArmorDefinition getMaterial(){return material;}
 public EquipmentSlot getEquipmentSlot(){return type.getSlot();}
}
