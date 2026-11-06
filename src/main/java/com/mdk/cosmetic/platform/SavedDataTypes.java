package com.mdk.cosmetic.platform;
import java.util.function.Supplier;
import java.util.function.BiFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
/** Retains the existing on-disk CompoundTag schema with the new context-aware SavedData codecs. */
public final class SavedDataTypes {
 public static <T extends SavedData & LegacySavedData> SavedDataType<T> create(String id,Supplier<T> constructor,BiFunction<CompoundTag,HolderLookup.Provider,T> reader){
  return new SavedDataType<>(id,context->constructor.get(),context->CompoundTag.CODEC.xmap(tag->reader.apply(tag,context.levelOrThrow().registryAccess()),data->data.save(new CompoundTag(),context.levelOrThrow().registryAccess())),null);
 }
}
