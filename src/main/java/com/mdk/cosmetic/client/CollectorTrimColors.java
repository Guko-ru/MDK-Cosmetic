package com.mdk.cosmetic.client;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

/** Maps vanilla smithing trim materials to the Collector armor's secondary accent color.
 * The black metal is on the first, non-tintable material layer. The trim is also
 * independently rendered by Minecraft's built-in ArmorTrim rendering pipeline.
 */
public final class CollectorTrimColors {
    private static final int DEFAULT_PURPLE = 0xFF9761B6;
    private CollectorTrimColors() { }

    public static int color(ItemStack stack) {
        ArmorTrim trim = stack.get(DataComponents.TRIM);
        if (trim == null) return DEFAULT_PURPLE;
        // Use registry IDs rather than matching translated item names or tooltips.
        String material = trim.material().unwrapKey()
                .map(key -> key.location().getPath()).orElse("");
        return switch (material) {
            case "quartz" -> 0xFFE9E6DB;
            case "iron" -> 0xFFB8C5D0;
            case "netherite" -> 0xFF4F4656;
            case "redstone" -> 0xFFF03737;
            case "copper" -> 0xFFDC8759;
            case "gold" -> 0xFFF0C53C;
            case "emerald" -> 0xFF39CE6B;
            case "diamond" -> 0xFF55D4DD;
            case "lapis" -> 0xFF426ED9;
            case "amethyst" -> 0xFFA96ACA;
            default -> DEFAULT_PURPLE;
        };
    }
}
