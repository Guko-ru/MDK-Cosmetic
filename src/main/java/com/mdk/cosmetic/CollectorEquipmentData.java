package com.mdk.cosmetic;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.mdk.cosmetic.platform.ForgePayloads;

/** Player-owned, server-authoritative backpack equipment and armor control state. */
public final class CollectorEquipmentData {
    private static final String KEY = "MDKCollectorEquipment";
    private CollectorEquipmentData() {}

    private static CompoundTag tag(Player player) {
        return com.mdk.cosmetic.platform.PersistentData.of(player).getCompoundOrEmpty(KEY);
    }
    private static void store(Player player, CompoundTag tag) {
        com.mdk.cosmetic.platform.PersistentData.of(player).put(KEY, tag);
    }
    public static ItemStack backpack(Player player) {
        if (player.level().isClientSide())
            return com.mdk.cosmetic.client.CollectorEquipmentVisuals.clientBackpack(player.getUUID());
        return com.mdk.cosmetic.platform.StackNbt.parse(player.registryAccess(), tag(player).getCompoundOrEmpty("Backpack"));
    }
    public static void setBackpack(Player player, ItemStack backpack) {
        ItemStack old = backpack(player);
        boolean visualChanged = old.getItem() != backpack.getItem()
                || CollectorBackpackData.linked(old) != CollectorBackpackData.linked(backpack);
        CompoundTag data = tag(player);
        if (backpack.isEmpty()) data.remove("Backpack");
        else if (backpack.getItem() instanceof CollectorBackpackItem)
            data.put("Backpack", com.mdk.cosmetic.platform.StackNbt.save(backpack,player.registryAccess()));
        else return;
        store(player, data);
        if (visualChanged && player instanceof ServerPlayer server) syncVisual(server);
    }
    public static ItemStack fuel(Player player) {
        return com.mdk.cosmetic.platform.StackNbt.parse(player.registryAccess(), tag(player).getCompoundOrEmpty("FlightFuel"));
    }
    public static void setFuel(Player player, ItemStack stack) {
        CompoundTag data = tag(player);
        if (stack.isEmpty()) data.remove("FlightFuel");
        else data.put("FlightFuel", com.mdk.cosmetic.platform.StackNbt.save(stack,player.registryAccess()));
        store(player, data);
    }
    public static int get(Player player, String setting) { return tag(player).getIntOr(setting,0); }
    public static boolean flag(Player player, String setting) { return tag(player).getBooleanOr(setting,false); }
    public static void set(Player player, String setting, int value) {
        CompoundTag data = tag(player);
        data.putInt(setting, value);
        store(player, data);
    }
    public static void flag(Player player, String setting, boolean value) {
        CompoundTag data = tag(player);
        data.putBoolean(setting, value);
        store(player, data);
    }
    // 0 = no fear, 1 = flee, 2 = hostile provocation. Default retains existing armor behavior.
    public static int fearMode(Player player) {
        return tag(player).contains("FearMode") ? get(player, "FearMode") : 1;
    }
    public static int visualType(Player player) {
        ItemStack stack = backpack(player);
        if (stack.is(MDKCosmetic.COLLECTOR_BACKPACK_BASIC.get())) return 1;
        return stack.is(MDKCosmetic.COLLECTOR_BACKPACK_REINFORCED.get()) ? 2 : 0;
    }
    public static void syncVisual(ServerPlayer owner) {
        var packet = new CollectorEquipmentVisualPayload(owner.getUUID().toString(), visualType(owner),
                CollectorBackpackData.linked(backpack(owner)));
        for (ServerPlayer target : owner.level().players()) {
            if (target.distanceToSqr(owner) < 16384.0 || target == owner)
                ForgePayloads.sendToPlayer(target, packet);
        }
    }
    public static void copyTo(Player previous, Player next) {
        CompoundTag data = com.mdk.cosmetic.platform.PersistentData.of(previous).getCompoundOrEmpty(KEY);
        if (!data.isEmpty()) com.mdk.cosmetic.platform.PersistentData.of(next).put(KEY, data.copy());
    }
}
