package com.mdk.cosmetic.platform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
public interface PersistentData {
    CompoundTag mdk$getPersistentData();
    static CompoundTag of(Entity entity){return ((PersistentData)entity).mdk$getPersistentData();}
}
