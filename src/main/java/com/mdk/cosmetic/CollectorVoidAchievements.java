package com.mdk.cosmetic;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Server-authoritative VOID progression. Never grants an achievement just because
 * a new item was registered or a GUI was opened. Five independent final criteria. */
public final class CollectorVoidAchievements {
    private CollectorVoidAchievements() {}

    private static AdvancementHolder holder(ServerPlayer player, String path) {
        return player.getServer().getAdvancements().get(
                ResourceLocation.fromNamespaceAndPath(MDKCosmetic.MOD_ID, path));
    }

    private static void award(ServerPlayer player, String path) {
        AdvancementHolder advancement = holder(player, path);
        if (advancement != null) player.getAdvancements().award(advancement, "done");
    }

    private static void finalCriterion(ServerPlayer player, String criterion) {
        AdvancementHolder advancement = holder(player, "void/collector_legacy");
        if (advancement != null) player.getAdvancements().award(advancement, criterion);
    }

    private static boolean completed(ServerPlayer player, String path) {
        AdvancementHolder advancement = holder(player, path);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    private static boolean fullArmor(ServerPlayer player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(MDKCosmetic.COLLECTOR_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(MDKCosmetic.COLLECTOR_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(MDKCosmetic.COLLECTOR_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(MDKCosmetic.COLLECTOR_BOOTS.get());
    }

    /** Already completed bosses are recognized retroactively from vanilla advancements. */
    private static void syncBosses(ServerPlayer player) {
        boolean memory = completed(player, "kills/collector_heart");
        boolean heart = completed(player, "kills/uno");
        if (memory) finalCriterion(player, "memory");
        if (heart) finalCriterion(player, "heart");
        if (memory && heart) award(player, "void/both_fragments");
    }

    public static void onBossKill(ServerPlayer player) {
        // The vanilla kill advancements may be granted later in the same tick.
        // onTick rechecks stored vanilla progression to avoid event-order races.
        syncBosses(player);
    }

    public static void onShellReady(ServerPlayer player) {
        award(player, "void/final_shell");
        finalCriterion(player, "shell");
    }

    public static void onEnter(ServerPlayer player) {
        award(player, "void/first_entry");
        finalCriterion(player, "entered");
        long visits = CollectorAdvancementData.get(player.getServer()).add(player, "void_entries", 1);
        if (visits >= 2) award(player, "void/return_visit");
        if (visits >= 5) award(player, "void/five_visits");
    }

    public static void onReturn(ServerPlayer player) {
        award(player, "void/return_home");
        finalCriterion(player, "returned");
        if (fullArmor(player)) award(player, "void/armored_return");
    }

    public static void onCraft(ServerPlayer player) {
        if (player.level().dimension().equals(CollectorVoidPortalController.VOID))
            award(player, "void/craft_in_void");
    }

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        BlockState state = event.getState();
        if (state.is(MDKCosmetic.COLLECTOR_METEOR_BLOCK.get())
                && player.getMainHandItem().is(MDKCosmetic.COLLECTOR_PICKAXE.get())) {
            award(player, "void/meteor_miner");
        }
    }

    public static void onTick(ServerPlayer player) {
        syncBosses(player);
        if (completed(player, "void/final_shell")) finalCriterion(player, "shell");
        if (completed(player, "void/first_entry")) finalCriterion(player, "entered");
        if (completed(player, "void/return_home")) finalCriterion(player, "returned");
        if (!player.level().dimension().equals(CollectorVoidPortalController.VOID)) return;

        long ticks = CollectorAdvancementData.get(player.getServer()).add(player, "void_exploration_ticks", 60);
        if (ticks >= 20L * 60 * 5) award(player, "void/five_minutes");
        if (ticks >= 20L * 60 * 30) award(player, "void/half_hour");
        if (fullArmor(player)) award(player, "void/armored_void");
        ItemStack backpack = CollectorEquipmentData.backpack(player);
        if (backpack.getItem() instanceof CollectorBackpackItem) {
            award(player, "void/backpack_void");
            int installed = 0;
            for (ItemStack module : CollectorBackpackData.upgradeItems(backpack, player.registryAccess()))
                if (!module.isEmpty()) installed++;
            if (installed >= 5) award(player, "void/portable_kingdom");
        }
        // A distant excursion cannot be earned from simply stepping off the landing platform.
        int nearestX = Math.round(player.getBlockX() / (float) CollectorVoidInstances.SPACING)
                * CollectorVoidInstances.SPACING;
        int nearestZ = Math.round(player.getBlockZ() / (float) CollectorVoidInstances.SPACING)
                * CollectorVoidInstances.SPACING;
        double dx = player.getX() - nearestX;
        double dz = player.getZ() - nearestZ;
        double horizontalDistanceSquared = dx * dx + dz * dz;
        if (horizontalDistanceSquared >= 128.0 * 128.0)
            award(player, "void/into_the_unknown");
        if (horizontalDistanceSquared >= 512.0 * 512.0)
            award(player, "void/beyond_the_horizon");
        for (ServerPlayer other : player.serverLevel().players()) {
            if (other == player || !other.isAlive()) continue;
            if (other.distanceToSqr(player) < 32 * 32) {
                award(player, "void/not_alone");
                award(other, "void/not_alone");
                break;
            }
        }
    }
}
