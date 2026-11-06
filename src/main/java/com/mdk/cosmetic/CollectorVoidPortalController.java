package com.mdk.cosmetic;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

/** Server-only gateway lifecycle, activation and returning home. */
public final class CollectorVoidPortalController {
    public static final ResourceKey<Level> VOID = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(MDKCosmetic.MOD_ID, "void"));
    private CollectorVoidPortalController() {}

    private static Direction connectorFace(ServerLevel level, BlockPos soulPos) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockState state = level.getBlockState(soulPos.relative(side));
            if (state.is(MDKCosmetic.COLLECTOR_LINK.get())
                    && state.getValue(CollectorLinkBlock.FACING) == side) return side;
        }
        return null;
    }

    /** A complete side shell, leaving the front face and top open. */
    public static boolean hasFinalShell(ServerLevel level, BlockPos soulPos) {
        if (!CollectorMemoryStructure.ready(level, soulPos)) return false;
        Direction face = connectorFace(level, soulPos);
        if (face == null) return false;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (side == face) continue;
            for (int y = -1; y <= 1; y++) for (int lateral = -1; lateral <= 1; lateral++) {
                int x = side.getStepX() * 2 + side.getStepZ() * lateral;
                int z = side.getStepZ() * 2 + side.getStepX() * lateral;
                // Four vertical corner pillars belong to the existing casing frame.
                if (Math.abs(x) == 2 && Math.abs(z) == 2) continue;
                BlockPos p = soulPos.offset(x, y, z);
                if (!level.hasChunkAt(p) || !level.getBlockState(p).is(MDKCosmetic.COLLECTOR_METEOR_BLOCK.get()))
                    return false;
            }
        }
        return true;
    }

    /** Invoked by the Memory core ticker every 16 server ticks, no chunk loading. */
    public static void tickCore(ServerLevel level, BlockPos corePos, CollectorMemoryCoreBlockEntity memory) {
        BlockPos soul = CollectorMemoryStructure.soulCenter(corePos);
        BlockPos portalCenter = soul.above(2);
        boolean active = hasFinalShell(level, soul);
        if (active) {
            // Award only players actually near the completed structure.
            for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class,
                    new AABB(soul).inflate(15.0D))) {
                CollectorVoidAchievements.onShellReady(player);
            }
        }
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            BlockPos p = portalCenter.offset(dx, 0, dz);
            if (!level.hasChunkAt(p)) continue;
            BlockState state = level.getBlockState(p);
            if (active && state.isAir())
                level.setBlock(p, MDKCosmetic.COLLECTOR_VOID_PORTAL.get().defaultBlockState(), 3);
            else if (!active && state.is(MDKCosmetic.COLLECTOR_VOID_PORTAL.get()))
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private static CollectorMemoryCoreBlockEntity memoryBelowPortal(ServerLevel level, BlockPos portal) {
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            BlockPos corePos = portal.offset(x, -4, z);
            if (!(level.getBlockEntity(corePos) instanceof CollectorMemoryCoreBlockEntity memory)) continue;
            if (hasFinalShell(level, CollectorMemoryStructure.soulCenter(corePos))) return memory;
        }
        return null;
    }

    public static void enter(ServerPlayer player, BlockPos portalPos) {
        if (!(player.level() instanceof ServerLevel origin) || origin.dimension().equals(VOID)) return;
        CollectorMemoryCoreBlockEntity memory = memoryBelowPortal(origin, portalPos);
        if (memory == null) return;
        ServerLevel destination = origin.getServer().getLevel(VOID);
        if (destination == null) {
            player.displayClientMessage(Component.translatable("message.mdkcosmetic.void_unavailable"), true);
            return;
        }
        UUID id = memory.identity();
        CollectorVoidInstances state = CollectorVoidInstances.get(destination);
        BlockPos center = CollectorVoidInstances.center(state.reserve(id));
        // Load only the relevant landing chunk; do not generate neighboring realms.
        destination.getChunkAt(center);
        ensurePlatform(destination, center);
        boolean first = state.markWelcomed(id);
        Vec3 landing = new Vec3(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5);
        origin.playSound(null, portalPos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.7f, 1.4f);
        player.teleport(new TeleportTransition(destination, landing, Vec3.ZERO,
                player.getYRot(), player.getXRot(), TeleportTransition.PLAY_PORTAL_SOUND));
        CollectorVoidAchievements.onEnter(player);
        if (first) {
            // Vanilla subtitles use a medium-size font, unlike the huge main title.
            player.connection.send(new ClientboundSetTitlesAnimationPacket(15, 90, 25));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(" ")));
            player.connection.send(new ClientboundSetSubtitleTextPacket(
                    Component.translatable("message.mdkcosmetic.void_welcome")
                            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
            player.sendSystemMessage(Component.translatable("message.mdkcosmetic.void_creator_thanks"));
        }
    }

    private static void ensurePlatform(ServerLevel level, BlockPos center) {
        for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
            BlockPos p = center.offset(dx, 0, dz);
            if (level.getBlockState(p).isAir()) level.setBlock(p, Blocks.BEDROCK.defaultBlockState(), 3);
        }
        // Floating return gateway on the east edge, styled as a vertical End-like beam.
        for (int dy = 1; dy <= 3; dy++) {
            BlockPos p = center.offset(3, dy, 0);
            if (level.getBlockState(p).isAir())
                level.setBlock(p, MDKCosmetic.COLLECTOR_VOID_RETURN.get().defaultBlockState(), 3);
        }
    }

    public static void leave(ServerPlayer player) {
        if (!player.level().dimension().equals(VOID)) return;
        // Return specifically to the Overworld, never to a Nether respawn anchor.
        ServerLevel overworld = player.getServer().overworld();
        if (player.getRespawnDimension().equals(Level.OVERWORLD)) {
            // Vanilla validates the saved bed and falls back to the world spawn if missing.
            player.teleport(player.findRespawnPositionAndUseSpawnBlock(
                    false, TeleportTransition.PLAY_PORTAL_SOUND));
        } else {
            BlockPos spawn = overworld.getSharedSpawnPos();
            player.teleport(new TeleportTransition(overworld,
                    Vec3.atBottomCenterOf(spawn).add(0.0, 1.0, 0.0), Vec3.ZERO,
                    player.getYRot(), player.getXRot(), TeleportTransition.PLAY_PORTAL_SOUND));
        }
        CollectorVoidAchievements.onReturn(player);
    }
}
