package com.mdk.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.StructureManager;

/**
 * Chunk-clipped, deterministic terrain and ruins. A single registered structure piece is
 * saved in structure NBT, which makes /locate structure and revisiting generated chunks work.
 * No cross-chunk direct writes: every call edits only its supplied 16x16 chunk bounds.
 */
public final class CollectorOriginPiece extends StructurePiece {
    private static final int RADIUS = 188;
    private static final int CRATER_RADIUS = 105;
    private static final int RIM_RADIUS = 124;
    private static final int[] HOUSE_X = { -135, -110, -58, 54, 117, 144, 26, -32, 91 };
    private static final int[] HOUSE_Z = { 25, -91, -135, -130, -78, 28, 150, 145, 111 };

    private final int centerX;
    private final int centerZ;
    private final int surfaceY;
    private final int minY;
    private final long seed;

    public CollectorOriginPiece(int centerX, int centerZ, int surfaceY, int minY, long seed) {
        super(MDKCosmetic.COLLECTOR_ORIGIN_PIECE_TYPE.get(), 0,
                new BoundingBox(centerX - RADIUS, minY, centerZ - RADIUS,
                        centerX + RADIUS, surfaceY + 55, centerZ + RADIUS));
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.surfaceY = surfaceY;
        this.minY = minY;
        this.seed = seed;
    }

    public CollectorOriginPiece(CompoundTag tag) {
        super(MDKCosmetic.COLLECTOR_ORIGIN_PIECE_TYPE.get(), tag);
        centerX = tag.getIntOr("OriginX",0);
        centerZ = tag.getIntOr("OriginZ",0);
        surfaceY = tag.getIntOr("SurfaceY",0);
        minY = tag.getIntOr("WorldMinY",0);
        seed = tag.getLongOr("OriginSeed",0L);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("OriginX", centerX);
        tag.putInt("OriginZ", centerZ);
        tag.putInt("SurfaceY", surfaceY);
        tag.putInt("WorldMinY", minY);
        tag.putLong("OriginSeed", seed);
    }

    private long hash(int x, int y, int z, int salt) {
        long h = seed ^ ((long) x * 341873128712L) ^ ((long) z * 132897987541L)
                ^ ((long) y * 42317861L) ^ ((long) salt * 0x9E3779B97F4A7C15L);
        h ^= h >>> 30;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 27;
        h *= 0x94D049BB133111EBL;
        return h ^ (h >>> 31);
    }

    private int roll(int x, int y, int z, int salt, int mod) {
        return (int) Math.floorMod(hash(x, y, z, salt), mod);
    }

    private BlockState palette(int x, int z, double distance) {
        int n = roll(x / 3, 0, z / 3, 7, 100);
        if (distance < 62) {
            if (n < 38) return Blocks.SCULK.defaultBlockState();
            if (n < 59) return Blocks.END_STONE.defaultBlockState();
            if (n < 76) return Blocks.NETHERRACK.defaultBlockState();
            if (n < 88) return Blocks.TUFF.defaultBlockState();
            return Blocks.BLACKSTONE.defaultBlockState();
        }
        if (distance < RIM_RADIUS) {
            if (n < 24) return Blocks.SCULK.defaultBlockState();
            if (n < 43) return Blocks.NETHERRACK.defaultBlockState();
            if (n < 61) return Blocks.END_STONE.defaultBlockState();
            if (n < 82) return Blocks.TUFF.defaultBlockState();
            return Blocks.COBBLESTONE.defaultBlockState();
        }
        if (n < 11) return Blocks.SCULK.defaultBlockState();
        if (n < 19) return Blocks.NETHERRACK.defaultBlockState();
        if (n < 29) return Blocks.END_STONE.defaultBlockState();
        if (n < 40) return Blocks.COARSE_DIRT.defaultBlockState();
        return Blocks.GRASS_BLOCK.defaultBlockState();
    }

    private BlockState ore(double distance, int x, int y, int z) {
        // Exposed vein clusters in the crater walls, with a smaller enrichment halo below.
        int cellX = Math.floorDiv(x, 3), cellZ = Math.floorDiv(z, 3);
        if (distance > 22 && distance < 107 && roll(cellX, 0, cellZ, 91, 275) == 0
                && roll(x, y, z, 92, 4) != 0)
            return y < 0 ? MDKCosmetic.COLLECTOR_ORE_DEEPSLATE.get().defaultBlockState()
                    : MDKCosmetic.COLLECTOR_ORE_STONE.get().defaultBlockState();
        return null;
    }

    private void set(WorldGenLevel level, BoundingBox chunk, BlockPos.MutableBlockPos pos,
                     int x, int y, int z, BlockState state) {
        if (y <= minY + 4 || y >= level.getMaxY() || !chunk.isInside(pos.set(x, y, z))) return;
        level.setBlock(pos, state, 2);
    }

    /** Remnants of nine houses: interrupted foundations, uneven walls and no roofs. */
    private void ruinedVillage(WorldGenLevel level, BoundingBox chunk, BlockPos.MutableBlockPos pos,
                               int x, int z, int surface) {
        for (int i = 0; i < HOUSE_X.length; ++i) {
            int dx = x - (centerX + HOUSE_X[i]);
            int dz = z - (centerZ + HOUSE_Z[i]);
            if (Math.abs(dx) > 5 || Math.abs(dz) > 5) continue;
            int foundation = Math.min(surfaceY + 1, Math.max(surfaceY - 4, surface));
            // The foundation is broken into mossy cobblestone and coarse dirt.
            if (roll(x, foundation, z, i + 11, 11) != 0)
                set(level, chunk, pos, x, foundation, z,
                        roll(x, foundation, z, 123, 3) == 0
                                ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                                : Blocks.COBBLESTONE.defaultBlockState());
            boolean edge = Math.abs(dx) == 5 || Math.abs(dz) == 5;
            if (!edge) {
                if (roll(x, 1, z, 183 + i, 13) == 0)
                    set(level, chunk, pos, x, foundation + 1, z, Blocks.OAK_PLANKS.defaultBlockState());
                continue;
            }
            int height = roll(x / 2, 0, z / 2, i + 176, 5) + 1;
            if (Math.abs(dx) == 5 && Math.abs(dz) == 5) height += 2;
            for (int h = 1; h <= height; ++h) {
                if (roll(x, h, z, 97 + i, 9) == 0) continue;
                BlockState wall = (Math.abs(dx) == 5 && Math.abs(dz) == 5)
                        ? Blocks.STRIPPED_OAK_LOG.defaultBlockState()
                        : (roll(x, h, z, 202 + i, 5) == 0
                            ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                            : Blocks.OAK_PLANKS.defaultBlockState());
                set(level, chunk, pos, x, foundation + h, z, wall);
            }
            // Empty windows and broken doors are left deliberately.
            if (Math.abs(dx) == 5 && dz == 0)
                for (int h = 1; h <= 2; ++h)
                    set(level, chunk, pos, x, foundation + h, z, Blocks.AIR.defaultBlockState());
        }
    }

    private void meteor(WorldGenLevel level, BoundingBox chunk, BlockPos.MutableBlockPos pos,
                        int x, int z, double distance) {
        if (distance > 19) return;
        int bottom = minY + 5;
        // The exposed core rises out of the lowest, bedrock-adjacent crater floor.
        for (int y = bottom; y <= bottom + 24; ++y) {
            double dx = (x - centerX) / 16.0;
            double dz = (z - centerZ) / 15.0;
            double dy = (y - (bottom + 10)) / 11.0;
            double surface = dx * dx + dz * dz + dy * dy;
            double noise = (roll(x / 2, y / 2, z / 2, 913, 11) - 5) * 0.013;
            if (surface > 1.0 + noise) continue;
            int kind = roll(x / 3, y / 3, z / 3, 400, 19);
            BlockState material = kind < 3 ? Blocks.END_STONE.defaultBlockState()
                    : kind == 3 ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                    : MDKCosmetic.COLLECTOR_METEOR_BLOCK.get().defaultBlockState();
            set(level, chunk, pos, x, y, z, material);
        }
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator,
                            RandomSource random, BoundingBox chunk, ChunkPos chunkPos, BlockPos origin) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int x0 = Math.max(centerX - RADIUS, chunkPos.getMinBlockX());
        int x1 = Math.min(centerX + RADIUS, chunkPos.getMaxBlockX());
        int z0 = Math.max(centerZ - RADIUS, chunkPos.getMinBlockZ());
        int z1 = Math.min(centerZ + RADIUS, chunkPos.getMaxBlockZ());
        for (int x = x0; x <= x1; ++x) for (int z = z0; z <= z1; ++z) {
            double dx = x - centerX, dz = z - centerZ;
            double distance = Math.sqrt(dx * dx + dz * dz);
            double waviness = (roll(x / 9, 0, z / 9, 11, 15) - 7) * 0.42;
            double r = distance + waviness;
            if (r > RADIUS) continue;

            int natural = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
            if (natural <= minY + 5) continue;
            int floor = minY + 6;
            int target = natural;
            if (r < CRATER_RADIUS) {
                double t = Math.max(0.0, r / CRATER_RADIUS);
                target = floor + (int) Math.round((surfaceY - floor + 11) * Math.pow(t, 2.18));
            } else if (r < RIM_RADIUS) {
                double t = (r - CRATER_RADIUS) / (RIM_RADIUS - CRATER_RADIUS);
                target = surfaceY + (int) Math.round(11 * (1 - t) + 2 * t);
            }
            target = Math.max(floor, Math.min(level.getMaxY() - 8, target));

            if (r < RIM_RADIUS) {
                // Carve old terrain instead of placing a flat disk; keep vanilla bedrock.
                // Clearing a little extra space also removes low caves and underground water.
                int high = Math.min(level.getMaxY() - 1, Math.max(natural, target) + 2);
                for (int y = target + 1; y <= high; ++y)
                    set(level, chunk, pos, x, y, z, Blocks.AIR.defaultBlockState());
                if (natural < target) {
                    for (int y = natural + 1; y <= target; ++y)
                        set(level, chunk, pos, x, y, z, Blocks.TUFF.defaultBlockState());
                }
                BlockState exposed = palette(x, z, r);
                BlockState oreState = ore(r, x, target, z);
                set(level, chunk, pos, x, target, z, oreState == null ? exposed : oreState);
                // Broken seams visible in the crater walls and near its bottom.
                if (r > 24 && r < 98 && roll(x, 1, z, 409, 350) == 0)
                    set(level, chunk, pos, x, target - 1, z,
                            MDKCosmetic.COLLECTOR_ORE_DEEPSLATE.get().defaultBlockState());
            } else {
                // The infected transition is intentionally irregular, not a colored circle.
                int chance = (int) (28 + 70 * (RADIUS - r) / (RADIUS - RIM_RADIUS));
                if (roll(x / 2, 0, z / 2, 55, 100) < chance) {
                    set(level, chunk, pos, x, natural, z, palette(x, z, r));
                    if (roll(x, 0, z, 300, 19) == 0)
                        set(level, chunk, pos, x, natural - 1, z, Blocks.TUFF.defaultBlockState());
                }
                if (r > 128 && r < 160) ruinedVillage(level, chunk, pos, x, z, natural);
            }

            // The village used to have a crossroads. Only broken stretches remain.
            if (r >= 126 && r < 163 && (Math.abs(dx) < 2.3 || Math.abs(dz) < 2.3)
                    && roll(x, 0, z, 119, 5) != 0) {
                set(level, chunk, pos, x, natural, z,
                        roll(x, 0, z, 120, 4) == 0
                                ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                                : Blocks.COARSE_DIRT.defaultBlockState());
            }
            // Scattered fragments and exposed stone around the impact rim.
            if (r >= 113 && r < 152 && roll(x, 0, z, 88, 145) == 0) {
                int height = 1 + roll(x, 0, z, 89, 4);
                for (int h = 1; h <= height; ++h)
                    set(level, chunk, pos, x, target + h, z,
                            h == height ? Blocks.END_STONE.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState());
            }
            // Additional collector ore clusters in the underground halo, not every tunnel.
            if (r >= 110 && r < 171 && roll(x / 3, 0, z / 3, 804, 370) == 0) {
                int y = minY + 12 + roll(x, 0, z, 805, Math.max(1, natural - minY - 24));
                if (y < natural - 4 && y > minY + 5) {
                    BlockState targetState = y < 0 ? MDKCosmetic.COLLECTOR_ORE_DEEPSLATE.get().defaultBlockState()
                            : MDKCosmetic.COLLECTOR_ORE_STONE.get().defaultBlockState();
                    BlockState existing = level.getBlockState(pos.set(x, y, z));
                    if (existing.is(Blocks.STONE) || existing.is(Blocks.DEEPSLATE) || existing.is(Blocks.TUFF))
                        set(level, chunk, pos, x, y, z, targetState);
                }
            }
            meteor(level, chunk, pos, x, z, r);
        }
    }
}
