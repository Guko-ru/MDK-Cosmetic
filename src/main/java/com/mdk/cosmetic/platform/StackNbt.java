package com.mdk.cosmetic.platform;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.core.HolderLookup;
public final class StackNbt {
 public static Tag save(ItemStack stack,HolderLookup.Provider provider){return stack.isEmpty()?new CompoundTag():stack.save(provider);}
 public static ItemStack parse(HolderLookup.Provider provider,CompoundTag tag){return tag.isEmpty()?ItemStack.EMPTY:ItemStack.parse(provider,tag).orElse(ItemStack.EMPTY);}
}
