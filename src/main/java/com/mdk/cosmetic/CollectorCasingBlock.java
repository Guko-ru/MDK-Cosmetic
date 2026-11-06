package com.mdk.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** One of 25 external blocks in the collector's 3x3x3 multiblock. */
public final class CollectorCasingBlock extends Block {
    public CollectorCasingBlock(Properties properties) { super(properties); }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (!level.isClientSide()) {
            CollectorSoulStorageBlockEntity.disassembleNear(level, pos);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
}
