package com.mdk.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Floating return beam, activated by passing through it. */
public final class CollectorVoidReturnBlock extends Block {
    public CollectorVoidReturnBlock(Properties props) { super(props); }

    @Override public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,net.minecraft.world.entity.InsideBlockEffectApplier effects) {
        if (entity instanceof ServerPlayer player && !player.isSpectator())
            CollectorVoidPortalController.leave(player);
    }
}
