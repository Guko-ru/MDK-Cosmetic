package com.mdk.cosmetic.platform;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.core.HolderLookup;
public final class StackNbt {
 public static Tag save(ItemStack stack,HolderLookup.Provider registries){return stack.isEmpty()?new CompoundTag():ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE),stack).getOrThrow();}
 public static ItemStack parse(HolderLookup.Provider registries,CompoundTag tag){return tag.isEmpty()?ItemStack.EMPTY:ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE),tag).resultOrPartial(error->org.slf4j.LoggerFactory.getLogger("mdkcosmetic").warn("Cannot load stored item: {}",error)).orElse(ItemStack.EMPTY);}
}
