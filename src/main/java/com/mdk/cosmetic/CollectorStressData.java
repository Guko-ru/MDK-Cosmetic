package com.mdk.cosmetic;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Persists stress/first-activation for each storage location even if a player
 * breaks and re-places the storage block. Each dimension stores its own data.
 * Only a meltdown clears its entry. This prevents free reset by reassembly.
 */
public final class CollectorStressData extends SavedData {
    private final Map<Long, Entry> entries = new HashMap<>();
    public record Entry(int stress, boolean everActivated) {}

    private static final SavedData.Factory<CollectorStressData> FACTORY =
            new SavedData.Factory<>(CollectorStressData::new, CollectorStressData::load, null);

    public static CollectorStressData forLevel(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, "mdkcosmetic_collector_stress");
    }

    public Entry read(BlockPos pos) {
        return entries.get(pos.asLong());
    }

    public void store(BlockPos pos, int stress, boolean everActivated) {
        entries.put(pos.asLong(), new Entry(Math.clamp(stress, 0, 100), everActivated));
        setDirty();
    }

    public void clearAfterExplosion(BlockPos pos) {
        if (entries.remove(pos.asLong()) != null) setDirty();
    }

    private static CollectorStressData load(CompoundTag tag, HolderLookup.Provider registries) {
        CollectorStressData data = new CollectorStressData();
        ListTag list = tag.getList("Storages", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag element = list.getCompound(i);
            data.entries.put(element.getLong("Position"),
                    new Entry(Math.clamp(element.getInt("Stress"), 0, 100),
                            element.getBoolean("Activated")));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<Long, Entry> e : entries.entrySet()) {
            CompoundTag element = new CompoundTag();
            element.putLong("Position", e.getKey());
            element.putInt("Stress", e.getValue().stress());
            element.putBoolean("Activated", e.getValue().everActivated());
            list.add(element);
        }
        tag.put("Storages", list);
        return tag;
    }
}
