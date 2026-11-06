package com.mdk.cosmetic.platform;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
public final class FurnaceFuel {
 private FurnaceFuel() {}
 public static int burnTime(ItemStack stack){return stack.isEmpty()?0:AbstractFurnaceBlockEntity.getFuel().getOrDefault(stack.getItem(),0);}
}
