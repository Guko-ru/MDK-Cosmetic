package com.mdk.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** 5x5x5 upgrade around the existing soul storage. No old casing is replaced. */
public final class CollectorMemoryStructure {
    private CollectorMemoryStructure() {}
    public record Status(boolean formed, boolean unloaded, int missing) {}

    /** The soul center is two blocks ABOVE the memory core in the floor center. */
    public static BlockPos soulCenter(BlockPos memoryCorePos) { return memoryCorePos.above(2); }
    public static BlockPos memoryCorePos(BlockPos soulCenter) { return soulCenter.below(2); }

    /** Edges of the top frame, four side pillars, and an entirely filled floor. */
    public static boolean isCasing(int x, int y, int z) {
        if (y == -2) return x != 0 || z != 0;
        if (y == 2) return Math.abs(x) == 2 || Math.abs(z) == 2;
        return Math.abs(x) == 2 && Math.abs(z) == 2;
    }
    public static boolean isOpenSpace(int x, int y, int z) {
        return (Math.abs(x) == 2 || Math.abs(y) == 2 || Math.abs(z) == 2)
                && !(x == 0 && y == -2 && z == 0) && !isCasing(x,y,z);
    }
    public static Status inspect(Level level, BlockPos soulCenter) {
        int missing=0; boolean unloaded=false;
        for(int x=-2;x<=2;x++) for(int y=-2;y<=2;y++) for(int z=-2;z<=2;z++) {
            boolean core=x==0 && y==-2 && z==0;
            boolean casing=isCasing(x,y,z);
            boolean air=isOpenSpace(x,y,z);
            if(!core && !casing && !air) continue;
            BlockPos p=soulCenter.offset(x,y,z);
            if(!level.hasChunkAt(p)) {unloaded=true;continue;}
            if(core && !level.getBlockState(p).is(MDKCosmetic.COLLECTOR_MEMORY_CORE.get())) missing++;
            else if(casing && !level.getBlockState(p).is(MDKCosmetic.COLLECTOR_MEMORY_CASING.get())) missing++;
            else if(air && !level.getBlockState(p).isAir()) {
                // Optional final-tier shell and activated horizontal portal do not
                // invalidate an already working 5x5 memory machine.
                boolean shell = y != 2 && (Math.abs(x)==2 || Math.abs(z)==2)
                        && level.getBlockState(p).is(MDKCosmetic.COLLECTOR_METEOR_BLOCK.get());
                boolean portal = y == 2 && level.getBlockState(p).is(MDKCosmetic.COLLECTOR_VOID_PORTAL.get());
                if (!shell && !portal) missing++;
            }
        }
        return new Status(missing==0 && !unloaded,unloaded,missing);
    }
    public static boolean ready(Level level, BlockPos soulCenter) {
        if(!(level.getBlockEntity(soulCenter) instanceof CollectorSoulStorageBlockEntity soul)
                || !soul.isAssembled() || soul.getStress()>=100) return false;
        if(!inspect(level,soulCenter).formed()) return false;
        return level.getBlockEntity(memoryCorePos(soulCenter)) instanceof CollectorMemoryCoreBlockEntity;
    }
    public static CollectorMemoryCoreBlockEntity findCore(Level level, BlockPos soulCenter) {
        if(!ready(level,soulCenter)) return null;
        return level.getBlockEntity(memoryCorePos(soulCenter)) instanceof CollectorMemoryCoreBlockEntity core ? core:null;
    }
}
