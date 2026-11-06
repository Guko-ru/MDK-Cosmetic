package com.mdk.cosmetic;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import com.mdk.cosmetic.platform.RegistryEntry;
import com.mdk.cosmetic.platform.RegistryBatch;

/** Flat, fully opaque mystery building blocks. Exact RGB textures (no noise or shading). */
public final class MysteryBlocks {
    private MysteryBlocks() {}
    public static final List<RegistryEntry<Item,BlockItem>> CREATIVE_ITEMS = new ArrayList<>();
    private static RegistryEntry<Item,BlockItem> white;
    public static void register(RegistryBatch<Block> blocks, RegistryBatch<Item> items) {
        String[] dyes = {"white","orange","magenta","light_blue","yellow","lime","pink","gray","light_gray","cyan","purple","blue","brown","green","red","black"};
        for (String color : dyes) {
            String base = color + "_mystery_block";
            var full = blocks.register(base, () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE).strength(2.2F, 6.0F)));
            var slab = blocks.register(color + "_mystery_slab", () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE).strength(2.2F, 6.0F)));
            var stairs = blocks.register(color + "_mystery_stairs", () -> new StairBlock(full.get().defaultBlockState(),
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(2.2F, 6.0F)));
            var fullItem = items.register(base, () -> new BlockItem(full.get(), new Item.Properties()));
            var slabItem = items.register(color + "_mystery_slab", () -> new BlockItem(slab.get(), new Item.Properties()));
            var stairsItem = items.register(color + "_mystery_stairs", () -> new BlockItem(stairs.get(), new Item.Properties()));
            CREATIVE_ITEMS.add(fullItem);
            CREATIVE_ITEMS.add(stairsItem);
            CREATIVE_ITEMS.add(slabItem);
            if (color.equals("white")) white = fullItem;
        }
    }
    public static Item whiteBlockItem() { return white.get(); }
}
