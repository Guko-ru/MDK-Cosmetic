package com.mdk.cosmetic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * World-persistent, per-player advancement counters. SavedData avoids losing
 * achievement totals on player death, entity cloning or inventory transfers.
 */
public final class CollectorAdvancementData extends SavedData implements com.mdk.cosmetic.platform.LegacySavedData {
    private static final String SAVE_ID = "mdkcosmetic_collector_advancement_counters";
    private static final net.minecraft.world.level.saveddata.SavedDataType<CollectorAdvancementData> FACTORY =
            com.mdk.cosmetic.platform.SavedDataTypes.create(SAVE_ID, CollectorAdvancementData::new, CollectorAdvancementData::load);
    private final Map<UUID, Map<String, Long>> byPlayer = new HashMap<>();

    private static CollectorAdvancementData load(CompoundTag nbt, HolderLookup.Provider registries) {
        CollectorAdvancementData data = new CollectorAdvancementData();
        ListTag list = nbt.getListOrEmpty("Players");
        for (int i = 0; i < list.size(); i++) {
            CompoundTag row = list.getCompoundOrEmpty(i);
            if (!row.read("Id",net.minecraft.core.UUIDUtil.CODEC).isPresent()) continue;
            UUID id = row.read("Id",net.minecraft.core.UUIDUtil.CODEC).orElseThrow();
            CompoundTag values = row.getCompoundOrEmpty("Counters");
            Map<String, Long> counts = new HashMap<>();
            for (String key : values.keySet()) {
                if (values.getLong(key).isPresent()) counts.put(key, Math.max(0L, values.getLongOr(key,0L)));
            }
            data.byPlayer.put(id, counts);
        }
        return data;
    }

    public static CollectorAdvancementData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY);
    }

    public long count(ServerPlayer player, String metric) {
        return byPlayer.getOrDefault(player.getUUID(), Map.of()).getOrDefault(metric, 0L);
    }

    public long add(ServerPlayer player, String metric, long increment) {
        if (increment <= 0L) return count(player, metric);
        Map<String, Long> counters = byPlayer.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>());
        long old = counters.getOrDefault(metric, 0L);
        long next = old > Long.MAX_VALUE - increment ? Long.MAX_VALUE : old + increment;
        counters.put(metric, next);
        setDirty();
        return next;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (var user : byPlayer.entrySet()) {
            CompoundTag row = new CompoundTag();
            row.store("Id",net.minecraft.core.UUIDUtil.CODEC,user.getKey());
            CompoundTag counts = new CompoundTag();
            for (var entry : user.getValue().entrySet()) counts.putLong(entry.getKey(), entry.getValue());
            row.put("Counters", counts);
            list.add(row);
        }
        tag.put("Players", list);
        return tag;
    }
}
