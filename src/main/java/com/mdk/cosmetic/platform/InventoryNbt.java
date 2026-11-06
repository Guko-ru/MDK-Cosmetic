package com.mdk.cosmetic.platform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.*;
import net.minecraft.util.ProblemReporter;
public final class InventoryNbt {
 public static void saveAllItems(CompoundTag tag,NonNullList<ItemStack> items,HolderLookup.Provider registries){var output=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,registries);net.minecraft.world.ContainerHelper.saveAllItems(output,items);tag.merge(output.buildResult());}
 public static void loadAllItems(CompoundTag tag,NonNullList<ItemStack> items,HolderLookup.Provider registries){net.minecraft.world.ContainerHelper.loadAllItems(TagValueInput.create(ProblemReporter.DISCARDING,registries,tag),items);}
}
