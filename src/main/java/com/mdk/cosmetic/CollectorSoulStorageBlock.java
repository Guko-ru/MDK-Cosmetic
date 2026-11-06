package com.mdk.cosmetic;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** The core of the 3x3x3 machine, responsible for persistent stress. */
public final class CollectorSoulStorageBlock extends Block implements EntityBlock {
    @Override public java.util.List<net.minecraft.world.item.ItemStack> getDrops(BlockState state,
            net.minecraft.world.level.storage.loot.LootParams.Builder params) {
        net.minecraft.world.item.ItemStack result=new net.minecraft.world.item.ItemStack(MDKCosmetic.COLLECTOR_SOUL_STORAGE_ITEM.get());
        var be=params.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        if (be instanceof CollectorSoulStorageBlockEntity storage)
            CollectorStorageItem.setStress(result,storage.getStress(),storage.everActivated());
        return java.util.List.of(result);
    }

    public CollectorSoulStorageBlock(Properties properties) { super(properties); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CollectorSoulStorageBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide() || type != MDKCosmetic.COLLECTOR_SOUL_STORAGE_BE.get()) return null;
        return (world, pos, current, entity) -> {
            if (entity instanceof CollectorSoulStorageBlockEntity storage) {
                CollectorSoulStorageBlockEntity.serverTick(world, pos, storage);
            }
        };
    }
}
