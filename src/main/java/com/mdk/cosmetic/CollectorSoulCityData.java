package com.mdk.cosmetic;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Permanent claim per ancient-city start chunk, surviving server restarts and deaths. */
public final class CollectorSoulCityData extends SavedData {
    private final Set<String> occupied=new HashSet<>();
    public static final SavedData.Factory<CollectorSoulCityData> FACTORY =
            new SavedData.Factory<>(CollectorSoulCityData::new, CollectorSoulCityData::load, null);

    public static CollectorSoulCityData forLevel(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY,"mdkcosmetic_collector_soul_cities");
    }
    public boolean contains(String cityId) { return occupied.contains(cityId); }
    public void claim(String cityId) { if (occupied.add(cityId)) setDirty(); }

    private static CollectorSoulCityData load(CompoundTag tag,HolderLookup.Provider registries) {
        CollectorSoulCityData data=new CollectorSoulCityData();
        ListTag list=tag.getList("ClaimedCities",Tag.TAG_STRING);
        for (int i=0;i<list.size();i++) data.occupied.add(list.getString(i));
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries) {
        ListTag list=new ListTag();
        for (String id:occupied) list.add(StringTag.valueOf(id));
        tag.put("ClaimedCities",list);
        return tag;
    }
}
