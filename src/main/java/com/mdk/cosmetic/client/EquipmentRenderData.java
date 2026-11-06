package com.mdk.cosmetic.client;
import net.minecraft.world.item.ItemStack;
public interface EquipmentRenderData {
 ItemStack mdk$getHelmet();
 ItemStack mdk$getBackpack();
 int mdk$getEntityId();
 void mdk$setEquipment(ItemStack helmet,ItemStack backpack,int entityId);
}
