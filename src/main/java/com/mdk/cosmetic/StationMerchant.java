package com.mdk.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

/** A non-entity merchant for the sewing station. Payment/result are handled by vanilla MerchantMenu. */
public final class StationMerchant implements Merchant {
    private final Level level;
    private final BlockPos pos;
    private final MerchantOffers offers = new MerchantOffers();
    private Player tradingPlayer;

    public StationMerchant(Player player, Level level, BlockPos pos) {
        this.tradingPlayer = player;
        this.level = level;
        this.pos = pos.immutable();
        offers.add(new MerchantOffer(
                new ItemCost(MDKCosmetic.FOX_FUR.get(), 5),
                Optional.of(new ItemCost(Items.STRING, 3)),
                new ItemStack(MDKCosmetic.FOX_HAT.get()),
                999999, 0, 0.0F));
        offers.add(new MerchantOffer(
                new ItemCost(MDKCosmetic.PANDA_FUR.get(), 12),
                Optional.of(new ItemCost(Items.STRING, 5)),
                new ItemStack(MDKCosmetic.PANDA_HAT.get()),
                999999, 0, 0.0F));
        offers.add(new MerchantOffer(
                new ItemCost(MDKCosmetic.CAT_FUR.get(), 4),
                Optional.of(new ItemCost(Items.STRING, 2)),
                new ItemStack(MDKCosmetic.CAT_EARS.get()),
                999999, 0, 0.0F));
        offers.add(new MerchantOffer(
                new ItemCost(MDKCosmetic.ALLAY_FIBERS.get(), 27),
                Optional.of(new ItemCost(Items.STRING, 7)),
                new ItemStack(MDKCosmetic.ALLAY_HAT.get()),
                999999, 0, 0.0F));
        offers.add(new MerchantOffer(
                new ItemCost(MDKCosmetic.AXOLOTL_SHED.get(), 8),
                Optional.of(new ItemCost(Items.STRING, 4)),
                new ItemStack(MDKCosmetic.AXOLOTL_HAT.get()),
                999999, 0, 0.0F));
        offers.add(new MerchantOffer(
                new ItemCost(MDKCosmetic.RABBIT_FUR.get(), 7),
                Optional.of(new ItemCost(Items.STRING, 2)),
                new ItemStack(MDKCosmetic.RABBIT_EARS.get()),
                999999, 0, 0.0F));
        offers.add(new MerchantOffer(
                new ItemCost(MDKCosmetic.SQUID_INK_CLOT.get(), 12),
                Optional.of(new ItemCost(Items.STRING, 4)),
                new ItemStack(MDKCosmetic.OCTOPUS_HAT.get()),
                999999, 0, 0.0F));
        offers.add(new MerchantOffer(
                new ItemCost(MDKCosmetic.GLOW_SQUID_INK_CLOT.get(), 10),
                Optional.of(new ItemCost(Items.STRING, 7)),
                new ItemStack(MDKCosmetic.GLOW_SQUID_HAT.get()),
                999999, 0, 0.0F));
        offers.add(new MerchantOffer(
                new ItemCost(MDKCosmetic.CAMEL_FUR.get(), 12),
                Optional.of(new ItemCost(Items.STRING, 5)),
                new ItemStack(MDKCosmetic.CAMEL_HAT.get()),
                999999, 0, 0.0F));
        offers.add(new MerchantOffer(
                new ItemCost(MDKCosmetic.COW_FUR.get(), 12),
                Optional.of(new ItemCost(Items.STRING, 5)),
                new ItemStack(MDKCosmetic.COW_HAT.get()),
                999999, 0, 0.0F));
        offers.add(new MerchantOffer(
                new ItemCost(MDKCosmetic.MOOSHROOM_FUR.get(), 12),
                Optional.of(new ItemCost(Items.STRING, 5)),
                new ItemStack(MDKCosmetic.MOOSHROOM_HAT.get()),
                999999, 0, 0.0F));
        offers.add(new MerchantOffer(
                new ItemCost(MDKCosmetic.POLAR_BEAR_FUR.get(), 12),
                Optional.of(new ItemCost(Items.STRING, 5)),
                new ItemStack(MDKCosmetic.POLAR_BEAR_HAT.get()),
                999999, 0, 0.0F));
        offers.add(new MerchantOffer(
                new ItemCost(MDKCosmetic.LLAMA_FUR.get(), 12),
                Optional.of(new ItemCost(Items.STRING, 5)),
                new ItemStack(MDKCosmetic.LLAMA_HAT.get()),
                999999, 0, 0.0F));
        // Only WHITE_WOOL is used for crafting; dye the finished hat in a workbench.
        offers.add(new MerchantOffer(
                new ItemCost(Items.WHITE_WOOL, 12),
                Optional.of(new ItemCost(Items.STRING, 5)),
                new ItemStack(MDKCosmetic.SHEEP_HAT.get()),
                999999, 0, 0.0F));
        offers.add(new MerchantOffer(
                new ItemCost(MDKCosmetic.WOLF_FUR.get(), 12),
                Optional.of(new ItemCost(Items.STRING, 5)),
                new ItemStack(MDKCosmetic.WOLF_HAT.get()),
                999999, 0, 0.0F));
        offers.add(new MerchantOffer(
                new ItemCost(MDKCosmetic.HORSE_FUR.get(), 14),
                Optional.of(new ItemCost(Items.STRING, 6)),
                new ItemStack(MDKCosmetic.HORSE_HAT.get()),
                999999, 0, 0.0F));
        offers.add(new MerchantOffer(
                new ItemCost(MDKCosmetic.DONKEY_FUR.get(), 14),
                Optional.of(new ItemCost(Items.STRING, 6)),
                new ItemStack(MDKCosmetic.DONKEY_HAT.get()),
                999999, 0, 0.0F));
    }

    @Override public void setTradingPlayer(@Nullable Player player) { this.tradingPlayer = player; }
    @Override @Nullable public Player getTradingPlayer() { return tradingPlayer; }
    @Override public MerchantOffers getOffers() { return offers; }
    @Override public void overrideOffers(MerchantOffers updated) { /* Station defines the prices server-side. */ }
    @Override public void notifyTrade(MerchantOffer offer) {
        if (!level.isClientSide()) {
            if (tradingPlayer instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                CollectorAdvancementEvents.onTailorTrade(serverPlayer, offer.getResult());
            }
            level.playSound(null, pos, SoundEvents.VILLAGER_WORK_SHEPHERD,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.7F, 1.15F);
        }
    }
    @Override public void notifyTradeUpdated(ItemStack stack) { }
    @Override public int getVillagerXp() { return 0; }
    @Override public void overrideXp(int xp) { }
    @Override public boolean showProgressBar() { return false; }
    @Override public SoundEvent getNotifyTradeSound() { return SoundEvents.VILLAGER_WORK_SHEPHERD; }
    @Override public boolean isClientSide() { return level.isClientSide(); }
    @Override public boolean canRestock() { return false; }
}
