package com.mdk.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** One of 25 external blocks in the collector's 3x3x3 multiblock. */
public final class CollectorCasingBlock extends Block {
    public CollectorCasingBlock(Properties properties) { super(properties); }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            CollectorSoulStorageBlockEntity.disassembleNear(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
