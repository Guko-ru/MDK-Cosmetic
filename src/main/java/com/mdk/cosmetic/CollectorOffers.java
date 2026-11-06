package com.mdk.cosmetic;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Server-owned catalogue. Clients send only an offer index, never a price or product. */
public final class CollectorOffers {
    public static final int EXCLUSIVE = 0;
    public static final int MISC = 1;
    private static final List<Offer> OFFERS = createOffers();
    public static final int COUNT = OFFERS.size();
    private CollectorOffers() {}
    private record Offer(String name, Supplier<? extends Item> item, int count, int cost, int stress, int category) {}
    private static List<Offer> createOffers() {
        List<Offer> offers = new ArrayList<>();
        offers.add(new Offer("collector_helmet", MDKCosmetic.COLLECTOR_HELMET::get, 1, 64, 50, EXCLUSIVE));
        offers.add(new Offer("collector_chestplate", MDKCosmetic.COLLECTOR_CHESTPLATE::get, 1, 64, 80, EXCLUSIVE));
        offers.add(new Offer("collector_leggings", MDKCosmetic.COLLECTOR_LEGGINGS::get, 1, 64, 50, EXCLUSIVE));
        offers.add(new Offer("collector_boots", MDKCosmetic.COLLECTOR_BOOTS::get, 1, 64, 30, EXCLUSIVE));
        offers.add(new Offer("greed_orb", MDKCosmetic.GREED_ORB::get, 1, 5, 50, EXCLUSIVE));
        offers.add(new Offer("productivity_orb", MDKCosmetic.PRODUCTIVITY_ORB::get, 1, 7, 50, EXCLUSIVE));
        offers.add(new Offer("collector_sword", MDKCosmetic.COLLECTOR_SWORD::get, 1, 64, 50, EXCLUSIVE));
        offers.add(new Offer("collector_pickaxe", MDKCosmetic.COLLECTOR_PICKAXE::get, 1, 64, 50, EXCLUSIVE));
        offers.add(new Offer("collector_backpack_death_vault", MDKCosmetic.COLLECTOR_BACKPACK_DEATH_VAULT::get, 1, 32, 50, EXCLUSIVE));
        offers.add(new Offer("collector_backpack_crafting", MDKCosmetic.COLLECTOR_BACKPACK_CRAFTING::get, 1, 1, 1, MISC));
        offers.add(new Offer("collector_backpack_furnace", MDKCosmetic.COLLECTOR_BACKPACK_FURNACE::get, 1, 1, 1, MISC));
        offers.add(new Offer("collector_backpack_totem", MDKCosmetic.COLLECTOR_BACKPACK_TOTEM::get, 1, 3, 3, MISC));
        offers.add(new Offer("collector_backpack_magnet", MDKCosmetic.COLLECTOR_BACKPACK_MAGNET::get, 1, 3, 3, MISC));
        offers.add(new Offer("white_mystery_block", () -> MysteryBlocks.whiteBlockItem(), 64, 1, 5, MISC));
        offers.add(new Offer("netherite_ingot", () -> Items.NETHERITE_INGOT, 1, 10, 10, MISC));
        offers.add(new Offer("diamond", () -> Items.DIAMOND, 1, 2, 1, MISC));
        offers.add(new Offer("gold_ingot", () -> Items.GOLD_INGOT, 16, 1, 1, MISC));
        offers.add(new Offer("iron_ingot", () -> Items.IRON_INGOT, 32, 1, 1, MISC));
        offers.add(new Offer("golden_apple", () -> Items.GOLDEN_APPLE, 1, 1, 1, MISC));
        offers.add(new Offer("fox_fur", MDKCosmetic.FOX_FUR::get, 32, 1, 1, MISC));
        offers.add(new Offer("panda_fur", MDKCosmetic.PANDA_FUR::get, 32, 1, 1, MISC));
        offers.add(new Offer("cat_fur", MDKCosmetic.CAT_FUR::get, 32, 1, 1, MISC));
        offers.add(new Offer("allay_fibers", MDKCosmetic.ALLAY_FIBERS::get, 32, 1, 1, MISC));
        offers.add(new Offer("axolotl_shed", MDKCosmetic.AXOLOTL_SHED::get, 32, 1, 1, MISC));
        offers.add(new Offer("rabbit_fur", MDKCosmetic.RABBIT_FUR::get, 32, 1, 1, MISC));
        offers.add(new Offer("squid_ink_clot", MDKCosmetic.SQUID_INK_CLOT::get, 32, 1, 1, MISC));
        offers.add(new Offer("glow_squid_ink_clot", MDKCosmetic.GLOW_SQUID_INK_CLOT::get, 32, 1, 1, MISC));
        offers.add(new Offer("camel_fur", MDKCosmetic.CAMEL_FUR::get, 32, 1, 1, MISC));
        offers.add(new Offer("cow_fur", MDKCosmetic.COW_FUR::get, 32, 1, 1, MISC));
        offers.add(new Offer("mooshroom_fur", MDKCosmetic.MOOSHROOM_FUR::get, 32, 1, 1, MISC));
        offers.add(new Offer("polar_bear_fur", MDKCosmetic.POLAR_BEAR_FUR::get, 32, 1, 1, MISC));
        offers.add(new Offer("llama_fur", MDKCosmetic.LLAMA_FUR::get, 32, 1, 1, MISC));
        offers.add(new Offer("wolf_fur", MDKCosmetic.WOLF_FUR::get, 32, 1, 1, MISC));
        offers.add(new Offer("horse_fur", MDKCosmetic.HORSE_FUR::get, 32, 1, 1, MISC));
        offers.add(new Offer("donkey_fur", MDKCosmetic.DONKEY_FUR::get, 32, 1, 1, MISC));
        return List.copyOf(offers);
    }
    private static Offer offer(int i) { return OFFERS.get(Math.clamp(i, 0, COUNT - 1)); }
    public static int cost(int i) { return offer(i).cost(); }
    public static int stress(int i) { return offer(i).stress(); }
    public static int category(int i) { return offer(i).category(); }
    public static String id(int i) { return offer(i).name(); }
    /** Resolve a persisted stable offer id, independent of catalogue ordering. */
    public static int indexOfId(String offerId) {
        for (int i=0; i<COUNT; i++) {
            if (offer(i).name().equals(offerId)) return i;
        }
        return -1;
    }

    public static Item product(int i) { return offer(i).item().get(); }
    public static ItemStack preview(int i) { return new ItemStack(product(i), offer(i).count()); }
    public static int count(int category) { int n=0; for (Offer o : OFFERS) if (o.category() == category) n++; return n; }
    public static int indexAt(int category, int position) {
        int k=0;
        for (int i=0; i<COUNT; i++) {
            if (offer(i).category() == category && k++ == position) return i;
        }
        return -1;
    }
}
