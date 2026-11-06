package com.mdk.cosmetic.jei;

import com.mdk.cosmetic.MDKCosmetic;
import com.mdk.cosmetic.CollectorOffers;
import com.mdk.cosmetic.CollectorMemoryStructure;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.List;

/** JEI-only representations of the actual workstation mechanics. */
public final class JeiData {
    private JeiData() {}
    public record Sewing(ItemStack fur, ItemStack thread, ItemStack result) {}
    public record Processing(ItemStack input, ItemStack result, String note) {}
    public record Trade(ItemStack coins, ItemStack product, int stress) {}
    public record BackpackUpgrade(ItemStack backpack, ItemStack shards, ItemStack coin, ItemStack result) {}
    public record Layer(boolean upgrade, int layer, List<ItemStack> blocks) {
        public int size() { return upgrade ? 5 : 3; }
    }

    public record Cell(int x, int y, int z, ItemStack item, boolean original) {}
    public record Structure(boolean upgrade, boolean finalTier, List<Cell> cells) {
        public int size() { return upgrade ? 5 : 3; }
    }
    /** Coordinates come from the exact same placement rules as CollectorMemoryStructure. */
    public static Structure structure(boolean upgrade) { return structure(upgrade ? 1 : 0); }

    /** Tier 0 = Collector 3x3, tier 1 = Memory 5x5, tier 2 = final VOID shell.
     * Mirrors CollectorVoidPortalController.hasFinalShell: three side walls,
     * y=-1..1, x/z=+/-2, front (z=-2) excluded, corner pillars already exist. */
    public static Structure structure(int tier) {
        boolean upgrade = tier > 0;
        boolean finalTier = tier == 2;
        List<Cell> cells=new ArrayList<>();
        int radius=upgrade?2:1;
        for (int y=-radius;y<=radius;y++) for (int z=-radius;z<=radius;z++) for (int x=-radius;x<=radius;x++) {
            ItemStack block=ItemStack.EMPTY;
            boolean original=Math.abs(x)<=1 && Math.abs(y)<=1 && Math.abs(z)<=1;
            if (original) {
                if(x==0 && y==0 && z==0) block=new ItemStack(MDKCosmetic.COLLECTOR_SOUL_STORAGE_ITEM.get());
                else if(x==0 && y==0 && z==-1) block=new ItemStack(MDKCosmetic.COLLECTOR_LINK_ITEM.get());
                else block=new ItemStack(MDKCosmetic.COLLECTOR_CASING_ITEM.get());
            } else if(upgrade) {
                if (x==0 && y==-2 && z==0) block=new ItemStack(MDKCosmetic.COLLECTOR_MEMORY_CORE_ITEM.get());
                else if (CollectorMemoryStructure.isCasing(x,y,z)) block=new ItemStack(MDKCosmetic.COLLECTOR_MEMORY_CASING_ITEM.get());
            }
            if (finalTier && block.isEmpty() && y >= -1 && y <= 1
                    && (x == -2 || x == 2 || z == 2)
                    && !(Math.abs(x) == 2 && Math.abs(z) == 2)) {
                block = new ItemStack(MDKCosmetic.COLLECTOR_METEOR_BLOCK_ITEM.get());
            }
            if (!block.isEmpty()) cells.add(new Cell(x,y,z,block,original || (finalTier && !block.is(MDKCosmetic.COLLECTOR_METEOR_BLOCK_ITEM.get()))));
        }
        return new Structure(upgrade,finalTier,List.copyOf(cells));
    }

    public static List<Sewing> sewing() {
        List<Sewing> result = new ArrayList<>();
        result.add(new Sewing(new ItemStack(MDKCosmetic.FOX_FUR.get(), 5), new ItemStack(Items.STRING, 3), new ItemStack(MDKCosmetic.FOX_HAT.get())));
        result.add(new Sewing(new ItemStack(MDKCosmetic.PANDA_FUR.get(), 12), new ItemStack(Items.STRING, 5), new ItemStack(MDKCosmetic.PANDA_HAT.get())));
        result.add(new Sewing(new ItemStack(MDKCosmetic.CAT_FUR.get(), 4), new ItemStack(Items.STRING, 2), new ItemStack(MDKCosmetic.CAT_EARS.get())));
        result.add(new Sewing(new ItemStack(MDKCosmetic.ALLAY_FIBERS.get(), 27), new ItemStack(Items.STRING, 7), new ItemStack(MDKCosmetic.ALLAY_HAT.get())));
        result.add(new Sewing(new ItemStack(MDKCosmetic.AXOLOTL_SHED.get(), 8), new ItemStack(Items.STRING, 4), new ItemStack(MDKCosmetic.AXOLOTL_HAT.get())));
        result.add(new Sewing(new ItemStack(MDKCosmetic.RABBIT_FUR.get(), 7), new ItemStack(Items.STRING, 2), new ItemStack(MDKCosmetic.RABBIT_EARS.get())));
        result.add(new Sewing(new ItemStack(MDKCosmetic.SQUID_INK_CLOT.get(), 12), new ItemStack(Items.STRING, 4), new ItemStack(MDKCosmetic.OCTOPUS_HAT.get())));
        result.add(new Sewing(new ItemStack(MDKCosmetic.GLOW_SQUID_INK_CLOT.get(), 10), new ItemStack(Items.STRING, 7), new ItemStack(MDKCosmetic.GLOW_SQUID_HAT.get())));
        result.add(new Sewing(new ItemStack(MDKCosmetic.CAMEL_FUR.get(), 12), new ItemStack(Items.STRING, 5), new ItemStack(MDKCosmetic.CAMEL_HAT.get())));
        result.add(new Sewing(new ItemStack(MDKCosmetic.COW_FUR.get(), 12), new ItemStack(Items.STRING, 5), new ItemStack(MDKCosmetic.COW_HAT.get())));
        result.add(new Sewing(new ItemStack(MDKCosmetic.MOOSHROOM_FUR.get(), 12), new ItemStack(Items.STRING, 5), new ItemStack(MDKCosmetic.MOOSHROOM_HAT.get())));
        result.add(new Sewing(new ItemStack(MDKCosmetic.POLAR_BEAR_FUR.get(), 12), new ItemStack(Items.STRING, 5), new ItemStack(MDKCosmetic.POLAR_BEAR_HAT.get())));
        result.add(new Sewing(new ItemStack(MDKCosmetic.LLAMA_FUR.get(), 12), new ItemStack(Items.STRING, 5), new ItemStack(MDKCosmetic.LLAMA_HAT.get())));
        result.add(new Sewing(new ItemStack(Items.WOOL.pick(net.minecraft.world.item.DyeColor.WHITE), 12), new ItemStack(Items.STRING, 5), new ItemStack(MDKCosmetic.SHEEP_HAT.get())));
        result.add(new Sewing(new ItemStack(MDKCosmetic.WOLF_FUR.get(), 12), new ItemStack(Items.STRING, 5), new ItemStack(MDKCosmetic.WOLF_HAT.get())));
        result.add(new Sewing(new ItemStack(MDKCosmetic.HORSE_FUR.get(), 14), new ItemStack(Items.STRING, 6), new ItemStack(MDKCosmetic.HORSE_HAT.get())));
        result.add(new Sewing(new ItemStack(MDKCosmetic.DONKEY_FUR.get(), 14), new ItemStack(Items.STRING, 6), new ItemStack(MDKCosmetic.DONKEY_HAT.get())));
        return List.copyOf(result);
    }
    public static List<BackpackUpgrade> backpackUpgrades() {
        return List.of(new BackpackUpgrade(
            new ItemStack(MDKCosmetic.COLLECTOR_BACKPACK_BASIC.get()),
            new ItemStack(MDKCosmetic.POWER_ESSENCE.get(),32),
            new ItemStack(MDKCosmetic.CHARGED_POWER_ESSENCE.get()),
            new ItemStack(MDKCosmetic.COLLECTOR_BACKPACK_REINFORCED.get())));
    }
    public static List<Processing> processing() {
        return List.of(new Processing(new ItemStack(MDKCosmetic.POWER_ESSENCE.get(), 64),
                new ItemStack(MDKCosmetic.CHARGED_POWER_ESSENCE.get()),
                "64 shards -> 1 coin; progress is retained"));
    }
    public static List<Trade> trades() {
        List<Trade> result = new ArrayList<>();
        for (int i = 0; i < CollectorOffers.COUNT; i++) {
            result.add(new Trade(new ItemStack(MDKCosmetic.CHARGED_POWER_ESSENCE.get(), CollectorOffers.cost(i)),
                    CollectorOffers.preview(i), CollectorOffers.stress(i)));
        }
        return List.copyOf(result);
    }
    /** Layers are bottom-up. Facing front: near side is z=-1, controller in middle height. */
    public static List<Layer> layers(boolean upgrade) {
        List<Layer> result = new ArrayList<>();
        int radius = upgrade ? 2 : 1;
        for (int y = -radius; y <= radius; y++) {
            List<ItemStack> slots = new ArrayList<>();
            for (int z=-radius; z<=radius; z++) for (int x=-radius; x<=radius; x++) {
                ItemStack block = ItemStack.EMPTY;
                if (Math.abs(x)<=1 && Math.abs(y)<=1 && Math.abs(z)<=1) {
                    if (x==0 && y==0 && z==0) block = new ItemStack(MDKCosmetic.COLLECTOR_SOUL_STORAGE_ITEM.get());
                    else if (x==0 && y==0 && z==-1) block = new ItemStack(MDKCosmetic.COLLECTOR_LINK_ITEM.get());
                    else block = new ItemStack(MDKCosmetic.COLLECTOR_CASING_ITEM.get());
                } else if (upgrade) {
                    if (x==0 && y==-2 && z==0) block = new ItemStack(MDKCosmetic.COLLECTOR_MEMORY_CORE_ITEM.get());
                    else if (CollectorMemoryStructure.isCasing(x,y,z)) block = new ItemStack(MDKCosmetic.COLLECTOR_MEMORY_CASING_ITEM.get());
                }
                slots.add(block);
            }
            result.add(new Layer(upgrade, y+radius+1, List.copyOf(slots)));
        }
        return List.copyOf(result);
    }
}
