package com.mdk.cosmetic;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.server.level.ServerLevel;

/** Durable per-dimension record: killing the boss will not create another one on chunk reload. */
public final class CollectorHeartSpawnData extends SavedData implements com.mdk.cosmetic.platform.LegacySavedData {
    private static final String ID = "mdk_collector_heart_monuments";
    private final List<BlockPos> monuments = new ArrayList<>();

    public static CollectorHeartSpawnData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                com.mdk.cosmetic.platform.SavedDataTypes.create(ID, CollectorHeartSpawnData::new, CollectorHeartSpawnData::load));
    }
    public boolean registered(BlockPos pos) {
        for (BlockPos existing : monuments)
            if (existing.distSqr(pos) < 100.0D * 100.0D) return true;
        return false;
    }
    public void mark(BlockPos pos) {
        if (registered(pos)) return;
        monuments.add(pos.immutable());
        setDirty();
    }
    private static CollectorHeartSpawnData load(CompoundTag tag, HolderLookup.Provider registries) {
        CollectorHeartSpawnData saved = new CollectorHeartSpawnData();
        ListTag list = tag.getListOrEmpty("Monuments"); // NBT long
        for (int i = 0; i < list.size(); i++) saved.monuments.add(BlockPos.of(((LongTag)list.get(i)).longValue()));
        return saved;
    }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (BlockPos pos : monuments) list.add(LongTag.valueOf(pos.asLong()));
        tag.put("Monuments", list);
        return tag;
    }
}
