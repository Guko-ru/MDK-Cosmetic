package com.mdk.cosmetic.client;

import com.mdk.cosmetic.MDKCosmetic;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.mdk.cosmetic.client.ClientEvents.ItemTooltipEvent;

/** Extra cosmetic descriptions, visible with Shift; the vanilla armor tooltip is kept intact. */
public final class CosmeticHatTooltip {
    private static final Set<String> MODULES = Set.of(
            "collector_backpack_filter", "collector_backpack_magnet", "collector_backpack_crafting",
            "collector_backpack_furnace", "collector_backpack_totem", "collector_backpack_death_vault");
    private static final Set<String> FRAGMENTS = Set.of("collector_soul", "collector_memory", "collector_backpack_link_key");
    private static final Set<String> HAT_IDS = Set.of(
            "fox_hat", "panda_hat", "cat_ears", "allay_hat", "axolotl_hat", "rabbit_ears",
            "octopus_hat", "glow_squid_hat", "camel_hat", "cow_hat", "mooshroom_hat",
            "polar_bear_hat", "llama_hat", "sheep_hat", "wolf_hat", "horse_hat", "donkey_hat",
            "sakura_thinker");

    private static final Set<String> FUR_IDS = Set.of(
            "fox_fur", "panda_fur", "cat_fur", "allay_fibers", "axolotl_shed",
            "rabbit_fur", "squid_ink_clot", "glow_squid_ink_clot", "camel_fur",
            "cow_fur", "mooshroom_fur", "polar_bear_fur", "llama_fur",
            "wolf_fur", "horse_fur", "donkey_fur", "sheep_fleece");

    private CosmeticHatTooltip() { }

    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().isEmpty()) return;
        // Enchanted books are vanilla items; detect the Collector enchantment from their stored data.
        if (event.getItemStack().is(Items.ENCHANTED_BOOK)) {
            var stored = event.getItemStack().get(DataComponents.STORED_ENCHANTMENTS);
            if (stored != null && stored.entrySet().stream().anyMatch(entry ->
                    entry.getKey().unwrapKey().map(key ->
                            key.location().equals(MDKCosmetic.COLLECTOR_ENCHANTMENT_ID)).orElse(false))) {
                event.getToolTip().add(Component.translatable("tooltip.mdkcosmetic.rework.collector_book")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            return;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (!MDKCosmetic.MOD_ID.equals(id.getNamespace())) return;
        String path = id.getPath();
        String tooltipKey = switch (path) {
            case "tailoring_table" -> "tailoring_table";
            case "essence_worktable" -> "essence_worktable";
            case "power_essence" -> "power_essence";
            case "charged_power_essence" -> "charged_power_essence";
            case "collector_pickaxe" -> "collector_multitool";
            case "collector_sword" -> "collector_sword";
            case "greed_orb" -> "greed_orb";
            case "productivity_orb" -> "productivity_orb";
            default -> null;
        };
        if (tooltipKey != null) {
            event.getToolTip().add(Component.translatable("tooltip.mdkcosmetic.rework." + tooltipKey)
                    .withStyle(path.equals("power_essence") ? ChatFormatting.AQUA
                            : path.equals("charged_power_essence") ? ChatFormatting.LIGHT_PURPLE
                            : ChatFormatting.GRAY));
            return;
        }
        if (MODULES.contains(path)) {
            event.getToolTip().add(Component.translatable("tooltip.mdkcosmetic.rework.backpack_module")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        if (FRAGMENTS.contains(path)) {
            event.getToolTip().add(Component.translatable("tooltip.mdkcosmetic.rework.collector_fragment")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            return;
        }
        if (path.endsWith("_spawn_egg")) {
            event.getToolTip().add(Component.translatable("tooltip.mdkcosmetic.rework.spawn_egg")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        if (id.getPath().equals("collector_helmet") || id.getPath().equals("collector_chestplate")
                || id.getPath().equals("collector_leggings") || id.getPath().equals("collector_boots")) {
            event.getToolTip().add(Component.translatable("item.mdkcosmetic.collector_armor_set_bonus")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            return;
        }
        if (FUR_IDS.contains(id.getPath())) {
            event.getToolTip().add(Component.translatable("tooltip.mdkcosmetic.fur_material")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        if (!HAT_IDS.contains(id.getPath())) return;

        List<Component> lines = event.getToolTip();
        if (id.getPath().equals("sakura_thinker")) {
            lines.add(Component.translatable("tooltip.mdkcosmetic.sakura_thinker.buff")
                    .withStyle(ChatFormatting.AQUA));
            return;
        }

        if (!Screen.hasShiftDown()) {
            lines.add(Component.translatable("tooltip.mdkcosmetic.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        // Some zero-armor cosmetic helmets have no vanilla modifiers to produce
        // a heading. If the vanilla heading is already present, do not duplicate it.
        final String headSection = Component.translatable("item.modifiers.head").getString();
        if (lines.stream().noneMatch(line -> line.getString().equals(headSection))) {
            lines.add(Component.empty());
            lines.add(Component.translatable("item.modifiers.head").withStyle(ChatFormatting.GRAY));
        }
        lines.add(Component.translatable("tooltip.mdkcosmetic.hat." + id.getPath() + ".1")
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.mdkcosmetic.hat." + id.getPath() + ".2")
                .withStyle(ChatFormatting.GRAY));
    }
}
