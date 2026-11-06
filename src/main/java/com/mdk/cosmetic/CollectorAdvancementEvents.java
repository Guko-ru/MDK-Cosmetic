package com.mdk.cosmetic;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import com.mdk.cosmetic.platform.Events.LivingDeathEvent;
import com.mdk.cosmetic.platform.Events.ItemEntityPickupEvent;
import com.mdk.cosmetic.platform.Events.PlayerEvent;
import com.mdk.cosmetic.platform.Events.PlayerTickEvent;

/**
 * Server-authoritative challenges that vanilla advancement predicates cannot count.
 * Ordinary item and individual-boss advancements are defined directly in JSON.
 */
public final class CollectorAdvancementEvents {
    private CollectorAdvancementEvents() { }

    private static final int[] KILLS_ALL = {10,50,100,500,1500,5000,10000};
    private static final int[] KILLS_MONSTERS = {25,100,500,1000};
    private static final int[] SHARD_PICKUPS = {8,32,64,256,512,1024,4096,10000};
    private static final int[] COIN_PICKUPS = {1,8,32,64,256,1024};
    private static final int[] SHARDS_HELD = {16,64,128,256,512};
    private static final int[] TRADES = {1,5,25,100,500};
    private static final int[] MEMORY_TYPES = {1,10,32,64,128,256};
    private static final int[] MEMORY_STACKS = {64,256,1024,4096,16384};
    private static final int[] CRAFTED_HATS = {1,5,10,17,32};
    private static final int[] CRAFTED_CASINGS = {1,8,16,32,52};

    private static final List<Item> GREEN_HATS = List.of(
            MDKCosmetic.FOX_HAT.get(), MDKCosmetic.PANDA_HAT.get(), MDKCosmetic.CAT_EARS.get(),
            MDKCosmetic.ALLAY_HAT.get(), MDKCosmetic.AXOLOTL_HAT.get(), MDKCosmetic.RABBIT_EARS.get(),
            MDKCosmetic.OCTOPUS_HAT.get(), MDKCosmetic.GLOW_SQUID_HAT.get(), MDKCosmetic.CAMEL_HAT.get(),
            MDKCosmetic.COW_HAT.get(), MDKCosmetic.MOOSHROOM_HAT.get(), MDKCosmetic.POLAR_BEAR_HAT.get(),
            MDKCosmetic.LLAMA_HAT.get(), MDKCosmetic.SHEEP_HAT.get(), MDKCosmetic.WOLF_HAT.get(),
            MDKCosmetic.HORSE_HAT.get(), MDKCosmetic.DONKEY_HAT.get());
    private static final List<Item> FURS = List.of(
            MDKCosmetic.FOX_FUR.get(), MDKCosmetic.PANDA_FUR.get(), MDKCosmetic.CAT_FUR.get(),
            MDKCosmetic.ALLAY_FIBERS.get(), MDKCosmetic.AXOLOTL_SHED.get(), MDKCosmetic.RABBIT_FUR.get(),
            MDKCosmetic.SQUID_INK_CLOT.get(), MDKCosmetic.GLOW_SQUID_INK_CLOT.get(), MDKCosmetic.CAMEL_FUR.get(),
            MDKCosmetic.COW_FUR.get(), MDKCosmetic.MOOSHROOM_FUR.get(), MDKCosmetic.POLAR_BEAR_FUR.get(),
            MDKCosmetic.LLAMA_FUR.get(), MDKCosmetic.SHEEP_FLEECE.get(), MDKCosmetic.WOLF_FUR.get(),
            MDKCosmetic.HORSE_FUR.get(), MDKCosmetic.DONKEY_FUR.get());

    private static void award(ServerPlayer player, String id) {
        AdvancementHolder advancement = com.mdk.cosmetic.platform.ServerAccess.get(player).getAdvancements().get(
                ResourceLocation.fromNamespaceAndPath(MDKCosmetic.MOD_ID, id));
        if (advancement != null) player.getAdvancements().award(advancement, "done");
    }

    /** Five independent criteria give a visible 0/5...5/5 milestone bar in the vanilla UI. */
    private static void progress(ServerPlayer player, String metric, long count, int[] targets) {
        for (int target : targets) {
            String id = "milestone/" + metric + "_" + target;
            AdvancementHolder advancement = com.mdk.cosmetic.platform.ServerAccess.get(player).getAdvancements().get(
                    ResourceLocation.fromNamespaceAndPath(MDKCosmetic.MOD_ID, id));
            if (advancement == null) continue;
            for (int step = 1; step <= 5; step++) {
                long required = (target * (long) step + 4L) / 5L;
                if (count >= required) player.getAdvancements().award(advancement, "step_" + step);
            }
        }
    }

    private static void increased(ServerPlayer player, String metric, long count) {
        switch (metric) {
            case "kills_all" -> progress(player,metric,count,KILLS_ALL);
            case "kills_monsters" -> progress(player,metric,count,KILLS_MONSTERS);
            case "shard_pickups" -> progress(player,metric,count,SHARD_PICKUPS);
            case "coin_pickups" -> progress(player,metric,count,COIN_PICKUPS);
            case "shards_held" -> progress(player,metric,count,SHARDS_HELD);
            case "trades" -> progress(player,metric,count,TRADES);
            case "memory_types" -> progress(player,metric,count,MEMORY_TYPES);
            case "memory_stacks" -> progress(player,metric,count,MEMORY_STACKS);
            case "crafted_hats" -> progress(player,metric,count,CRAFTED_HATS);
            case "crafted_casings" -> progress(player,metric,count,CRAFTED_CASINGS);
        }
    }

    private static long accumulate(ServerPlayer player, String metric, long count) {
        long total = CollectorAdvancementData.get(com.mdk.cosmetic.platform.ServerAccess.get(player)).add(player, metric, count);
        increased(player, metric, total);
        return total;
    }

    public static void onPickup(ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        ItemStack original=event.getOriginalStack();
        int picked=Math.max(0,original.getCount()-event.getCurrentStack().getCount());
        if (picked<=0) return;
        if (original.is(MDKCosmetic.POWER_ESSENCE.get())) accumulate(player,"shard_pickups",picked);
        if (original.is(MDKCosmetic.CHARGED_POWER_ESSENCE.get())) accumulate(player,"coin_pickups",picked);
    }

    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity victim=event.getEntity();
        if (victim == player) return;
        accumulate(player,"kills_all",1L);
        if (victim instanceof Monster) accumulate(player,"kills_monsters",1L);
        if (victim instanceof CollectorHeartEntity heart && !heart.isDecoy()) {
            if (player.getMainHandItem().is(net.minecraft.world.item.Items.TRIDENT))
                award(player, "ocean/memory_trophy");
            if (heart.hasCreatedClone()) award(player, "ocean/illusion_survivor");
            if (heart.hasEnteredFinalPhase()) award(player, "ocean/storm_survivor");
            if (player.getItemBySlot(EquipmentSlot.HEAD).isEmpty() &&
                    player.getItemBySlot(EquipmentSlot.CHEST).isEmpty() &&
                    player.getItemBySlot(EquipmentSlot.LEGS).isEmpty() &&
                    player.getItemBySlot(EquipmentSlot.FEET).isEmpty())
                award(player, "ocean/barehanded_memory");
            if (player.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS))
                award(player, "ocean/under_pressure");
        }
        if (victim instanceof UnoEntity) {
            if (fullArmor(player)) award(player, "heart/defeat_heart");
            if (player.getItemBySlot(EquipmentSlot.HEAD).isEmpty() &&
                    player.getItemBySlot(EquipmentSlot.CHEST).isEmpty() &&
                    player.getItemBySlot(EquipmentSlot.LEGS).isEmpty() &&
                    player.getItemBySlot(EquipmentSlot.FEET).isEmpty()) award(player,"challenge/one_vs_uno");
            if (player.getMainHandItem().is(MDKCosmetic.COLLECTOR_SWORD.get())) award(player,"challenge/uno_sword");
            if (victim.level().dimension()==Level.END) award(player,"challenge/end_uno");
        }
        CollectorVoidAchievements.onBossKill(player);
        if (victim instanceof CollectorSoulEntity soul && victim.level() instanceof ServerLevel level) {
            if (!level.getEntitiesOfClass(Warden.class, new AABB(soul.blockPosition()).inflate(25.0D)).isEmpty())
                award(player,"challenge/soul_and_warden");
        }
    }

    public static void onBackpackUpgrade(ServerPlayer player) { award(player, "backpack/reinforced"); }
    public static void onBackpackLink(ServerPlayer player) { award(player, "backpack/linked"); }
    public static void onMemoryInsurance(ServerPlayer player) { award(player, "backpack/insured_death"); }

    /** Vanilla crafting achievement counter for 5x5 casing components. */
    public static void onCraft(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack output=event.getCrafting();
        CollectorVoidAchievements.onCraft(player);
        if (output.is(MDKCosmetic.COLLECTOR_BACKPACK_BASIC.get())) award(player, "backpack/first");
        if (output.is(MDKCosmetic.COLLECTOR_BACKPACK_CRAFTING.get())) award(player, "backpack/craft_workbench");
        if (output.is(MDKCosmetic.COLLECTOR_BACKPACK_FURNACE.get())) award(player, "backpack/craft_furnace");
        if (output.is(MDKCosmetic.COLLECTOR_BACKPACK_MAGNET.get())) award(player, "backpack/craft_magnet");
        if (output.is(MDKCosmetic.COLLECTOR_BACKPACK_TOTEM.get())) award(player, "backpack/craft_totem");
        if (output.is(MDKCosmetic.COLLECTOR_MEMORY_CASING_ITEM.get()))
            accumulate(player,"crafted_casings",output.getCount());
    }

    /** The tailoring table uses a MerchantMenu, so ordinary crafting events do not fire. */
    public static void onTailorTrade(ServerPlayer player, ItemStack result) {
        if (!result.isEmpty() && GREEN_HATS.contains(result.getItem()))
            accumulate(player,"crafted_hats",result.getCount());
    }

    /** Called only after a server-authorized purchase has consumed its coins. */
    public static void onCollectorTrade(ServerPlayer player, Item product) {
        accumulate(player,"trades",1);
        if (product == MDKCosmetic.COLLECTOR_BACKPACK_DEATH_VAULT.get())
            award(player, "backpack/insurance_buyer");
    }

    private static Set<Item> inventoryItems(ServerPlayer player) {
        Set<Item> items = new HashSet<>();
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) if (!stack.isEmpty()) items.add(stack.getItem());
        for (ItemStack stack : java.util.List.of(player.getItemBySlot(EquipmentSlot.HEAD),player.getItemBySlot(EquipmentSlot.CHEST),player.getItemBySlot(EquipmentSlot.LEGS),player.getItemBySlot(EquipmentSlot.FEET))) if (!stack.isEmpty()) items.add(stack.getItem());
        for (ItemStack stack : java.util.List.of(player.getOffhandItem())) if (!stack.isEmpty()) items.add(stack.getItem());
        return items;
    }

    private static boolean fullArmor(ServerPlayer player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(MDKCosmetic.COLLECTOR_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(MDKCosmetic.COLLECTOR_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(MDKCosmetic.COLLECTOR_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(MDKCosmetic.COLLECTOR_BOOTS.get());
    }

    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 60 != 0) return;
        CollectorVoidAchievements.onTick(player);

        Set<Item> collected=inventoryItems(player);
        if (!player.level().getEntitiesOfClass(CollectorHeartEntity.class,
                player.getBoundingBox().inflate(26.0D), heart -> !heart.isDecoy()).isEmpty())
            award(player, "ocean/first_contact");
        for (ItemStack bag : player.getInventory().getNonEquipmentItems()) {
            if (!(bag.getItem() instanceof CollectorBackpackItem)) continue;
            var upgrades = CollectorBackpackData.upgradeItems(bag, player.registryAccess());
            int installed=0;
            for (ItemStack module : upgrades) if (!module.isEmpty()) {
                installed++;
                if (module.is(MDKCosmetic.COLLECTOR_BACKPACK_MAGNET.get())) award(player, "backpack/install_magnet");
                if (module.is(MDKCosmetic.COLLECTOR_BACKPACK_CRAFTING.get())) award(player, "backpack/install_workbench");
                if (module.is(MDKCosmetic.COLLECTOR_BACKPACK_FURNACE.get())) award(player, "backpack/install_furnace");
                if (module.is(MDKCosmetic.COLLECTOR_BACKPACK_TOTEM.get())) award(player, "backpack/install_totem");
                if (module.is(MDKCosmetic.COLLECTOR_BACKPACK_DEATH_VAULT.get())) award(player, "backpack/install_insurance");
            }
            if (installed >= 3) award(player, "backpack/three_modules");
            if (installed >= 5) award(player, "backpack/all_modules");
            if (CollectorBackpackData.linked(bag) && installed >= 5) award(player, "backpack/full_terminal");
        }
        boolean armor=fullArmor(player);
        if (armor) award(player,"challenge/complete_armor");
        if (collected.containsAll(List.of(MDKCosmetic.COLLECTOR_HELMET.get(),
                MDKCosmetic.COLLECTOR_CHESTPLATE.get(), MDKCosmetic.COLLECTOR_LEGGINGS.get(),
                MDKCosmetic.COLLECTOR_BOOTS.get(), MDKCosmetic.COLLECTOR_SWORD.get(),
                MDKCosmetic.COLLECTOR_PICKAXE.get()))) award(player,"challenge/armor_carry");
        for (Item green : GREEN_HATS) if (player.getItemBySlot(EquipmentSlot.HEAD).is(green)) {
            award(player,"wardrobe/one_worn");break;
        }
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(MDKCosmetic.SAKURA_THINKER.get()))
            award(player,"challenge/wear_sakura");
        if (player.hasEffect(MDKCosmetic.GREED_AURA.holder())) award(player,"challenge/greedy");
        if (player.hasEffect(MDKCosmetic.PRODUCTIVITY_AURA.holder())) award(player,"challenge/productive");
        if (player.hasEffect(MDKCosmetic.GREED_AURA.holder()) && player.hasEffect(MDKCosmetic.PRODUCTIVITY_AURA.holder()))
            award(player,"challenge/two_spheres");
        if (armor && !player.level().getEntitiesOfClass(Warden.class,
                player.getBoundingBox().inflate(18.0D)).isEmpty()) award(player,"challenge/fearless_warden");

        long held=0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems())
            if (stack.is(MDKCosmetic.POWER_ESSENCE.get())) held += stack.getCount();
        increased(player,"shards_held",held);

        // Only known, nearby loaded controller blocks. No unloaded chunks are force-loaded.
        CollectorMemoryCoreBlockEntity nearestMemory=null;
        boolean assembled=false;
        boolean memoryReady=false;
        int stress=0;
        if (player.level() instanceof ServerLevel level) {
            BlockPos center=player.blockPosition();
            findStructure:
            for (int x=-7;x<=7;x++) for (int y=-4;y<=4;y++) for (int z=-7;z<=7;z++) {
                BlockPos p=center.offset(x,y,z);
                if (!level.hasChunkAt(p)) continue;
                if (!(level.getBlockEntity(p) instanceof CollectorLinkBlockEntity link) || !link.isActive()) continue;
                CollectorSoulStorageBlockEntity soul=link.core();
                if (soul==null || !soul.isAssembled()) continue;
                assembled=true;
                stress=Math.max(stress,soul.getStress());
                nearestMemory=link.memoryCore();
                memoryReady=nearestMemory!=null;
                break findStructure;
            }
        }
        if (assembled) award(player,"challenge/assemble_3");
        if (stress>=50) award(player,"challenge/stress_half");
        if (stress>=90) award(player,"challenge/stress_ninety");
        if (memoryReady) {
            award(player,"challenge/assemble_5");
            long total=0;
            for (int index=0;index<nearestMemory.types();index++) {
                long qty=nearestMemory.countAt(index);
                total=total > Long.MAX_VALUE-qty ? Long.MAX_VALUE : total+qty;
                ItemStack item=nearestMemory.view(index);
                if (!item.isEmpty()) collected.add(item.getItem());
            }
            increased(player,"memory_types",nearestMemory.types());
            increased(player,"memory_stacks",total);
        }
        if (collected.containsAll(GREEN_HATS)) award(player,"wardrobe/full_green");
        int hatCount=0;
        for (Item hat : GREEN_HATS) if (collected.contains(hat)) hatCount++;
        if (hatCount>=9) award(player,"wardrobe/half_hats");
        if (collected.containsAll(FURS)) award(player,"wardrobe/all_furs");
        if (memoryReady && armor && collected.containsAll(GREEN_HATS))
            award(player,"challenge/ultimate_collection");
    }
}
