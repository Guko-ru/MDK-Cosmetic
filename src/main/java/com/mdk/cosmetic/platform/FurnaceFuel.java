package com.mdk.cosmetic.platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.item.component.CookingFuel;
/** Resolves the new fuel component with ordinary-furnace semantics for the backpack furnace. */
public final class FurnaceFuel {
 private static final class FuelContext extends FurnaceBlockEntity {
  FuelContext(){super(BlockPos.ZERO,Blocks.FURNACE.defaultBlockState());}
  LootContext context(ServerLevel level){return getLootContext(level);}
 }
 public static int burnTime(Level level,ItemStack stack){
  if(stack.isEmpty()||!stack.has(DataComponents.COOKING_FUEL))return 0;
  if(!(level instanceof ServerLevel server))return 1;
  return ResolvableInt.getFromItem(stack,DataComponents.COOKING_FUEL,CookingFuel::burnTime,new FuelContext().context(server),0);
 }
}
