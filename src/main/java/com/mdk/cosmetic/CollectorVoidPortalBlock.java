package com.mdk.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Horizontal 3x3 non-solid End-like VOID gateway. */
public final class CollectorVoidPortalBlock extends Block {
    public CollectorVoidPortalBlock(Properties props) { super(props); }

    @Override public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof ServerPlayer player && !player.isSpectator())
            CollectorVoidPortalController.enter(player, pos);
    }
}
