package com.mdk.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Collection of Mysteries multiblock.
 * Front face: 8 casings and a controller in the center.
 * Middle slice: 8 casings and the soul storage in the exact center.
 * Back face: 9 casings.
 *
 * The controller's FACING points out of the front face; the storage is
 * one block behind the controller. No blocks are consumed on formation.
 */
public final class CollectorStructure {
    private CollectorStructure() {}

    public record Check(boolean formed, int missingCasings, boolean missingSoulStorage,
                        boolean unloadedPart) {}

    public static Check inspect(Level level, BlockPos controllerPos, Direction outward) {
        if (!outward.getAxis().isHorizontal())
            return new Check(false, 25, true, false);
        final BlockPos center = controllerPos.relative(outward.getOpposite());
        int missingCasings = 0;
        boolean storageMissing = false;
        boolean unloaded = false;
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                for (int z = -1; z <= 1; ++z) {
                    BlockPos checkPos = center.offset(x, y, z);
                    if (checkPos.equals(controllerPos)) continue;
                    if (!level.hasChunkAt(checkPos)) {
                        unloaded = true;
                        continue;
                    }
                    if (checkPos.equals(center)) {
                        if (!level.getBlockState(checkPos).is(MDKCosmetic.COLLECTOR_SOUL_STORAGE.get()))
                            storageMissing = true;
                    } else if (!level.getBlockState(checkPos).is(MDKCosmetic.COLLECTOR_CASING.get())) {
                        ++missingCasings;
                    }
                }
            }
        }
        return new Check(!unloaded && !storageMissing && missingCasings == 0,
                missingCasings, storageMissing, unloaded);
    }
}
