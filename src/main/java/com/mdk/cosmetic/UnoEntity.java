package com.mdk.cosmetic;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.ThrownSplashPotion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import java.util.Optional;
import java.util.Comparator;
import java.util.UUID;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Uno: an original humanoid duelist with timed sword combos, dash, dodge and finishers.
 * The animations are synced by vanilla entity data and played on the Uno 3D model.
 * There are no companion/summon mechanics.
 */
public final class UnoEntity extends Monster {
    public static final int IDLE = 0;
    public static final int SLASH_LEFT = 1;
    public static final int SLASH_RIGHT = 2;
    public static final int HEAVY_SLASH = 3;
    public static final int UPPERCUT = 4;
    public static final int DODGE = 5;
    public static final int DASH = 6;
    public static final int SWEEP_BURST = 7;
    public static final int FINISHER = 8;

    private static final EntityDataAccessor<Integer> ACTION =
            SynchedEntityData.defineId(UnoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> ACTION_START =
            SynchedEntityData.defineId(UnoEntity.class, EntityDataSerializers.LONG);
    // Synced through vanilla entity data; no commands or client-only skin requests.
    private static final EntityDataAccessor<Boolean> SECOND_PHASE =
            SynchedEntityData.defineId(UnoEntity.class, EntityDataSerializers.BOOLEAN);
    // Phase III must also reach clients so animations play at twice their original speed.
    private static final EntityDataAccessor<Boolean> RAGE_ACTIVE =
            SynchedEntityData.defineId(UnoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<net.minecraft.world.entity.EntityReference<LivingEntity>>> MIMIC_TARGET =
            SynchedEntityData.defineId(UnoEntity.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
    private static final EquipmentSlot[] MIMIC_ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    public static final int EAT_APPLE = 9;
    private static final int[] ACTION_TICKS = {0, 16, 16, 23, 20, 13, 17, 25, 28, 34};
    private static final int POTION_COOLDOWN = 20 * 60;
    private static final int SONIC_COOLDOWN = 20 * 60;
    private static final int APPLE_COOLDOWN = 20 * 60;
    private static final int BLINK_COOLDOWN = 20 * 5;
    private static final int WINDUP_TICKS = 20;

    private int dashCooldown;
    private int burstCooldown;
    private int dodgeCooldown;
    private int combo;
    private int pendingImpactTicks = -1;
    private int pendingTargetId = -1;
    private int pendingAction;
    private int dashTicks;
    private int poisonCooldown = 200; // first poison attack after ten seconds, both phases
    private int sonicCooldown = SONIC_COOLDOWN;
    private int appleCooldown = APPLE_COOLDOWN;
    private int blinkCooldown = BLINK_COOLDOWN;
    private int castTicks;
    private int castSpell; // 0 none, 1 poison, 2 red sonic
    private UUID castVictim;
    private int appleEatingTicks;
    private boolean endEncounter;
    /** Triggered once when the offhand totem saves Uno. Persists across world saves. */
    private boolean totemRage;
    private int totemWaveTicks = -1;
    private final java.util.Set<UUID> totemWaveHits = new java.util.HashSet<>();
    private Vec3 totemWaveOrigin;
    private static final int RAGE_SONIC_COOLDOWN = 20 * 5;
    private static final double TARGET_RADIUS_SQUARED = 50.0D * 50.0D;
    private static final ResourceLocation RAGE_SPEED_ID = ResourceLocation.fromNamespaceAndPath(
            MDKCosmetic.MOD_ID, "uno_phase_three_double_speed");
    private BlockPos endAnchor;

    public UnoEntity(EntityType<? extends UnoEntity> type, Level level) {
        super(type, level);
        this.xpReward = 75;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.38D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.45D)
                .add(Attributes.FOLLOW_RANGE, 50.0D);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTION, IDLE);
        builder.define(ACTION_START, 0L);
        builder.define(SECOND_PHASE, false);
        builder.define(RAGE_ACTIVE, false);
        builder.define(MIMIC_TARGET, Optional.empty());
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.28D, true) {
            // Vanilla attacks every 16 ticks normally, every 8 ticks after the totem.
            @Override protected int getAttackInterval() { return UnoEntity.this.totemRage ? 8 : 16; }
        });
        goalSelector.addGoal(3, new RandomStrollGoal(this, 0.9D));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        // No vanilla HurtByTargetGoal / NearestAttackableTargetGoal here:
        // they would overwrite the weak-player priority during multi-player combat.
        // The authoritative target is selected every server tick below.
    }

    public int getAction() { return entityData.get(ACTION); }
    public boolean isSecondPhase() { return entityData.get(SECOND_PHASE); }
    public boolean isTotemRage() { return entityData.get(RAGE_ACTIVE); }
    public Optional<UUID> getMimicTarget() { return entityData.get(MIMIC_TARGET).map(net.minecraft.world.entity.EntityReference::getUUID); }
    public float actionSeconds() {
        float elapsed = Math.max(0F, (level().getGameTime() - entityData.get(ACTION_START)) / 20.0F);
        // Sword/dash/dodge animations on both the client and server are twice as fast in phase III.
        return isTotemRage() && getAction() != EAT_APPLE ? elapsed * 2.0F : elapsed;
    }

    /** Chooses a live survival/adventure player in Uno's own dimension, at most 50 blocks away.
     * All players below two hearts (< 4 HP) outrank healthy players; within each group,
     * the closest player wins. Called before AI ticks so spells and melee share the same target.
     */
    private void updatePreferredPlayerTarget() {
        if (!(level() instanceof ServerLevel server)) return;
        Player chosen = UnoTargetingRules.select(
                server.players(),
                player -> distanceToSqr(player),
                Player::getHealth,
                player -> player.isAlive() && !player.isCreative() && !player.isSpectator(),
                TARGET_RADIUS_SQUARED);
        if (getTarget() != chosen) {
            setTarget(chosen);
            if (chosen == null) getNavigation().stop();
        }
    }

    /** A temporary +100% modifier is reapplied on world load, without duplicating it. */
    private void ensureRageMovementSpeed() {
        if (!totemRage) return;
        AttributeInstance movement = getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement != null && !movement.hasModifier(RAGE_SPEED_ID)) {
            movement.addTransientModifier(new AttributeModifier(
                    RAGE_SPEED_ID, 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    /** Dragon-spawned Uno never naturally despawns and remembers his encounter. */
    public void markDragonEncounter(ServerPlayer player) {
        endEncounter = true;
        endAnchor = blockPosition();
        restrictTo(endAnchor, 120);
        setPersistenceRequired();
        setTarget(player);
        getNavigation().moveTo(player, 1.35D);
    }

    @Override public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    private void playAction(int action) {
        entityData.set(ACTION, action);
        entityData.set(ACTION_START, level().getGameTime());
    }

    @Override public void tick() {
        // Override target selection *before* vanilla AI / melee goal ticks.
        if (!level().isClientSide()) updatePreferredPlayerTarget();
        super.tick();
        if (level().isClientSide()) return;
        ensureRageMovementSpeed();
        // Enforce the Collector Sword even on mobs created before this update.
        if (!getMainHandItem().is(MDKCosmetic.COLLECTOR_SWORD.get())) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(MDKCosmetic.COLLECTOR_SWORD.get()));
            setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        }
        if (!totemRage && !getOffhandItem().is(Items.TOTEM_OF_UNDYING) && appleEatingTicks == 0) {
            setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
            setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        }
        if (totemWaveTicks >= 0) tickTotemWave();
        // Never allow the dragon encounter to vanish into the End void or leave its island.
        if (endEncounter && level().dimension() == Level.END && endAnchor != null) {
            double dx=getX()-(endAnchor.getX()+0.5D), dz=getZ()-(endAnchor.getZ()+0.5D);
            if (getY()<40D || dx*dx+dz*dz>150D*150D) {
                teleportTo(endAnchor.getX()+0.5D,endAnchor.getY(),endAnchor.getZ()+0.5D);
                setDeltaMovement(Vec3.ZERO);
                fallDistance=0.0F;
                getNavigation().stop();
                dashTicks=0;
            }
        }

        // Phase II triggers at 200 HP and retains vanilla player-skin/armor copying.
        if (!isSecondPhase() && getHealth() <= getMaxHealth() * 0.5F) activateSecondPhase();
        if (dashCooldown > 0) dashCooldown--;
        if (burstCooldown > 0) burstCooldown--;
        if (dodgeCooldown > 0) dodgeCooldown--;
        if (poisonCooldown > 0) poisonCooldown--;
        if (sonicCooldown > 0) sonicCooldown--;
        if (appleCooldown > 0) appleCooldown--;
        if (blinkCooldown > 0) blinkCooldown--;

        // Black smoke and ash (no imported textures or extra 3D body parts).
        if (level() instanceof ServerLevel server && tickCount % 5 == 0) {
            server.sendParticles(ParticleTypes.ASH, getX(), getY()+1.05D, getZ(),
                    4, 0.37D, 0.8D, 0.37D, 0.004D);
            server.sendParticles(ParticleTypes.SMOKE, getX(), getY()+0.85D, getZ(),
                    3, 0.31D, 0.58D, 0.31D, 0.005D);
            if (isSecondPhase() && tickCount % 15 == 0) server.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    getX(), getY()+0.8D, getZ(), 2, 0.23D, 0.48D, 0.23D, 0.012D);
        }

        int action = getAction();
        if (action != IDLE && action < ACTION_TICKS.length
                && actionSeconds()*20F >= ACTION_TICKS[action]) playAction(IDLE);

        if (pendingImpactTicks >= 0 && --pendingImpactTicks <= 0) {
            resolveSwordImpact();
            pendingImpactTicks = -1;
        }

        if (castTicks > 0 && --castTicks == 0) finishSpell();
        if (appleEatingTicks > 0 && --appleEatingTicks == 0) finishGoldenApple();
        if (castTicks > 0 || appleEatingTicks > 0) return;

        LivingEntity target = getTarget();
        // A player who just died or left range is replaced on the next server tick.
        if (!(target instanceof Player player) || !player.isAlive()
                || player.isCreative() || player.isSpectator()
                || distanceToSqr(player) > TARGET_RADIUS_SQUARED) return;

        double distance = distanceToSqr(target);
        if (dashTicks > 0) {
            continueDash(target);
            if (pendingImpactTicks >= 0) return;
        }
        if (pendingImpactTicks >= 0) return;

        // After the totem is spent, the crimson Warden beam is the highest-priority spell.
        // Its cooldown is five seconds, regardless of the old sixty-second cooldown.
        if (totemRage && isSecondPhase() && sonicCooldown == 0 && distance < 35D * 35D) {
            beginSpell(2, target);
            sonicCooldown = RAGE_SONIC_COOLDOWN;
            return;
        }

        if (isSecondPhase() && blinkCooldown == 0 && distance > 5D && distance < 2500D) {
            if (blinkNextTo(target)) {
                blinkCooldown = BLINK_COOLDOWN;
                return;
            }
            blinkCooldown = 15; // retry without spamming an invalid destination
        }

        if (poisonCooldown == 0 && distance < 35D*35D && hasLineOfSight(target)) {
            beginSpell(1, target);
            poisonCooldown = POTION_COOLDOWN;
            return;
        }
        if (isSecondPhase() && sonicCooldown == 0 && distance < 35D*35D) {
            beginSpell(2, target);
            sonicCooldown = totemRage ? RAGE_SONIC_COOLDOWN : SONIC_COOLDOWN;
            return;
        }
        if (isSecondPhase() && appleCooldown == 0 && getHealth() < getMaxHealth()-10.0F) {
            beginGoldenApple();
            appleCooldown = APPLE_COOLDOWN;
            return;
        }

        // Sustained dash, up to 20+ blocks per use; phase II also has a short blink.
        if (dashCooldown == 0 && distance > 16.0D && distance < 2500.0D
                && onGround() && hasLineOfSight(target)) {
            performDash(target);
        } else if (burstCooldown == 0 && distance < 18.0D && distance > 2.0D && hasLineOfSight(target)) {
            performSweepBurst();
        }
    }

    private void beginSpell(int spell, LivingEntity target) {
        castSpell = spell;
        castVictim = target.getUUID();
        castTicks = WINDUP_TICKS;
        playSound(spell == 1 ? SoundEvents.WITCH_CELEBRATE : SoundEvents.WARDEN_SONIC_CHARGE,
                1.35F, spell == 1 ? 0.75F : 0.67F);
        if (level() instanceof ServerLevel server) server.sendParticles(
                spell == 1 ? ParticleTypes.WITCH : ParticleTypes.ASH,
                getX(), getY()+1.3D, getZ(), 18, 0.4D, 0.5D, 0.4D, 0.01D);
    }

    private void finishSpell() {
        int spell = castSpell;
        UUID id = castVictim;
        castSpell = 0;
        castVictim = null;
        if (!(level() instanceof ServerLevel server) || id == null || !isAlive()) return;
        Entity hit = server.getEntity(id);
        if (!(hit instanceof LivingEntity victim) || !victim.isAlive()
                || distanceToSqr(victim)>35D*35D) return;
        if (victim instanceof Player p && (p.isCreative() || p.isSpectator())) return;
        if (spell == 1 && hasLineOfSight(victim)) throwPoison(server,victim);
        if (spell == 2 && isSecondPhase()) fireCrimsonSonic(server,victim);
    }

    /** A real throwable poison potion; poison itself never bypasses vanilla resistances. */
    private void throwPoison(ServerLevel server, LivingEntity target) {
        ThrownSplashPotion potion = new ThrownSplashPotion(server,this, PotionContents.createItemStack(Items.SPLASH_POTION,Potions.POISON));
        potion.setItem(PotionContents.createItemStack(Items.SPLASH_POTION,Potions.POISON));
        potion.setPos(getX(),getY()+1.6D,getZ());
        Vec3 aim = target.getEyePosition().subtract(potion.position());
        potion.shoot(aim.x, aim.y+aim.horizontalDistance()*0.10D, aim.z,1.05F,2.5F);
        server.addFreshEntity(potion);
        server.playSound(null,blockPosition(),SoundEvents.WITCH_THROW,SoundSource.HOSTILE,1.25F,0.8F);
    }

    /** Warden sonic mechanics; red-black particles replace the blue visual. */
    private void fireCrimsonSonic(ServerLevel server,LivingEntity victim) {
        Vec3 start = position().add(0,1.45D,0);
        Vec3 end = victim.getEyePosition();
        Vec3 delta = end.subtract(start);
        int count = Math.max(2,(int)Math.ceil(delta.length()/0.33D));
        DustParticleOptions crimson = new DustParticleOptions(net.minecraft.util.ARGB.colorFromFloat(1F,0.84F,0.035F,0.06F),1.55F);
        DustParticleOptions black = new DustParticleOptions(net.minecraft.util.ARGB.colorFromFloat(1F,0.045F,0.012F,0.020F),1.2F);
        for (int i=0; i<=count; i++) {
            Vec3 p=start.add(delta.scale((double)i/count));
            server.sendParticles(i%3==0 ? black : crimson,p.x,p.y,p.z,2,0.055D,0.055D,0.055D,0.005D);
            if (i%5==0) server.sendParticles(ParticleTypes.SMOKE,p.x,p.y,p.z,1,0.04D,0.04D,0.04D,0.002D);
        }
        server.playSound(null,blockPosition(),SoundEvents.WARDEN_SONIC_BOOM,SoundSource.HOSTILE,2.0F,0.8F);
        victim.hurt(server.damageSources().sonicBoom(this),10.0F);
        if (delta.lengthSqr()>0.001D) {
            Vec3 push=delta.normalize().scale(1.0D);
            victim.push(push.x,0.28D,push.z);
            victim.hurtMarked=true;
        }
    }

    private void beginGoldenApple() {
        appleEatingTicks = 30;
        playAction(EAT_APPLE);
        // Preserve the Totem of Undying in the offhand while eating.
        // The apple is an ability animation, not an equipment replacement.
        playSound(SoundEvents.GENERIC_EAT.value(),0.8F,0.8F);
    }

    /** Exactly the two ordinary golden-apple effects, no enchanted-apple buffs. */
    private void finishGoldenApple() {
        if (!isSecondPhase() || !isAlive()) return;
        addEffect(new MobEffectInstance(MobEffects.REGENERATION,20*5,1));
        addEffect(new MobEffectInstance(MobEffects.ABSORPTION,20*120,0));
        playSound(SoundEvents.PLAYER_BURP,0.75F,0.85F);
        if (level() instanceof ServerLevel server) server.sendParticles(ParticleTypes.HEART,
                getX(),getY()+1.3D,getZ(),10,0.45D,0.8D,0.45D,0.12D);
    }

    /** Enderman audio and a collision-checked arrival next to the target. */
    private boolean blinkNextTo(LivingEntity target) {
        if (!(level() instanceof ServerLevel server)) return false;
        for (int i=0;i<24;i++) {
            double a=2*Math.PI*random.nextDouble();
            double r=1.5D+1.1D*random.nextDouble();
            double x=target.getX()+Math.cos(a)*r;
            double z=target.getZ()+Math.sin(a)*r;
            for (int dy=1;dy>=-2;dy--) {
                double y=Math.floor(target.getY())+dy;
                BlockPos feet=BlockPos.containing(x,y,z);
                if (!server.hasChunkAt(feet) || server.getBlockState(feet.below()).isAir()
                        || !server.getBlockState(feet.below()).blocksMotion()) continue;
                AABB box=getBoundingBox().move(x-getX(), y-getY(),z-getZ());
                if (!server.noCollision(this,box)) continue;
                Vec3 old=position();
                server.playSound(null,blockPosition(),SoundEvents.ENDERMAN_TELEPORT,SoundSource.HOSTILE,1.4F,0.85F);
                server.sendParticles(ParticleTypes.PORTAL,old.x,old.y+1D,old.z,30,0.3D,0.8D,0.3D,0.13D);
                teleportTo(x,y,z);
                server.playSound(null,blockPosition(),SoundEvents.ENDERMAN_TELEPORT,SoundSource.HOSTILE,1.4F,1.05F);
                server.sendParticles(ParticleTypes.REVERSE_PORTAL,x,y+1D,z,35,0.35D,0.8D,0.35D,0.1D);
                getNavigation().stop();
                return true;
            }
        }
        return false;
    }

    /** A short, sustained burst, not a single velocity impulse. */
    private void continueDash(LivingEntity target) {
        dashTicks--;
        Vec3 direction=target.position().subtract(position());
        direction=new Vec3(direction.x,0D,direction.z);
        if (direction.lengthSqr()<7D) {dashTicks=0;return;}
        direction=direction.normalize().scale(1.38D);
        setDeltaMovement(direction.x,Math.max(0.0D,getDeltaMovement().y),direction.z);
        hurtMarked=true;
    }

    /** Copies the appearance and armor of the nearest player, never their inventory items. */
    private void activateSecondPhase() {
        if (!(level() instanceof ServerLevel server)) return;
        ServerPlayer nearest = server.getEntitiesOfClass(ServerPlayer.class,
                getBoundingBox().inflate(48.0D),
                player -> player.isAlive() && !player.isSpectator() && !player.isCreative())
                .stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if (nearest == null) return; // A player must be close; retry automatically next tick.

        // No client-side game-profile fetch and no user-facing skin command.
        entityData.set(MIMIC_TARGET, Optional.of(new net.minecraft.world.entity.EntityReference<LivingEntity>(nearest)));
        entityData.set(SECOND_PHASE, true);
        // First showcase casts shortly after transformation; subsequent uses obey full cooldowns.
        blinkCooldown = 0;
        sonicCooldown = 80;
        appleCooldown = 120;

        for (EquipmentSlot slot : MIMIC_ARMOR_SLOTS) {
            ItemStack equipped = nearest.getItemBySlot(slot);
            // A true copy preserves trims, enchantments, color, condition, and modded armor data.
            setItemSlot(slot, equipped.isEmpty() ? ItemStack.EMPTY : equipped.copy());
            setDropChance(slot, 0.0F); // No free copied armor drops / duplication.
        }
        setCanPickUpLoot(false);
        playSound(SoundEvents.ILLUSIONER_CAST_SPELL, 1.6F, 0.8F);
        server.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 1.1D, getZ(),
                90, 0.65D, 0.95D, 0.65D, 0.08D);
        server.sendParticles(ParticleTypes.WITCH, getX(), getY() + 1.0D, getZ(),
                35, 0.6D, 0.8D, 0.6D, 0.06D);
    }

    /** Melee goal schedules damage at the blade's actual animation impact frame. */
    @Override public boolean doHurtTarget(ServerLevel damageLevel,Entity victim) {
        if (!(victim instanceof LivingEntity living) || pendingImpactTicks >= 0) return false;
        if (getAction() != IDLE && getAction() != DASH) return false;
        combo++;
        int selected = switch (combo % 5) {
            case 1 -> SLASH_LEFT;
            case 2 -> SLASH_RIGHT;
            case 3 -> UPPERCUT;
            case 4 -> HEAVY_SLASH;
            default -> FINISHER;
        };
        pendingAction = selected;
        pendingTargetId = living.getId();
        int ordinaryImpact = selected == HEAVY_SLASH ? 8 : selected == FINISHER ? 11 : 5;
        // Otherwise the attack interval would double but the damage frame would not.
        pendingImpactTicks = totemRage ? Math.max(1, (ordinaryImpact + 1) / 2) : ordinaryImpact;
        playAction(selected);
        playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.85F, selected == FINISHER ? 0.66F : 1.04F);
        return true;
    }

    private void resolveSwordImpact() {
        if (!(level() instanceof ServerLevel server)) return;
        Entity victim = server.getEntity(pendingTargetId);
        pendingTargetId = -1;
        if (!(victim instanceof LivingEntity living) || !living.isAlive()
                || distanceToSqr(living) > 18D || !hasLineOfSight(living)) return;
        super.doHurtTarget((ServerLevel)level(),living);
        if (pendingAction == UPPERCUT) {
            living.push(0D, 0.83D, 0D);
            living.hurtMarked = true;
        } else if (pendingAction == HEAVY_SLASH || pendingAction == FINISHER) {
            Vec3 away = living.position().subtract(position());
            if (away.lengthSqr() > 0.001D) {
                Vec3 direction = away.normalize();
                living.push(direction.x * 0.9D, 0.36D, direction.z * 0.9D);
                living.hurtMarked = true;
            }
            if (pendingAction == FINISHER) living.hurt(damageSources().mobAttack(this), 5F);
        }
        server.sendParticles(ParticleTypes.SWEEP_ATTACK, living.getX(), living.getY() + 1.2,
                living.getZ(), 4, 0.3D, 0.2D, 0.3D, 0.0D);
        server.sendParticles(ParticleTypes.CRIT, living.getX(), living.getY() + 1.1,
                living.getZ(), 9, 0.3D, 0.2D, 0.3D, 0.07D);
    }

    private void performDash(LivingEntity target) {
        Vec3 toward = target.position().subtract(position());
        toward = new Vec3(toward.x, 0D, toward.z);
        if (toward.lengthSqr() < 0.001D) return;
        dashTicks = 17;
        dashCooldown = 65;
        playAction(DASH);
        continueDash(target);
        playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.85F);
        if (level() instanceof ServerLevel server) server.sendParticles(ParticleTypes.SMOKE,
                getX(), getY() + 0.65D, getZ(), 23, 0.4D, 0.65D, 0.4D, 0.05D);
    }

    private void performSweepBurst() {
        burstCooldown = 150;
        playAction(SWEEP_BURST);
        playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1F, 0.76F);
        if (!(level() instanceof ServerLevel server)) return;
        server.sendParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY() + 1.0D, getZ(),
                7, 1.2D, 0.4D, 1.2D, 0D);
        server.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.0D, getZ(),
                18, 1.2D, 0.5D, 1.2D, 0.07D);
        for (LivingEntity other : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(2.65D))) {
            if (other == this || other instanceof UnoEntity || isAlliedTo(other)) continue;
            if (!hasLineOfSight(other)) continue;
            other.hurt(damageSources().mobAttack(this), 4.0F);
            Vec3 v = other.position().subtract(position());
            if (v.lengthSqr() > 0.001D) {
                Vec3 away = v.normalize().scale(0.9D);
                other.push(away.x, 0.35D, away.z);
                other.hurtMarked = true;
            }
        }
    }

    @Override public boolean hurtServer(ServerLevel damageLevel,DamageSource source, float amount) {
        if (!level().isClientSide() && dodgeCooldown == 0 && !isDeadOrDying()
                && source.getEntity() instanceof LivingEntity attacker && random.nextFloat() < 0.17F) {
            dodgeCooldown = 95;
            playAction(DODGE);
            Vec3 away = position().subtract(attacker.position());
            if (away.lengthSqr() > 0.001D) {
                away = away.normalize().scale(0.7D);
                setDeltaMovement(away.x, 0.23D, away.z);
                hurtMarked = true;
            }
            playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 0.6F, 1.25F);
            if (level() instanceof ServerLevel server)
                server.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.8D, getZ(),
                        12, 0.4D, 0.6D, 0.4D, 0.04D);
            return false;
        }
        boolean hadTotem = !totemRage && getOffhandItem().is(Items.TOTEM_OF_UNDYING);
        boolean damaged = super.hurtServer(damageLevel,source, amount);
        // Vanilla LivingEntity consumes totems for mobs as well as players.
        // Observe that change instead of duplicating death-protection mechanics.
        if (hadTotem && !getOffhandItem().is(Items.TOTEM_OF_UNDYING) && isAlive()) {
            awakenTotemRage();
        }
        return damaged;
    }

    /** One-shot boss transformation after vanilla totem resurrection. */
    private void awakenTotemRage() {
        totemRage = true;
        entityData.set(RAGE_ACTIVE, true);
        ensureRageMovementSpeed(); // 0.38 -> 0.76 base movement speed, exactly double.
        totemWaveOrigin = position().add(0.0D, 0.6D, 0.0D);
        totemWaveTicks = 0;
        totemWaveHits.clear();
        sonicCooldown = 0;
        // A fatal hit leaves Uno at 1 HP. Ensure phase II is active for raycasting.
        if (!isSecondPhase()) activateSecondPhase();
        if (level() instanceof ServerLevel server) {
            server.playSound(null, blockPosition(), SoundEvents.WARDEN_SONIC_BOOM,
                    SoundSource.HOSTILE, 1.8F, 0.8F);
            server.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 1.3D, getZ(),
                    70, 0.7D, 0.9D, 0.7D, 0.1D);
        }
    }

    /** Same 20-block expanding knockback ring as CollectorSwordWave, excluding Uno. */
    private void tickTotemWave() {
        if (!(level() instanceof ServerLevel server) || totemWaveOrigin == null) {
            totemWaveTicks = -1;
            return;
        }
        double radius = ++totemWaveTicks;
        if (totemWaveTicks % 2 == 0 || totemWaveTicks == 1) {
            for (int i = 0; i < 48; i++) {
                double a = 2.0D * Math.PI * i / 48.0D;
                server.sendParticles(i % 4 == 0 ? ParticleTypes.END_ROD : ParticleTypes.REVERSE_PORTAL,
                        totemWaveOrigin.x + Math.cos(a) * radius, totemWaveOrigin.y,
                        totemWaveOrigin.z + Math.sin(a) * radius,
                        1, 0.0D, 0.05D, 0.0D, 0.01D);
            }
        }
        for (LivingEntity other : server.getEntitiesOfClass(LivingEntity.class,
                new AABB(totemWaveOrigin, totemWaveOrigin).inflate(21.0D),
                entity -> entity.isAlive() && entity != this && !entity.isSpectator())) {
            if (other instanceof UnoEntity || other instanceof net.minecraft.world.entity.monster.warden.Warden
                    || isAlliedTo(other) || totemWaveHits.contains(other.getUUID())) continue;
            double dx = other.getX() - totemWaveOrigin.x;
            double dz = other.getZ() - totemWaveOrigin.z;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > radius + 0.5D || d > 20.0D) continue;
            totemWaveHits.add(other.getUUID());
            if (d < 0.01D) { dx = 1.0D; dz = 0.0D; d = 1.0D; }
            double force = 3.4D + (20.0D - d) * 0.035D;
            other.setDeltaMovement(dx / d * force, Math.max(0.65D, other.getDeltaMovement().y),
                    dz / d * force);
            other.hasImpulse = true;
            other.hurtMarked = true;
            server.sendParticles(ParticleTypes.CLOUD, other.getX(), other.getY() + 0.5D,
                    other.getZ(), 15, 0.4D, 0.3D, 0.4D, 0.08D);
        }
        if (totemWaveTicks >= 20) {
            totemWaveTicks = -1;
            totemWaveOrigin = null;
            totemWaveHits.clear();
        }
    }

    @Override protected void populateDefaultEquipmentSlots(net.minecraft.util.RandomSource random, DifficultyInstance difficulty) {
        // Real Collector Sword shown in the player's hand and used by the melee AI.
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(MDKCosmetic.COLLECTOR_SWORD.get()));
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
        setDropChance(EquipmentSlot.OFFHAND, 0.0F);
    }

    @Override public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level,
            DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        return result;
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("UnoDashCooldown", dashCooldown);
        tag.putInt("UnoBurstCooldown", burstCooldown);
        tag.putInt("UnoDodgeCooldown", dodgeCooldown);
        tag.putInt("UnoCombo", combo);
        tag.putInt("UnoDashTicks", dashTicks);
        tag.putInt("UnoPoisonCooldown", poisonCooldown);
        tag.putInt("UnoSonicCooldown", sonicCooldown);
        tag.putInt("UnoAppleCooldown", appleCooldown);
        tag.putInt("UnoBlinkCooldown", blinkCooldown);
        tag.putBoolean("UnoEndEncounter", endEncounter);
        tag.putBoolean("UnoTotemRage", totemRage);
        if(endAnchor!=null) {
            tag.putInt("UnoAnchorX",endAnchor.getX());
            tag.putInt("UnoAnchorY",endAnchor.getY());
            tag.putInt("UnoAnchorZ",endAnchor.getZ());
        }
        tag.putBoolean("UnoPhaseTwo", isSecondPhase());
        getMimicTarget().ifPresent(id -> tag.store("UnoMimicTarget",net.minecraft.core.UUIDUtil.CODEC,id));
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        dashCooldown = tag.getIntOr("UnoDashCooldown",0);
        burstCooldown = tag.getIntOr("UnoBurstCooldown",0);
        dodgeCooldown = tag.getIntOr("UnoDodgeCooldown",0);
        combo = tag.getIntOr("UnoCombo",0);
        dashTicks=tag.getIntOr("UnoDashTicks",0);
        poisonCooldown=tag.contains("UnoPoisonCooldown") ? tag.getIntOr("UnoPoisonCooldown",0) : 200;
        sonicCooldown=tag.contains("UnoSonicCooldown") ? tag.getIntOr("UnoSonicCooldown",0) : SONIC_COOLDOWN;
        appleCooldown=tag.contains("UnoAppleCooldown") ? tag.getIntOr("UnoAppleCooldown",0) : APPLE_COOLDOWN;
        blinkCooldown=tag.contains("UnoBlinkCooldown") ? tag.getIntOr("UnoBlinkCooldown",0) : BLINK_COOLDOWN;
        endEncounter=tag.getBooleanOr("UnoEndEncounter",false);
        totemRage=tag.getBooleanOr("UnoTotemRage",false);
        entityData.set(RAGE_ACTIVE, totemRage);
        ensureRageMovementSpeed();
        if (totemRage) sonicCooldown = Math.min(sonicCooldown, RAGE_SONIC_COOLDOWN);
        if(tag.contains("UnoAnchorY")) endAnchor = new BlockPos(
                tag.getIntOr("UnoAnchorX",0), tag.getIntOr("UnoAnchorY",0), tag.getIntOr("UnoAnchorZ",0));
        if(endEncounter) {
            setPersistenceRequired();
            if(endAnchor!=null) restrictTo(endAnchor,120);
        }
        if (totemRage) {
            setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        } else if (!getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
        }
        setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        if (!getMainHandItem().is(MDKCosmetic.COLLECTOR_SWORD.get())) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(MDKCosmetic.COLLECTOR_SWORD.get()));
        }
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        castTicks=0; castSpell=0; castVictim=null; appleEatingTicks=0;
        entityData.set(SECOND_PHASE, tag.getBooleanOr("UnoPhaseTwo",false));
        entityData.set(MIMIC_TARGET,
                tag.read("UnoMimicTarget",net.minecraft.core.UUIDUtil.CODEC).isPresent() ? Optional.of(new net.minecraft.world.entity.EntityReference<LivingEntity>(tag.read("UnoMimicTarget",net.minecraft.core.UUIDUtil.CODEC).orElseThrow())) : Optional.empty());
        if (isSecondPhase()) {
            // The copied equipment is restored by vanilla Mob NBT. Never allow it to drop.
            for (EquipmentSlot slot : MIMIC_ARMOR_SLOTS) setDropChance(slot, 0.0F);
            setCanPickUpLoot(false);
        }
        pendingImpactTicks = -1;
        pendingTargetId = -1;
        playAction(IDLE);
    }
}
