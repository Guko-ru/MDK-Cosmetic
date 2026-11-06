package com.mdk.cosmetic.platform;
import java.util.function.Supplier;
import java.util.function.BiFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
/** Retains the four primitive-only saved-data schemas with the context-free native codec. */
public final class SavedDataTypes {
 public static <T extends SavedData & LegacySavedData> SavedDataType<T> create(String id,Supplier<T> constructor,BiFunction<CompoundTag,HolderLookup.Provider,T> reader){
  return new SavedDataType<>(id,constructor,CompoundTag.CODEC.xmap(tag->reader.apply(tag,net.minecraft.core.RegistryAccess.EMPTY),data->data.save(new CompoundTag(),net.minecraft.core.RegistryAccess.EMPTY)),null);
 }
}
