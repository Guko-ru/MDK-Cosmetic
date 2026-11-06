package com.mdk.cosmetic;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import com.mdk.cosmetic.platform.EnchantedBooks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import java.util.Optional;

/**
 * Adds independent offers with exact per-trader probabilities on first interaction.
 * The persistent marker prevents rerolling on repeated right clicks, saves and reloads.
 */
public final class CollectorWanderingTrader {
    private static final String ROLLED = "mdkcosmetic_collector_trades_rolled";
    private CollectorWanderingTrader() { }

    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof WanderingTrader trader)
                || !(event.getEntity() instanceof ServerPlayer player)) return;
        if (com.mdk.cosmetic.platform.PersistentData.of(trader).getBooleanOr(ROLLED,false)) return;
        // Set marker BEFORE generating offers so a nested interaction cannot roll again.
        com.mdk.cosmetic.platform.PersistentData.of(trader).putBoolean(ROLLED, true);
        MerchantOffers offers = trader.getOffers();
        var random = trader.getRandom();

        // Exact independent 0.1% roll per trader. One use only, 64 emeralds.
        if (random.nextInt(1000) == 0) {
            offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 64), Optional.empty(),
                    new ItemStack(MDKCosmetic.CHARGED_POWER_ESSENCE.get()), 1, 0, 0.0F));
        }
        // Independent 10% chance for the unique "Collector" enchanted book.
        if (random.nextInt(10) == 0) {
            var key = ResourceKey.create(Registries.ENCHANTMENT,
                    Identifier.fromNamespaceAndPath(MDKCosmetic.MOD_ID, "collector"));
            player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
                    .ifPresent(holder -> offers.add(new MerchantOffer(
                            new ItemCost(Items.EMERALD, 18 + random.nextInt(15)), Optional.empty(),
                            EnchantedBooks.createForEnchantment(new EnchantmentInstance(holder, 1)),
                            1, 0, 0.0F)));
        }
        // A rare Sakura hat offer.
        if (random.nextInt(10) == 0) {
            offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 24 + random.nextInt(9)),
                    Optional.empty(), new ItemStack(MDKCosmetic.SAKURA_THINKER.get()), 1, 0, 0.0F));
        }
    }
}
