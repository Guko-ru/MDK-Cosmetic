package com.mdk.cosmetic;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/** Persistent per-stack state. The remote terminal references a server-side memory core, never a copy. */
public final class CollectorBackpackData {
    private CollectorBackpackData() {}

    private static final String KEY = "MDKBackpack";
    public static final int UPGRADE_SLOTS = 5;
    public static final int FILTER_MAX = 54;
    public static final int FILTER_MODULE_SLOT = 0;
    public static final int MAGNET_MODULE_SLOT = 1;

    public static CompoundTag read(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(KEY).copy();
    }

    public static void write(ItemStack stack, CompoundTag data) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        root.put(KEY, data.copy());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    public static boolean linked(ItemStack stack) { return read(stack).getBoolean("Linked"); }
    public static boolean whitelist(ItemStack stack) { return read(stack).getBoolean("Whitelist"); }
    public static boolean magnetEnabled(ItemStack stack) {
        CompoundTag data = read(stack);
        return !data.contains("MagnetEnabled") || data.getBoolean("MagnetEnabled");
    }
    public static boolean totemEnabled(ItemStack stack) { return !read(stack).getBoolean("TotemDisabled"); }
    public static boolean deathVaultEnabled(ItemStack stack) { return !read(stack).getBoolean("DeathVaultDisabled"); }

    public static int capacity(ItemStack stack) { return stack.is(MDKCosmetic.COLLECTOR_BACKPACK_REINFORCED.get()) ? 45 : 27; }

    public static List<ItemStack> items(ItemStack backpack, HolderLookup.Provider provider) {
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < 45; i++) result.add(ItemStack.EMPTY);
        ListTag list = read(backpack).getList("Items", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag slot = list.getCompound(i);
            int index = slot.getInt("Slot");
            if (index >= 0 && index < capacity(backpack))
                result.set(index, ItemStack.parseOptional(provider, slot.getCompound("Stack")));
        }
        return result;
    }

    public static void saveItems(ItemStack backpack, List<ItemStack> items, HolderLookup.Provider provider) {
        CompoundTag data = read(backpack);
        ListTag list = new ListTag();
        for (int i = 0; i < Math.min(capacity(backpack), items.size()); i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;
            CompoundTag entry = new CompoundTag();
            entry.putInt("Slot", i);
            entry.put("Stack", stack.saveOptional(provider));
            list.add(entry);
        }
        data.put("Items", list);
        write(backpack, data);
    }

    public static List<ItemStack> upgradeItems(ItemStack backpack, HolderLookup.Provider provider) {
        migrateLegacyModules(backpack, provider);
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < UPGRADE_SLOTS; i++) result.add(ItemStack.EMPTY);
        ListTag list = read(backpack).getList("UpgradeItems", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getInt("Slot");
            if (slot >= 0 && slot < UPGRADE_SLOTS)
                result.set(slot, ItemStack.parseOptional(provider, entry.getCompound("Stack")));
        }
        return result;
    }

    public static void saveUpgradeItems(ItemStack backpack, List<ItemStack> upgrades, HolderLookup.Provider provider) {
        CompoundTag data = read(backpack);
        ListTag list = new ListTag();
        for (int i = 0; i < Math.min(UPGRADE_SLOTS, upgrades.size()); i++) {
            ItemStack stack = upgrades.get(i);
            if (stack.isEmpty()) continue;
            CompoundTag entry = new CompoundTag();
            entry.putInt("Slot", i);
            entry.put("Stack", stack.saveOptional(provider));
            list.add(entry);
        }
        data.put("UpgradeItems", list);
        data.remove("Filter");
        data.remove("Magnet");
        write(backpack, data);
    }

    public static void setUpgradeItem(ItemStack backpack, int slot, ItemStack stack, HolderLookup.Provider provider) {
        if (slot < 0 || slot >= UPGRADE_SLOTS) return;
        List<ItemStack> upgrades = upgradeItems(backpack, provider);
        upgrades.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        saveUpgradeItems(backpack, upgrades, provider);
    }

    /** All filtering is configured in the magnet, never in a separate upgrade. */
    public static boolean hasFilter(ItemStack backpack, HolderLookup.Provider provider) {
        return hasMagnet(backpack, provider);
    }
    public static boolean hasMagnet(ItemStack backpack, HolderLookup.Provider provider) {
        return hasUpgradeItem(backpack, MDKCosmetic.COLLECTOR_BACKPACK_MAGNET.get(), provider);
    }

    public static boolean hasUpgradeItem(ItemStack backpack, Item item, HolderLookup.Provider provider) {
        for (ItemStack stack : upgradeItems(backpack, provider)) {
            if (stack.is(item)) return true;
        }
        return false;
    }

    public static List<ItemStack> filterItems(ItemStack backpack, HolderLookup.Provider provider) {
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < FILTER_MAX; i++) result.add(ItemStack.EMPTY);
        ListTag list = read(backpack).getList("FilterItems", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getInt("Slot");
            if (slot >= 0 && slot < FILTER_MAX) result.set(slot, ItemStack.parseOptional(provider, entry.getCompound("Stack")));
        }
        return result;
    }

    public static void setFilterItem(ItemStack backpack, int index, ItemStack item, HolderLookup.Provider provider) {
        if (index < 0 || index >= FILTER_MAX) return;
        List<ItemStack> list = filterItems(backpack, provider);
        list.set(index, item.isEmpty() ? ItemStack.EMPTY : item.copyWithCount(1));
        CompoundTag data = read(backpack);
        ListTag out = new ListTag();
        for (int i = 0; i < FILTER_MAX; i++) {
            if (list.get(i).isEmpty()) continue;
            CompoundTag entry = new CompoundTag();
            entry.putInt("Slot", i);
            entry.put("Stack", list.get(i).saveOptional(provider));
            out.add(entry);
        }
        data.put("FilterItems", out);
        write(backpack, data);
    }

    public static boolean accepts(ItemStack backpack, ItemStack candidate, HolderLookup.Provider provider) {
        if (candidate.isEmpty() || candidate.getItem() instanceof CollectorBackpackItem) return false;
        if (!hasFilter(backpack, provider)) return true;
        boolean match = false;
        boolean exact = read(backpack).getBoolean("Exact");
        for (ItemStack entry : filterItems(backpack, provider)) {
            if (!entry.isEmpty() && (exact ? ItemStack.isSameItemSameComponents(entry, candidate) : entry.is(candidate.getItem()))) {
                match = true;
                break;
            }
        }
        return whitelist(backpack) ? match : !match;
    }

    public static CollectorMemoryCoreBlockEntity remoteCore(ItemStack backpack, MinecraftServer server) {
        CompoundTag data = read(backpack);
        if (!data.getBoolean("Linked") || !data.hasUUID("MemoryId")) return null;
        ResourceLocation id = ResourceLocation.tryParse(data.getString("Dimension"));
        if (id == null) return null;
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, id);
        ServerLevel level = server.getLevel(dimension);
        if (level == null) return null;
        BlockPos pos = BlockPos.of(data.getLong("Pos"));
        level.getChunkAt(pos);
        if (!(level.getBlockEntity(pos) instanceof CollectorMemoryCoreBlockEntity core)) return null;
        if (!core.identity().equals(data.getUUID("MemoryId"))) return null;
        for (int x = -2; x <= 2; x += 4) for (int z = -2; z <= 2; z += 4) level.getChunkAt(pos.offset(x, 0, z));
        return CollectorMemoryStructure.ready(level, pos.above(2)) ? core : null;
    }

    public static void link(ItemStack backpack, CollectorMemoryCoreBlockEntity core, ServerLevel level) {
        CompoundTag data = read(backpack);
        data.putBoolean("Linked", true);
        data.putUUID("MemoryId", core.identity());
        data.putString("Dimension", level.dimension().location().toString());
        data.putLong("Pos", core.getBlockPos().asLong());
        data.remove("Items");
        write(backpack, data);
        core.setChanged();
    }

    public static void flag(ItemStack backpack, String name, boolean enabled) {
        CompoundTag data = read(backpack);
        data.putBoolean(name, enabled);
        write(backpack, data);
    }

    private static void migrateLegacyModules(ItemStack backpack, HolderLookup.Provider provider) {
        CompoundTag data = read(backpack);
        if (data.contains("UpgradeItems", 9)) {
            // Older builds used 2 positions: filter / magnet. Convert those safely.
            ListTag old = data.getList("UpgradeItems", 10);
            boolean legacy = false;
            for (int i = 0; i < old.size(); i++) {
                ItemStack item = ItemStack.parseOptional(provider, old.getCompound(i).getCompound("Stack"));
                if (item.is(MDKCosmetic.COLLECTOR_BACKPACK_FILTER.get())) legacy = true;
            }
            if (!legacy) return;
            List<ItemStack> migrated = new ArrayList<>();
            for (int i = 0; i < UPGRADE_SLOTS; i++) migrated.add(ItemStack.EMPTY);
            for (int i = 0; i < old.size(); i++) {
                ItemStack item = ItemStack.parseOptional(provider, old.getCompound(i).getCompound("Stack"));
                if (item.isEmpty()) continue;
                if (item.is(MDKCosmetic.COLLECTOR_BACKPACK_FILTER.get())) {
                    // Keep the legacy item as a reclaimable item; the magnet now owns its filter.
                    List<ItemStack> contents = items(backpack, provider);
                    int free = -1;
                    for (int j = 0; j < capacity(backpack); j++) if (contents.get(j).isEmpty()) { free = j; break; }
                    if (free >= 0 && !linked(backpack)) { contents.set(free, item); saveItems(backpack, contents, provider); }
                    else migrated.set(0, item); // never silently delete a player's item
                } else {
                    int destination = item.is(MDKCosmetic.COLLECTOR_BACKPACK_MAGNET.get()) ? 1 : 0;
                    if (!migrated.get(destination).isEmpty()) destination = 2;
                    migrated.set(destination, item);
                }
            }
            saveUpgradeItems(backpack, migrated, provider);
            return;
        }
        boolean changed = false;
        List<ItemStack> upgrades = new ArrayList<>();
        for (int i = 0; i < UPGRADE_SLOTS; i++) upgrades.add(ItemStack.EMPTY);
        if (data.getBoolean("Filter")) {
            // Legacy installed filter is folded into the magnet's configuration.
            if (!data.getBoolean("Magnet")) upgrades.set(0, new ItemStack(MDKCosmetic.COLLECTOR_BACKPACK_MAGNET.get()));
            changed = true;
        }
        if (data.getBoolean("Magnet")) {
            upgrades.set(0, new ItemStack(MDKCosmetic.COLLECTOR_BACKPACK_MAGNET.get()));
            changed = true;
        }
        if (!changed) return;
        saveUpgradeItems(backpack, upgrades, provider);
        CompoundTag refreshed = read(backpack);
        if (data.getBoolean("MagnetEnabled") && !refreshed.getBoolean("MagnetEnabled")) {
            refreshed.putBoolean("MagnetEnabled", true);
            write(backpack, refreshed);
        }
    }
}
