package com.mdk.cosmetic;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.Containers;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;

/** Decorative artisan worktable with a persistent 64-unit essence charging interface. */
public final class EssenceWorktableBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final MapCodec<EssenceWorktableBlock> CODEC = simpleCodec(EssenceWorktableBlock::new);
    private static final DustParticleOptions ESSENCE_SPARK =
            new DustParticleOptions(net.minecraft.util.ARGB.colorFromFloat(1F,0.66F, 0.28F, 0.95F), 0.65F);

    @Override
    protected MapCodec<EssenceWorktableBlock> codec() {
        return CODEC;
    }

    public EssenceWorktableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Match the player-facing front, exactly as our tailoring table does.
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.TRY_WITH_EMPTY_HAND;
        open(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        open(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EssenceWorktableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide() || type != MDKCosmetic.ESSENCE_WORKTABLE_BE.get()) return null;
        return (currentLevel, blockPos, blockState, blockEntity) -> {
            if (blockEntity instanceof EssenceWorktableBlockEntity table)
                EssenceWorktableBlockEntity.serverTick(currentLevel, blockPos, blockState, table);
        };
    }


    private void open(Level level, BlockPos pos, Player player) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) return;
        if (level.getBlockEntity(pos) instanceof EssenceWorktableBlockEntity table) {
            serverPlayer.openMenu(table);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Purely visual client-side particles; actual progress is persisted in the block entity.
        if (random.nextInt(5) != 0) return;
        double x = pos.getX() + 0.18 + random.nextDouble() * 0.64;
        double y = pos.getY() + 0.88 + random.nextDouble() * 0.50;
        double z = pos.getZ() + 0.18 + random.nextDouble() * 0.64;
        level.addParticle(ESSENCE_SPARK, x, y, z,
                (random.nextDouble()-0.5)*0.008, 0.012 + random.nextDouble()*0.012,
                (random.nextDouble()-0.5)*0.008);
    }
}
