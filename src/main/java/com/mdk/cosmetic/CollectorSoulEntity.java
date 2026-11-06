package com.mdk.cosmetic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Floating Ancient City mimic. Its combat casts use a 20-tick audio warning,
 * followed by vanilla projectiles / a visual sonic beam / visual lightning.
 * A Warden remains an ally and is never targeted by the mimic's attacks.
 */
public final class CollectorSoulEntity extends PathfinderMob {
    private static final int VISION = 16;
    private static final int COMBAT_VISION = 32;
    private static final int TELEPORT_CD = 200; // 10 seconds
    private static final int ARROW_CD = 100; // 5 seconds
    private static final int POISON_CD = 600; // 30 seconds
    private static final int SONIC_CD = 600; // 30 seconds
    private static final int LIGHTNING_CD = 1200; // 60 seconds
    private static final int SPELL_WINDUP = 20; // sound comes exactly 1s before effect
    private static final int MIMIC_DURATION = 100; // five seconds
    private static final int WAVE_DURATION = 20; // one block per tick to radius 20
    private static final double WAVE_RADIUS = 20.0;

    private enum Spell { NONE, ARROW, POISON, SONIC, LIGHTNING }
    private Spell pendingSpell = Spell.NONE;
    private int spellWindup;
    @Nullable private UUID spellTarget;
    private int arrowCooldown;
    private int poisonCooldown;
    private int sonicCooldown;
    private int teleportCooldown;
    private int lightningCooldown;
    private int mimicTicks;
    private int waveTicks;
    private final Set<UUID> waveHit = new HashSet<>();
    private int flyCooldown;
    private int unseenTicks;
    private boolean aggro;
    // Highest spoken health-stage index. Persisted in NBT; a line is never repeated.
    private int highestTauntStage = -1;
    private boolean provoking;
    private boolean hasSummonedGuardian;
    private Vec3 flightTarget = Vec3.ZERO;
    @Nullable private BlockPos lastShrieker;

    public CollectorSoulEntity(EntityType<? extends CollectorSoulEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setPersistenceRequired(); // one per city must not despawn when players leave
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 160.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18)
                .add(Attributes.FLYING_SPEED, 0.24)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.15);
    }

    @Override protected void registerGoals() {
        // Bat-like drift is controlled by custom server tick, not vanilla ground pathing.
    }

    @Override public boolean isAlliedTo(Entity entity) {
        return entity instanceof Warden || super.isAlliedTo(entity);
    }

    /** No body-to-body push contact that could make a Warden suspicious. */
    @Override public boolean isPushable() { return false; }

    @Override public boolean dampensVibrations() {
        // The soul's idle flight is invisible to sculk; only its deliberate decoys have effects.
        return true;
    }

    @Override protected @Nullable SoundEvent getAmbientSound() { return null; }
    @Override protected @Nullable SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return SoundEvents.ALLAY_HURT; }
    @Override protected @Nullable SoundEvent getDeathSound() { return SoundEvents.ALLAY_DEATH; }

    @Override public void travel(Vec3 travelVector) {
        // No gravity, no land navigation. Collision still applies to walls and floor.
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.86));
    }

    @Override public void aiStep() {
        super.aiStep();
        this.setNoGravity(true);
        if (this.level().isClientSide()) {
            if (this.random.nextInt(3)==0) {
                double dx=(this.random.nextDouble()-0.5)*1.05;
                double dz=(this.random.nextDouble()-0.5)*1.05;
                this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX()+dx,
                        this.getY()+0.28+(this.random.nextDouble()-0.5)*0.5,
                        this.getZ()+dz, 0, 0.004, 0);
            }
            return;
        }
        if (!(this.level() instanceof ServerLevel level)) return;
        if (teleportCooldown>0) --teleportCooldown;
        if (arrowCooldown>0) --arrowCooldown;
        if (poisonCooldown>0) --poisonCooldown;
        if (sonicCooldown>0) --sonicCooldown;
        if (lightningCooldown>0) --lightningCooldown;

        if (waveTicks>0) tickShockwave(level);
        // A spell cannot fire during its own audio wind-up. Cancel on death/unload.
        if (pendingSpell!=Spell.NONE && --spellWindup<=0) finishSpell(level);

        // The soul is silent and passive until first detecting a player from 16 blocks.
        // Once provoked, it tracks visible enemies out to 32 blocks, including after blink.
        ServerPlayer player=nearestVisiblePlayer(level, aggro ? COMBAT_VISION : VISION);
        if (player!=null) {
            unseenTicks=0;
            if (!aggro) aggro=true;
            if (!hasSummonedGuardian) provoking=true;
        } else {
            ++unseenTicks;
            if (unseenTicks>60) aggro=false;
            if (unseenTicks>600) {
                provoking=false;
                hasSummonedGuardian=false;
            }
        }

        boolean wardenPresent=!level.getEntitiesOfClass(Warden.class,
                getBoundingBox().inflate(48.0),Entity::isAlive).isEmpty();
        if (wardenPresent) {
            provoking=false;
            hasSummonedGuardian=true;
            mimicTicks=0;
        }

        // Use one cast at a time, with one second between cue and attack.
        // Spell cooldowns start at the warning, preventing a reload exploit.
        if (player!=null && pendingSpell==Spell.NONE) {
            if (poisonCooldown==0 && distanceToSqr(player)<=8.0*8.0) {
                beginSpell(level,Spell.POISON,player);
            } else if (sonicCooldown==0 && wardenPresent && distanceToSqr(player)<=24.0*24.0) {
                beginSpell(level,Spell.SONIC,player);
            } else if (lightningCooldown==0 && distanceToSqr(player)<=24.0*24.0) {
                beginSpell(level,Spell.LIGHTNING,player);
            } else if (arrowCooldown==0 && distanceToSqr(player)<=COMBAT_VISION*COMBAT_VISION) {
                beginSpell(level,Spell.ARROW,player);
            }
        }
        // Do not blink away midway through a wind-up: attacks remain legible.
        if (player!=null && pendingSpell==Spell.NONE && teleportCooldown==0) {
            if (blinkToNearestShrieker(level,player)) {
                teleportCooldown=TELEPORT_CD;
                if (!hasSummonedGuardian) {
                    mimicTicks=MIMIC_DURATION;
                    startShrieker(level,player);
                }
            }
        }
        if (mimicTicks>0 && provoking && !hasSummonedGuardian) {
            if (mimicTicks==MIMIC_DURATION || mimicTicks%9==0) playMimic(level);
            if (mimicTicks==8 && player!=null) startShrieker(level,player);
            --mimicTicks;
        }
        drift(level,player);
        if (tickCount%6==0) {
            level.sendParticles(ParticleTypes.SOUL,getX(),getY()+0.45,getZ(),
                    2,0.30,0.24,0.30,0.012);
        }
    }

    /** Send the distinctive vanilla sound before queuing a cast. */
    private void beginSpell(ServerLevel level, Spell spell, ServerPlayer player) {
        pendingSpell=spell;
        spellWindup=SPELL_WINDUP;
        spellTarget=player.getUUID();
        SoundEvent cue;
        switch (spell) {
            case ARROW -> {
                arrowCooldown=ARROW_CD;
                cue=SoundEvents.SKELETON_AMBIENT;
            }
            case POISON -> {
                poisonCooldown=POISON_CD;
                cue=SoundEvents.WITCH_CELEBRATE;
            }
            case SONIC -> {
                sonicCooldown=SONIC_CD;
                cue=SoundEvents.WARDEN_SONIC_CHARGE;
            }
            case LIGHTNING -> {
                lightningCooldown=LIGHTNING_CD;
                cue=SoundEvents.LIGHTNING_BOLT_THUNDER;
            }
            default -> { return; }
        }
        level.playSound(null, blockPosition(), cue, SoundSource.HOSTILE, 1.65F, 1.0F);
        level.sendParticles(spell==Spell.SONIC ? ParticleTypes.SCULK_SOUL :
                spell==Spell.POISON ? ParticleTypes.WITCH : ParticleTypes.ENCHANT,
                getX(),getY()+0.6,getZ(),18,0.40,0.40,0.40,0.045);
    }

    /** Resolve at exactly 20 ticks after the audio warning; do not strike escaped players. */
    private void finishSpell(ServerLevel level) {
        Spell spell=pendingSpell;
        UUID id=spellTarget;
        pendingSpell=Spell.NONE;
        spellTarget=null;
        spellWindup=0;
        if (id==null || spell==Spell.NONE || !this.isAlive()) return;
        ServerPlayer target=level.getServer().getPlayerList().getPlayer(id);
        if (target==null || target.level()!=level || !target.isAlive()
                || target.isCreative() || target.isSpectator()) return;
        double range=switch(spell) {
            case POISON -> 10.0;
            case SONIC,LIGHTNING -> 26.0;
            case ARROW -> 34.0;
            default -> 0.0;
        };
        if (distanceToSqr(target)>range*range) return;
        // Warden-style sonic boom goes through walls; projectiles require line of sight.
        if ((spell==Spell.ARROW || spell==Spell.POISON) && !hasLineOfSight(target)) return;
        switch(spell) {
            case ARROW -> fireArrow(level,target);
            case POISON -> throwPoison(level,target);
            case SONIC -> {
                if (!level.getEntitiesOfClass(Warden.class,getBoundingBox().inflate(48.0),Entity::isAlive).isEmpty())
                    fireSonicBoom(level,target);
            }
            case LIGHTNING -> strikeLightning(level,target);
            default -> { }
        }
    }

    /** Actual vanilla projectile, cannot hit the allied Warden. */
    private void fireArrow(ServerLevel level,ServerPlayer target) {
        Arrow arrow=new Arrow(level,this,new ItemStack(Items.ARROW),null) {
            @Override protected boolean canHitEntity(Entity entity) {
                return !(entity instanceof Warden) && !(entity instanceof CollectorSoulEntity)
                        && super.canHitEntity(entity);
            }
        };
        arrow.setPos(getX(),getY()+0.55,getZ());
        Vec3 aim=target.getEyePosition().subtract(arrow.position());
        arrow.shoot(aim.x,aim.y+aim.horizontalDistance()*0.065,aim.z,1.8F,2.5F);
        arrow.setBaseDamage(2.5);
        arrow.pickup=AbstractArrow.Pickup.DISALLOWED;
        level.addFreshEntity(arrow);
        level.playSound(null,blockPosition(),SoundEvents.ARROW_SHOOT,SoundSource.HOSTILE,1.25F,1.0F);
        level.sendParticles(ParticleTypes.CRIT,getX(),getY()+0.5,getZ(),12,0.2,0.2,0.2,0.08);
    }

    /** Throw a real splash poison potion, complete with vanilla bottle trajectory and impact. */
    private void throwPoison(ServerLevel level,ServerPlayer target) {
        ThrownPotion potion=new ThrownPotion(level,this);
        potion.setItem(PotionContents.createItemStack(Items.SPLASH_POTION,Potions.POISON));
        potion.setPos(getX(),getY()+0.55,getZ());
        Vec3 aim=target.position().add(0.0,1.0,0.0).subtract(potion.position());
        potion.shoot(aim.x,aim.y+aim.horizontalDistance()*0.12,aim.z,0.85F,3.0F);
        level.addFreshEntity(potion);
        level.playSound(null,blockPosition(),SoundEvents.WITCH_THROW,SoundSource.HOSTILE,1.3F,1.05F);
        level.sendParticles(ParticleTypes.WITCH,getX(),getY()+0.5,getZ(),18,0.35,0.30,0.35,0.05);
    }

    /** Warden-style beam: one targeted hit with vanilla sonic particles and damage type. */
    private void fireSonicBoom(ServerLevel level,ServerPlayer target) {
        Vec3 start=position().add(0.0,0.6,0.0);
        Vec3 end=target.getEyePosition();
        Vec3 delta=end.subtract(start);
        int steps=Math.max(1,(int)Math.ceil(delta.length()/0.75));
        for (int i=0;i<=steps;i++) {
            Vec3 p=start.add(delta.scale((double)i/steps));
            level.sendParticles(ParticleTypes.SONIC_BOOM,p.x,p.y,p.z,1,0,0,0,0);
        }
        level.playSound(null,blockPosition(),SoundEvents.WARDEN_SONIC_BOOM,
                SoundSource.HOSTILE,2.0F,1.02F);
        target.hurt(level.damageSources().sonicBoom(this),10.0F); // 5 hearts base damage
        Vec3 push=delta.normalize().scale(1.2);
        target.push(push.x,0.25,push.z);
        target.hurtMarked=true;
    }

    /** Non-incendiary lightning with exactly five hearts of base magic damage. */
    private void strikeLightning(ServerLevel level, ServerPlayer target) {
        LightningBolt bolt=EntityType.LIGHTNING_BOLT.create(level);
        if (bolt!=null) {
            bolt.moveTo(target.getX(),target.getY(),target.getZ());
            bolt.setVisualOnly(true);
            level.addFreshEntity(bolt);
        }
        target.hurt(level.damageSources().magic(),10.0F);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,target.getX(),target.getY()+1.0,
                target.getZ(),45,0.8,1.1,0.8,0.20);
    }

    /** Every successful hit opens a visible radial wave (not a passive periodic aura). */
    @Override public boolean hurt(DamageSource source,float amount) {
        // The Warden must never damage the soul, including sonic boom attacks.
        // Keep all other damage sources intact so players can still hunt it.
        if (source.getEntity() instanceof Warden
                || source.getDirectEntity() instanceof Warden) return false;
        boolean damaged=super.hurt(source,amount);
        if (damaged && this.level() instanceof ServerLevel level) {
            sayInjuryLine(level);
        }
        if (damaged && this.isAlive() && this.level() instanceof ServerLevel level && waveTicks==0) {
            waveTicks=WAVE_DURATION;
            waveHit.clear();
            level.playSound(null,blockPosition(),SoundEvents.ILLUSIONER_CAST_SPELL,
                    SoundSource.HOSTILE,1.8F,0.58F);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,getX(),getY()+0.45,getZ(),
                    95,0.65,0.65,0.65,0.10);
        }
        return damaged;
    }

    /**
     * Exactly one unique, progressively distressed line per health band.
     * The stage only increases: healing, relogging, and repeated hits cannot
     * replay an earlier line. Large hits skip intermediate lines naturally.
     */
    private void sayInjuryLine(ServerLevel level) {
        float fraction = getMaxHealth() > 0 ? getHealth() / getMaxHealth() : 0.0F;
        int stage = fraction > 0.93F ? 0 :
                fraction > 0.86F ? 1 :
                fraction > 0.79F ? 2 :
                fraction > 0.72F ? 3 :
                fraction > 0.65F ? 4 :
                fraction > 0.58F ? 5 :
                fraction > 0.51F ? 6 :
                fraction > 0.44F ? 7 :
                fraction > 0.37F ? 8 :
                fraction > 0.30F ? 9 :
                fraction > 0.24F ? 10 :
                fraction > 0.18F ? 11 :
                fraction > 0.12F ? 12 : 13;

        // Never repeat, even if the soul regains health or is saved/reloaded.
        if (stage <= highestTauntStage) return;
        highestTauntStage = stage;

        String key = "title.mdkcosmetic.soul_"+stage;
        ChatFormatting color = stage >= 12 ? ChatFormatting.RED :
                stage >= 9 ? ChatFormatting.LIGHT_PURPLE :
                stage >= 5 ? ChatFormatting.YELLOW : ChatFormatting.AQUA;
        CollectorTitles.announce(level, position(), key, color);
    }

    private void tickShockwave(ServerLevel level) {
        double radius=WAVE_RADIUS*(WAVE_DURATION-waveTicks+1)/(double)WAVE_DURATION;
        --waveTicks;
        // Render every second tick to avoid hundreds of network packets per frame.
        if (waveTicks%2==0 || waveTicks==1) {
            int points=40;
            for (int i=0;i<points;i++) {
                double angle=2.0*Math.PI*i/points;
                double x=getX()+Math.cos(angle)*radius;
                double z=getZ()+Math.sin(angle)*radius;
                level.sendParticles(i%4==0?ParticleTypes.END_ROD:ParticleTypes.REVERSE_PORTAL,
                        x,getY()+0.55,z,1,0.0,0.05,0.0,0.012);
            }
        }
        // All living creatures except the source and its Warden ally.
        List<LivingEntity> targets=level.getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(WAVE_RADIUS+1), e->e.isAlive() && e!=this
                        && !(e instanceof Warden) && !e.isSpectator());
        for (LivingEntity victim : targets) {
            double dx=victim.getX()-getX();
            double dz=victim.getZ()-getZ();
            double d=Math.sqrt(dx*dx+dz*dz);
            if (d>radius+0.5 || d>WAVE_RADIUS || !waveHit.add(victim.getUUID())) continue;
            if (d<0.001) {
                double angle=random.nextDouble()*Math.PI*2;
                dx=Math.cos(angle); dz=Math.sin(angle); d=1.0;
            }
            // Strong horizontal impulse; motion stops at walls instead of phasing.
            // The shockwave visibly expands to 20 blocks from the caster.
            double force=3.4+(WAVE_RADIUS-d)*0.035;
            Vec3 velocity=victim.getDeltaMovement();
            victim.setDeltaMovement(dx/d*force,Math.max(0.65,velocity.y),dz/d*force);
            victim.hurtMarked=true;
            victim.hasImpulse=true;
            level.sendParticles(ParticleTypes.CLOUD,victim.getX(),victim.getY()+0.5,
                    victim.getZ(),18,0.4,0.4,0.4,0.1);
        }
        if (waveTicks==0) waveHit.clear();
    }

    private @Nullable ServerPlayer nearestVisiblePlayer(ServerLevel level, int radius) {
        ServerPlayer nearest=null;
        double best=(double)radius*radius;
        for (ServerPlayer p : level.players()) {
            if (p.isSpectator() || p.isCreative() || !p.isAlive()) continue;
            double d=this.distanceToSqr(p);
            if (d < best && this.hasLineOfSight(p)) { best=d; nearest=p; }
        }
        return nearest;
    }

    private void drift(ServerLevel level,@Nullable ServerPlayer threat) {
        if (--flyCooldown<=0 || this.distanceToSqr(flightTarget)<2.0) {
            flyCooldown=18+random.nextInt(48);
            Vec3 next=new Vec3(this.getX()+(random.nextDouble()-0.5)*12,
                    Mth.clamp(this.getY()+(random.nextDouble()-0.5)*5,-58,-12),
                    this.getZ()+(random.nextDouble()-0.5)*12);
            if (level.noCollision(this,this.getBoundingBox().move(next.subtract(position())))) flightTarget=next;
            else flightTarget=position();
        }
        Vec3 delta=flightTarget.subtract(position());
        if (delta.lengthSqr()>0.16) {
            Vec3 steering=delta.normalize().scale(threat==null?0.036:0.060);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.82).add(steering));
        }
    }

    /**
     * Scan block entity indexes in already-loaded chunks (cheap compared to a
     * 3D block scan). Select the nearest naturally generated shrieker IN an
     * Ancient City, then find a free point within five blocks of that shrieker.
     */
    private boolean blinkToNearestShrieker(ServerLevel level,ServerPlayer player) {
        BlockPos origin=blockPosition();
        List<BlockPos> candidates=new ArrayList<>();
        int ccx=origin.getX()>>4, ccz=origin.getZ()>>4;
        for (int cx=ccx-4;cx<=ccx+4;cx++) {
            for (int cz=ccz-4;cz<=ccz+4;cz++) {
                LevelChunk chunk=level.getChunkSource().getChunkNow(cx,cz);
                if (chunk==null) continue; // absolutely no chunk generation/loading
                for (BlockPos pos:chunk.getBlockEntitiesPos()) {
                    if (Math.abs(pos.getY()-origin.getY())>22) continue;
                    if (origin.distSqr(pos)>72.0*72.0) continue;
                    if (!(level.getBlockEntity(pos) instanceof SculkShriekerBlockEntity)) continue;
                    if (!level.getBlockState(pos).hasProperty(SculkShriekerBlock.CAN_SUMMON)
                            || !level.getBlockState(pos).getValue(SculkShriekerBlock.CAN_SUMMON)) continue;
                    if (!level.structureManager().getStructureWithPieceAt(pos,
                            level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                            .getHolderOrThrow(BuiltinStructures.ANCIENT_CITY).value()).isValid()) continue;
                    candidates.add(pos.immutable());
                }
            }
        }
        candidates.sort(Comparator.comparingDouble(origin::distSqr));
        for (BlockPos shrieker:candidates) {
            // Hover over or beside the block, never inside solid blocks.
            for (int i=0;i<28;i++) {
                double angle=(i*2.399963229728653)+random.nextFloat()*0.2;
                double radius=1.65+(i%5)*0.85;
                double x=shrieker.getX()+0.5+Math.cos(angle)*radius;
                double z=shrieker.getZ()+0.5+Math.sin(angle)*radius;
                double y=shrieker.getY()+1.3+(i%4)*0.65;
                if (new Vec3(x,y,z).distanceToSqr(player.position())<16.0*16.0) continue;
                BlockPos dest=BlockPos.containing(x,y,z);
                if (level.getChunkSource().getChunkNow(dest.getX()>>4,dest.getZ()>>4)==null) continue;
                if (!level.noCollision(this,getBoundingBox().move(x-getX(),y-getY(),z-getZ()))) continue;
                level.sendParticles(ParticleTypes.PORTAL,getX(),getY()+0.5,getZ(),
                        35,0.5,0.5,0.5,0.22);
                teleportTo(x,y,z);
                setDeltaMovement(Vec3.ZERO);
                flightTarget=position();
                flyCooldown=30;
                lastShrieker=shrieker;
                level.sendParticles(ParticleTypes.PORTAL,getX(),getY()+0.5,getZ(),
                        35,0.5,0.5,0.5,0.22);
                level.playSound(null,blockPosition(),SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.HOSTILE,0.8F,1.22F);
                return true;
            }
        }
        return false; // no arbitrary teleport if no safe shrieker was found
    }

    private void startShrieker(ServerLevel level, ServerPlayer player) {
        if (level.getDifficulty()==Difficulty.PEACEFUL || lastShrieker==null) return;
        if (level.getBlockEntity(lastShrieker) instanceof SculkShriekerBlockEntity shrieker
                && level.getBlockState(lastShrieker).hasProperty(SculkShriekerBlock.CAN_SUMMON)
                && level.getBlockState(lastShrieker).getValue(SculkShriekerBlock.CAN_SUMMON)) {
            shrieker.tryShriek(level,player);
        }
    }

    /** Decoy audio only during the post-blink five-second window. */
    private void playMimic(ServerLevel level) {
        SoundEvent[] sounds={
                SoundEvents.ZOMBIE_AMBIENT,SoundEvents.ZOMBIE_STEP,
                SoundEvents.SKELETON_STEP,SoundEvents.SPIDER_AMBIENT,
                SoundEvents.PLAYER_BIG_FALL,SoundEvents.PLAYER_HURT,
                SoundEvents.STONE_PLACE,SoundEvents.STONE_BREAK,
                SoundEvents.WOOD_PLACE,SoundEvents.STONE_STEP
        };
        SoundEvent sound=sounds[random.nextInt(sounds.length)];
        level.playSound(null,blockPosition(),sound,SoundSource.BLOCKS,
                1.25F,0.87F+random.nextFloat()*0.28F);
        level.sendParticles(ParticleTypes.SCULK_SOUL,getX(),getY()+0.4,getZ(),
                4,0.35,0.25,0.35,0.015);
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TeleportCooldown",teleportCooldown);
        tag.putInt("LightningCooldown",lightningCooldown);
        tag.putInt("ArrowCooldown",arrowCooldown);
        tag.putInt("PoisonCooldown",poisonCooldown);
        tag.putInt("SonicCooldown",sonicCooldown);
        tag.putBoolean("SummonedGuardian",hasSummonedGuardian);
        tag.putInt("HighestDialogueStage", highestTauntStage);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        teleportCooldown=tag.getInt("TeleportCooldown");
        lightningCooldown=tag.getInt("LightningCooldown");
        arrowCooldown=tag.getInt("ArrowCooldown");
        poisonCooldown=tag.getInt("PoisonCooldown");
        sonicCooldown=tag.getInt("SonicCooldown");
        hasSummonedGuardian=tag.getBoolean("SummonedGuardian");
        if (tag.contains("HighestDialogueStage")) {
            highestTauntStage = Mth.clamp(tag.getInt("HighestDialogueStage"), -1, 13);
        } else if (tag.contains("LastTauntStage")) {
            // Upgrade a soul saved on v1.18.20. Old dialogue had four health bands.
            // Map its highest shown band to the new sequence without replaying
            // already surpassed health bands. The old timer is intentionally ignored.
            highestTauntStage = switch (tag.getInt("LastTauntStage")) {
                case 0 -> 4;
                case 1 -> 8;
                case 2 -> 12;
                case 3 -> 13;
                default -> -1;
            };
        } else {
            highestTauntStage = -1;
        }
        // A spell in progress is intentionally cancelled on reload: never fire without a warning.
        pendingSpell=Spell.NONE;
        spellWindup=0;
        spellTarget=null;
    }
}
