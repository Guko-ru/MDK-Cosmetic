package com.mdk.cosmetic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Soul storage stress state. The machine only triggers on a transition from
 * incomplete to complete, not on every tick. The SavedData mirror prevents
 * loss of stress if the player breaks/replaces the storage at this position.
 */
public final class CollectorSoulStorageBlockEntity extends BlockEntity {
    private static final int WAVE_TICKS = 20;
    private static final double WAVE_RADIUS = 20.0;
    private int stress;
    private boolean assembled;
    private boolean everActivated;
    private int waveTicks;
    private final Set<UUID> waveHit = new HashSet<>();

    public CollectorSoulStorageBlockEntity(BlockPos pos, BlockState state) {
        super(MDKCosmetic.COLLECTOR_SOUL_STORAGE_BE.get(), pos, state);
    }

    public int getStress() { return stress; }
    public boolean everActivated() { return everActivated; }
    public boolean isAssembled() { return assembled; }
    public void setFromItem(int value, boolean activated) {
        stress=Math.clamp(value,0,100);
        everActivated=activated;
        assembled=false;
        setChanged();
    }
    public void reduceStress(int amount) {
        if (amount<=0 || !(level instanceof ServerLevel)) return;
        stress=Math.max(0,stress-amount);
        setChanged();
    }

    /** Future collector-link orders can call this on the SERVER to add their price. */
    public void addStress(int amount) {
        if (amount <= 0 || !(level instanceof ServerLevel server)) return;
        if (stress >= 100) return;
        stress = Math.min(100, stress + amount);
        setChanged();
        if (stress >= 100 && assembled) meltdown(server);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        stress = Math.clamp(tag.getIntOr("CollectorStress",0), 0, 100);
        assembled = tag.getBooleanOr("CollectorAssembled",false);
        everActivated = tag.getBooleanOr("CollectorEverActivated",false);
        waveTicks = 0; // visual effect does not resume after a server restart
        waveHit.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("CollectorStress", stress);
        tag.putBoolean("CollectorAssembled", assembled);
        tag.putBoolean("CollectorEverActivated", everActivated);
    }

    /** Called when a casing/controller is actually removed, even within one tick. */
    public static void disassembleNear(Level level, BlockPos removedPos) {
        if (!(level instanceof ServerLevel)) return;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos candidate = removedPos.offset(x, y, z);
                    if (!level.hasChunkAt(candidate)) continue;
                    if (level.getBlockEntity(candidate) instanceof CollectorSoulStorageBlockEntity core
                            && core.assembled) {
                        core.assembled = false;
                        core.setChanged();
                    }
                }
            }
        }
    }

    public static void serverTick(Level world, BlockPos pos, CollectorSoulStorageBlockEntity storage) {
        if (!(world instanceof ServerLevel level)) return;
        if (storage.waveTicks > 0) storage.tickShockwave(level);
        // Detect assembly at most 4 times a second without loading any chunks.
        if ((level.getGameTime() + (pos.asLong() & 3)) % 5 != 0) return;
        boolean formed = storage.isComplete(level);
        if (formed && !storage.assembled) {
            storage.onAssembly(level);
        } else if (!formed && storage.assembled && storage.isKnownIncomplete(level)) {
            storage.assembled = false;
            storage.setChanged();
        }
    }

    /** True only when there is a valid, outward-facing link on one face. */
    private boolean isComplete(ServerLevel level) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos linkPos = worldPosition.relative(side);
            if (!level.hasChunkAt(linkPos)) continue;
            BlockState state = level.getBlockState(linkPos);
            if (!state.is(MDKCosmetic.COLLECTOR_LINK.get())) continue;
            if (state.getValue(CollectorLinkBlock.FACING) != side) continue;
            if (CollectorStructure.inspect(level, linkPos, side).formed()) return true;
        }
        return false;
    }

    /** Do not treat an unloaded piece as disassembly (prevents free repeated charges). */
    private boolean isKnownIncomplete(ServerLevel level) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos linkPos = worldPosition.relative(side);
            if (!level.hasChunkAt(linkPos)) return false;
            BlockState state = level.getBlockState(linkPos);
            if (state.is(MDKCosmetic.COLLECTOR_LINK.get())
                    && state.getValue(CollectorLinkBlock.FACING) == side
                    && CollectorStructure.inspect(level, linkPos, side).unloadedPart()) return false;
        }
        return true;
    }

    private void onAssembly(ServerLevel level) {
        boolean first = !everActivated;
        assembled = true;
        everActivated = true;
        // Ten stress points = ten percent of total stability, on every rebuild.
        stress = Math.min(100, stress + 10);
        setChanged();
        if (stress >= 100) {
            meltdown(level);
            return;
        }
        {
            waveTicks = WAVE_TICKS;
            waveHit.clear();
            Vec3 center = Vec3.atCenterOf(worldPosition);
            level.playSound(null, worldPosition, SoundEvents.WARDEN_SONIC_BOOM,
                    SoundSource.BLOCKS, 2.0F, 0.65F);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z,
                    120, 0.8, 0.8, 0.8, 0.10);
            if (first) CollectorTitles.announce(level, center, "title.mdkcosmetic.awakening", ChatFormatting.LIGHT_PURPLE);
        }
    }

    /** Expand a one-time 20 block wave, pushing each living entity only once. */
    private void tickShockwave(ServerLevel level) {
        double radius = WAVE_RADIUS * (WAVE_TICKS - waveTicks + 1) / WAVE_TICKS;
        --waveTicks;
        Vec3 center = Vec3.atCenterOf(worldPosition);
        if (waveTicks % 2 == 0 || waveTicks == 1) {
            for (int i = 0; i < 40; i++) {
                double angle = 2.0 * Math.PI * i / 40;
                level.sendParticles(i % 4 == 0 ? ParticleTypes.END_ROD : ParticleTypes.REVERSE_PORTAL,
                        center.x + Math.cos(angle) * radius, center.y + 0.05,
                        center.z + Math.sin(angle) * radius,
                        1, 0.0, 0.05, 0.0, 0.012);
            }
        }
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(worldPosition).inflate(WAVE_RADIUS + 2),
                e -> e.isAlive() && !(e instanceof Warden) && !e.isSpectator())) {
            double dx = victim.getX() - center.x;
            double dz = victim.getZ() - center.z;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > radius + 0.5 || distance > WAVE_RADIUS
                    || !waveHit.add(victim.getUUID())) continue;
            if (distance < 0.001) {
                double a = level.random.nextDouble() * Math.PI * 2;
                dx = Math.cos(a);
                dz = Math.sin(a);
                distance = 1.0;
            }
            double power = 3.4 + (WAVE_RADIUS - distance) * 0.035;
            victim.setDeltaMovement(dx / distance * power,
                    Math.max(0.65, victim.getDeltaMovement().y), dz / distance * power);
            victim.hurtMarked = true;
            victim.hasImpulse = true;
            level.sendParticles(ParticleTypes.CLOUD, victim.getX(), victim.getY() + 0.5,
                    victim.getZ(), 18, 0.4, 0.4, 0.4, 0.1);
        }
        if (waveTicks == 0) waveHit.clear();
    }

    private void meltdown(ServerLevel level) {
        if (!assembled) return;
        assembled = false;
        setChanged();
        Vec3 center = Vec3.atCenterOf(worldPosition);
        // Clear first, then remove the machine. A rebuilt machine starts fresh.
        List<BlockPos> parts = new ArrayList<>(27);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos part = worldPosition.offset(x, y, z);
                    if (level.hasChunkAt(part)) parts.add(part.immutable());
                }
            }
        }
        // Remove only our multiblock parts, never arbitrary neighboring blocks.
        for (BlockPos part : parts) {
            BlockState state = level.getBlockState(part);
            if (state.is(MDKCosmetic.COLLECTOR_CASING.get())
                    || state.is(MDKCosmetic.COLLECTOR_LINK.get())
                    || state.is(MDKCosmetic.COLLECTOR_SOUL_STORAGE.get())) {
                level.removeBlock(part, false);
            }
        }
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                center.x, center.y, center.z, 160, 1.7, 1.7, 1.7, 0.18);
        level.explode(null, center.x, center.y, center.z, 3.0F, Level.ExplosionInteraction.BLOCK);
        CollectorSoulEntity soul = MDKCosmetic.COLLECTOR_SOUL_ENTITY.get().create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (soul != null) {
            soul.snapTo(center.x, center.y + 0.5, center.z,
                    level.random.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(soul);
        }
    }
}
