package com.mdk.cosmetic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** One persistent realm ID per Memory identity; separate plots inside one empty dimension.
 * Minecraft dimension registries are frozen at server startup, hence individual regions
 * are preferable to trying to register a ServerLevel for every block in multiplayer. */
public final class CollectorVoidInstances extends SavedData implements com.mdk.cosmetic.platform.LegacySavedData {
    private static final String STORAGE_ID = "mdk_collector_void_instances";
    public static final int SPACING = 1024;
    private final Map<UUID, Integer> slots = new HashMap<>();
    private int nextSlot;
    private final java.util.Set<UUID> welcomed = new java.util.HashSet<>();

    public static CollectorVoidInstances get(ServerLevel voidLevel) {
        return voidLevel.getDataStorage().computeIfAbsent(
                com.mdk.cosmetic.platform.SavedDataTypes.create(STORAGE_ID, CollectorVoidInstances::new, CollectorVoidInstances::load));
    }

    public int reserve(UUID id) {
        Integer existing = slots.get(id);
        if (existing != null) return existing;
        if (nextSlot >= 65_000) throw new IllegalStateException("Collector VOID allocation limit reached");
        int slot = nextSlot++;
        slots.put(id, slot);
        setDirty();
        return slot;
    }

    public UUID realmAt(BlockPos position) {
        long x = position.getX();
        long z = position.getZ();
        for (var e : slots.entrySet()) {
            BlockPos center = center(e.getValue());
            if (Math.abs(x - center.getX()) <= 24 && Math.abs(z - center.getZ()) <= 24)
                return e.getKey();
        }
        return null;
    }

    public boolean markWelcomed(UUID id) {
        if (!welcomed.add(id)) return false;
        setDirty();
        return true;
    }

    public static BlockPos center(int slot) {
        // 255 x 255 valid plots, all separated by 1024 blocks.
        int x = (slot % 255 - 127) * SPACING;
        int z = (slot / 255 - 127) * SPACING;
        return new BlockPos(x, 64, z);
    }

    private static CollectorVoidInstances load(CompoundTag tag, HolderLookup.Provider provider) {
        CollectorVoidInstances state = new CollectorVoidInstances();
        state.nextSlot = tag.getIntOr("NextSlot",0);
        ListTag entries = tag.getListOrEmpty("Realms");
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag e = entries.getCompoundOrEmpty(i);
            if (e.read("Id",net.minecraft.core.UUIDUtil.CODEC).isPresent()) state.slots.put(e.read("Id",net.minecraft.core.UUIDUtil.CODEC).orElseThrow(), e.getIntOr("Slot",0));
        }
        ListTag welcomed = tag.getListOrEmpty("Welcomed");
        for (int i = 0; i < welcomed.size(); i++) {
            CompoundTag e = welcomed.getCompoundOrEmpty(i);
            if (e.read("Id",net.minecraft.core.UUIDUtil.CODEC).isPresent()) state.welcomed.add(e.read("Id",net.minecraft.core.UUIDUtil.CODEC).orElseThrow());
        }
        return state;
    }

    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("NextSlot", nextSlot);
        ListTag entries = new ListTag();
        for (var e : slots.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.store("Id",net.minecraft.core.UUIDUtil.CODEC,e.getKey());
            entry.putInt("Slot", e.getValue());
            entries.add(entry);
        }
        tag.put("Realms", entries);
        ListTag firstVisits = new ListTag();
        for (UUID id : welcomed) {
            CompoundTag entry = new CompoundTag();
            entry.store("Id",net.minecraft.core.UUIDUtil.CODEC,id);
            firstVisits.add(entry);
        }
        tag.put("Welcomed", firstVisits);
        return tag;
    }
}
