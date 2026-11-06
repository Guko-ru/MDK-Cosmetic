package com.mdk.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Stable meteor relic: the Collector pickaxe is the only survival harvesting tool. */
public final class CollectorMeteorBlock extends Block {
    public CollectorMeteorBlock(Properties props) { super(props); }

    @Override public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        if (!player.getAbilities().instabuild && !player.getMainHandItem().is(MDKCosmetic.COLLECTOR_PICKAXE.get()))
            return 0.0F;
        return super.getDestroyProgress(state, player, world, pos);
    }

    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!event.getState().is(MDKCosmetic.COLLECTOR_METEOR_BLOCK.get())) return;
        Player player = event.getPlayer();
        if (player != null && !player.getAbilities().instabuild
                && !player.getMainHandItem().is(MDKCosmetic.COLLECTOR_PICKAXE.get()))
            event.setCanceled(true);
    }
}
