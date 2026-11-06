package com.mdk.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.Vec3;
import com.mdk.cosmetic.platform.Events.LivingChangeTargetEvent;
import com.mdk.cosmetic.platform.Events.ServerTickEvent;
import com.mdk.cosmetic.platform.Events.EntityTickEvent;

/** Natural one-time generation per ancient city, and Warden neutrality. */
public final class CollectorSoulWorldEvents {
    private CollectorSoulWorldEvents() {}
    public static void onServerTick(ServerTickEvent.Post event) {
        // Only scan once every 5 seconds to avoid expensive structure lookup.
        if (event.getServer().getTickCount()%100!=0) return;
        for (ServerLevel level:event.getServer().getAllLevels()) {
            if (level.dimension()!=net.minecraft.world.level.Level.OVERWORLD) continue;
            Structure city=level.registryAccess().lookupOrThrow(Registries.STRUCTURE)
                    .getOrThrow(BuiltinStructures.ANCIENT_CITY).value();
            CollectorSoulCityData data=CollectorSoulCityData.forLevel(level);
            for (ServerPlayer player:level.players()) {
                if (player.isSpectator() || player.getY()>0) continue;
                BlockPos pos=player.blockPosition();
                StructureStart start=level.structureManager().getStructureWithPieceAt(pos,city);
                if (!start.isValid()) continue;
                String id=level.dimension().identifier()+":"+start.getChunkPos().x()+":"+start.getChunkPos().z();
                if (data.contains(id)) continue;
                // Prefer a nearby safe air space inside the actual structure.
                for (int attempt=0;attempt<48;attempt++) {
                    int dx=level.getRandom().nextInt(29)-14, dy=level.getRandom().nextInt(11)-5;
                    int dz=level.getRandom().nextInt(29)-14;
                    BlockPos dest=pos.offset(dx,dy,dz);
                    if (!level.hasChunkAt(dest) || !level.getBlockState(dest).isAir()) continue;
                    if (!level.getBlockState(dest.above()).isAir()) continue;
                    if (!level.structureManager().getStructureWithPieceAt(dest,city).isValid()) continue;
                    CollectorSoulEntity soul=MDKCosmetic.COLLECTOR_SOUL_ENTITY.get().create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
                    if (soul==null) break;
                    soul.snapTo(dest.getX()+0.5,dest.getY()+0.25,dest.getZ()+0.5,
                            level.getRandom().nextFloat()*360,0.0F);
                    if (!level.noCollision(soul,soul.getBoundingBox())) continue;
                    if (level.addFreshEntity(soul)) data.claim(id);
                    break;
                }
            }
        }
    }
    /**
     * Warden target selection can use brain memories and anger independently.
     * This is the final, defensive guard against selecting the Collector Soul.
     * It never cancels player targeting or the Warden's usual investigation.
     */
    public static void onWardenChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Warden
                && event.getNewAboutToBeSetTarget() instanceof CollectorSoulEntity) {
            event.setCanceled(true);
        }
    }

    /**
     * Warden anger isn't the same as Mob#setTarget. Previous versions only
     * cancelled the target-change event and left an angry suspect behind.
     * Clear our soul's entry in AngerManagement and both combat-target brain
     * memories after every Warden tick. Other suspects remain unaffected.
     */
    public static void onWardenTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Warden warden)
                || !(warden.level() instanceof ServerLevel level)) return;

        // Clear stale brain targets first, even if the mob just teleported.
        boolean targetingSoul = warden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET)
                .filter(target -> target instanceof CollectorSoulEntity).isPresent();
        boolean roaringAtSoul = warden.getBrain().getMemory(MemoryModuleType.ROAR_TARGET)
                .filter(target -> target instanceof CollectorSoulEntity).isPresent();
        if (targetingSoul) {
            warden.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
        if (roaringAtSoul) {
            warden.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
        }
        if (targetingSoul || roaringAtSoul) {
            warden.getNavigation().stop();
        }

        // Warden vibration/smell anger is stored separately from its AI target.
        // There is normally only one soul per ancient city, so this is cheap.
        for (CollectorSoulEntity soul : level.getEntitiesOfClass(
                CollectorSoulEntity.class, warden.getBoundingBox().inflate(64.0),
                Entity::isAlive)) {
            warden.clearAnger(soul);
        }
    }
}
