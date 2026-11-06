package com.mdk.cosmetic;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;

/** Underwater guardian-derived boss with 250 HP, decoy and final lightning discharge. */
public final class CollectorHeartEntity extends Guardian {
    private boolean decoy;
    private boolean cloneCreated;
    private boolean finalPhase;
    private int dischargeTicks;
    private int blastCooldown;
    private int weaknessCooldown;
    private boolean droppedKey;

    public CollectorHeartEntity(EntityType<? extends Guardian> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Guardian.createAttributes()
                .add(Attributes.MAX_HEALTH, 250.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.75D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    public boolean isDecoy() { return decoy; }
    public boolean hasCreatedClone() { return cloneCreated; }
    public boolean hasEnteredFinalPhase() { return finalPhase; }
    public void makeDecoy() {
        decoy = true;
        cloneCreated = true;
        finalPhase = true;
        setHealth(10.0F); // 5 hearts
        var damage = getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) damage.setBaseValue(4.0D);
    }

    @Override public void aiStep() {
        super.aiStep();
        if (level().isClientSide() || !(level() instanceof ServerLevel level) || !isAlive()) return;
        if (decoy) return;
        if (blastCooldown > 0) blastCooldown--;
        if (weaknessCooldown > 0) weaknessCooldown--;
        if (dischargeTicks > 0) {
            setDeltaMovement(Vec3.ZERO);
            getNavigation().stop();
            if (dischargeTicks % 20 == 0) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 0.6, getZ(), 70, 12, 5, 12, 0.08);
                level.playSound(null, blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.HOSTILE, 0.55f, 1.75f);
                for (net.minecraft.world.entity.Mob mob : level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
                        getBoundingBox().inflate(30), mob -> mob != this && mob.isAlive())) {
                    mob.setTarget(this); // Even allied guardians turn hostile during the discharge.
                }
                for (Player p : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(30),
                        p -> p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) <= 900)) {
                    p.hurt(damageSources().magic(), 2.0f); // 1 heart per second
                }
            }
            dischargeTicks--;
            return;
        }
        Player player = level.getNearestPlayer(this, 32);
        if (player == null || player.isSpectator() || !player.isAlive()) return;
        if (getHealth() <= 10.0f && !finalPhase) {
            finalPhase = true;
            blinkAway(level, player, 10.0);
            dischargeTicks = 200; // 10 seconds, all damageable during the cast
            level.playSound(null, blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.HOSTILE, 2f, 0.8f);
            return;
        }
        if (getHealth() <= 125.0f && !cloneCreated) {
            cloneCreated = true;
            CollectorHeartEntity illusion = MDKCosmetic.COLLECTOR_HEART_ENTITY.get().create(level);
            if (illusion != null) {
                illusion.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
                illusion.makeDecoy();
                level.addFreshEntity(illusion);
            }
            blinkAway(level, player, 5.0);
            return;
        }
        if (weaknessCooldown == 0 && distanceToSqr(player) <= 144) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
            weaknessCooldown = 160;
        }
        if (blastCooldown == 0 && distanceToSqr(player) <= 4.0D) {
            blastCooldown = 400;
            level.sendParticles(ParticleTypes.BUBBLE, getX(), getY(), getZ(), 80, 2, 1, 2, 0.23);
            level.playSound(null, blockPosition(), SoundEvents.GUARDIAN_ATTACK, SoundSource.HOSTILE, 1.5f, 0.65f);
            for (Player p : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(2),
                    p -> p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) <= 4)) {
                p.hurt(damageSources().mobAttack(this), 8.0f);
                Vec3 away = p.position().subtract(position()).normalize().scale(1.35);
                p.push(away.x, 0.45, away.z);
            }
        }
        // Guardian's native aquatic navigator and attacking goals remain active.
    }

    @Override public void travel(Vec3 movement) {
        if (dischargeTicks > 0) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }
        super.travel(movement);
    }

    private void blinkAway(ServerLevel level, Player target, double distance) {
        Vec3 away = position().subtract(target.position());
        if (away.lengthSqr() < 0.1) away = new Vec3(1, 0, 0);
        away = away.normalize();
        Vec3 proposed = position().add(away.scale(distance));
        for (int n = 0; n < 8; n++) {
            var pos = net.minecraft.core.BlockPos.containing(proposed.x, proposed.y, proposed.z);
            if (level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER)) {
                teleportTo(proposed.x, proposed.y, proposed.z);
                level.sendParticles(ParticleTypes.BUBBLE, proposed.x, proposed.y, proposed.z, 35, 1, 1, 1, 0.08);
                return;
            }
            proposed = proposed.add(0, n % 2 == 0 ? 1 : -1, 0);
        }
    }

    @Override public void die(DamageSource source) {
        if (!level().isClientSide() && !decoy && !droppedKey && getHealth() <= 0) {
            droppedKey = true;
            spawnAtLocation(new ItemStack(MDKCosmetic.COLLECTOR_MEMORY.get()));
        }
        super.die(source);
    }

    @Override public boolean canAttackType(EntityType<?> type) {
        return type != MDKCosmetic.COLLECTOR_HEART_ENTITY.get()
                && type != EntityType.GUARDIAN && type != EntityType.ELDER_GUARDIAN
                && super.canAttackType(type);
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Decoy", decoy);
        tag.putBoolean("CloneCreated", cloneCreated);
        tag.putBoolean("FinalPhase", finalPhase);
        tag.putInt("DischargeTicks", dischargeTicks);
        tag.putInt("BlastCooldown", blastCooldown);
        tag.putInt("WeaknessCooldown", weaknessCooldown);
        tag.putBoolean("DroppedKey", droppedKey);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        decoy = tag.getBoolean("Decoy");
        cloneCreated = tag.getBoolean("CloneCreated");
        finalPhase = tag.getBoolean("FinalPhase");
        dischargeTicks = tag.getInt("DischargeTicks");
        blastCooldown = tag.getInt("BlastCooldown");
        weaknessCooldown = tag.getInt("WeaknessCooldown");
        droppedKey = tag.getBoolean("DroppedKey");
    }
}
