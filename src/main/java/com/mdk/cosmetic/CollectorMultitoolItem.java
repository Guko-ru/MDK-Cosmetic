package com.mdk.cosmetic;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Tool;

/** One native mining tool for stone, wood and soil. Keep the old registry ID. */
public final class CollectorMultitoolItem extends Item {
    public static final float SPEED = 11.0F; // Netherite speed 9 + 2.

    public CollectorMultitoolItem(Properties properties) {
        super(properties.enchantable(20).repairable(net.minecraft.world.item.Items.NETHERITE_INGOT));
    }



    public static Tool createToolComponent() {
        return new Tool(List.of(
            new Tool.Rule(BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK).getOrThrow(BlockTags.INCORRECT_FOR_NETHERITE_TOOL), Optional.empty(), Optional.of(false)),
            new Tool.Rule(BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK).getOrThrow(BlockTags.MINEABLE_WITH_PICKAXE), Optional.of(SPEED), Optional.of(true)),
            new Tool.Rule(BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK).getOrThrow(BlockTags.MINEABLE_WITH_AXE), Optional.of(SPEED), Optional.of(true)),
            new Tool.Rule(BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK).getOrThrow(BlockTags.MINEABLE_WITH_SHOVEL), Optional.of(SPEED), Optional.of(true))
        ), 1.0F, 1,true);
    }
}
