package com.mdk.cosmetic.platform;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
public final class FurnaceFuel {
 private FurnaceFuel() {}
 public static int burnTime(Level level,ItemStack stack){return stack.isEmpty()?0:level.fuelValues().burnDuration(stack);}
}
