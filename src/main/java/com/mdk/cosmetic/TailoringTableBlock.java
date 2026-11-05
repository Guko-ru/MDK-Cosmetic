package com.mdk.cosmetic;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;

/** Tailoring station with a proper, server-authoritative villager-style exchange GUI. */
public final class TailoringTableBlock extends HorizontalDirectionalBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final MapCodec<TailoringTableBlock> CODEC = simpleCodec(TailoringTableBlock::new);
    private static final DustParticleOptions AMBER_SPARK =
            new DustParticleOptions(new Vector3f(1.0F, 0.50F, 0.13F), 0.70F);

    @Override
    protected MapCodec<TailoringTableBlock> codec() {
        return CODEC;
    }

    public TailoringTableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        open(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        open(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    private void open(Level level, BlockPos pos, Player player) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) return;
        // A dedicated Merchant per open GUI means multiple players can use the station independently.
        StationMerchant merchant = new StationMerchant(serverPlayer, level, pos);
        merchant.openTradingScreen(serverPlayer,
                net.minecraft.network.chat.Component.translatable("menu.mdkcosmetic.tailoring_table"), 1);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Purely visual client-side orange thread-motes. No persistent block entity needed.
        if (random.nextInt(5) != 0) return;
        double x = pos.getX() + 0.18 + random.nextDouble() * 0.64;
        double y = pos.getY() + 0.88 + random.nextDouble() * 0.50;
        double z = pos.getZ() + 0.18 + random.nextDouble() * 0.64;
        level.addParticle(AMBER_SPARK, x, y, z,
                (random.nextDouble()-0.5)*0.008, 0.012 + random.nextDouble()*0.012,
                (random.nextDouble()-0.5)*0.008);
    }
}
