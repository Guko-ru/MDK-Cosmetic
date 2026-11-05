package com.mdk.cosmetic;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/** The impact site where the Collector lived before the anomaly split him apart. */
public final class CollectorOriginStructure extends Structure {
    public static final MapCodec<CollectorOriginStructure> CODEC =
            Structure.simpleCodec(CollectorOriginStructure::new);

    public CollectorOriginStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        final int x = context.chunkPos().getMinBlockX() + 8;
        final int z = context.chunkPos().getMinBlockZ() + 8;
        final int surface = context.chunkGenerator().getBaseHeight(x, z,
                Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        // Only build the village on dry, comparatively low ground. This leaves mountains
        // and oceans alone and keeps the crater exposed rather than filling with water.
        if (surface < 66 || surface > 106) return Optional.empty();
        final BlockPos origin = new BlockPos(x, surface - 1, z);
        final int minimum = context.heightAccessor().getMinBuildHeight();
        return Optional.of(new GenerationStub(origin, builder ->
                builder.addPiece(new CollectorOriginPiece(x, z, surface - 1, minimum, context.seed()))));
    }

    @Override
    public StructureType<?> type() {
        return MDKCosmetic.COLLECTOR_ORIGIN_STRUCTURE_TYPE.get();
    }
}
