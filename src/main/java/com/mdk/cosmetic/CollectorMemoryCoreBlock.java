package com.mdk.cosmetic;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/** The soul's expanded storage persists inside the dropped core item. */
public final class CollectorMemoryCoreBlock extends Block implements EntityBlock {
    public CollectorMemoryCoreBlock(Properties props){super(props);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){
        return new CollectorMemoryCoreBlockEntity(pos,state);
    }
    @Override public List<ItemStack> getDrops(BlockState state,LootParams.Builder params) {
        ItemStack stack=new ItemStack(MDKCosmetic.COLLECTOR_MEMORY_CORE_ITEM.get());
        BlockEntity entity=params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if(entity instanceof CollectorMemoryCoreBlockEntity core && entity.getLevel()!=null)
            CollectorMemoryCoreItem.writeMemory(stack,core.exportMemory(entity.getLevel().registryAccess()));
        return List.of(stack);
    }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){
        if(level.isClientSide()||type!=MDKCosmetic.COLLECTOR_MEMORY_CORE_BE.get())return null;
        return (world,pos,current,be)->{
            if(be instanceof CollectorMemoryCoreBlockEntity core)CollectorMemoryCoreBlockEntity.serverTick(world,pos,core);
        };
    }
}
