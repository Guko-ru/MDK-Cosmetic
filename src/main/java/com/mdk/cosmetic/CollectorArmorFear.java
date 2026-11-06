package com.mdk.cosmetic;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import com.mdk.cosmetic.platform.ForgeEntityTicks.EntityTickEvent;

/** Full Collector armor creates a frightening aura. No invulnerability or
 * forced pacification of bosses. Works on the server (multiplayer-safe). */
public final class CollectorArmorFear {
    private CollectorArmorFear() {}
    public static boolean fullSet(Player player) {
        return player.isAlive() && !player.isSpectator()
                && player.getItemBySlot(EquipmentSlot.HEAD).is(MDKCosmetic.COLLECTOR_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(MDKCosmetic.COLLECTOR_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(MDKCosmetic.COLLECTOR_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(MDKCosmetic.COLLECTOR_BOOTS.get());
    }
    private static boolean exempt(Mob mob) {
        return mob instanceof WitherBoss || mob instanceof Warden
                || mob instanceof ElderGuardian || mob instanceof Evoker
                || mob instanceof CollectorSoulEntity || mob instanceof CollectorHeartEntity
                || mob instanceof UnoEntity;
    }
    private static final class CollectorAvoidGoal extends AvoidEntityGoal<Player> {
        CollectorAvoidGoal(PathfinderMob mob) {
            super(mob,Player.class,14.0F,1.25D,1.65D,
                    entity -> entity instanceof Player player && fullSet(player) && CollectorEquipmentData.fearMode(player) == 1);
        }
        @Override public boolean canContinueToUse() {
            return toAvoid!=null && fullSet(toAvoid) && CollectorEquipmentData.fearMode(toAvoid) == 1 && super.canContinueToUse();
        }
    }
    public static void onMobJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof PathfinderMob mob) || exempt(mob)) return;
        // The project already includes an Access Transformer exposing goalSelector.
        // Test the selector rather than persistent NBT: reloaded entities must
        // always receive the goal again but never acquire duplicate goals.
        // Animals normally have no attack goal. Supply one only for provocation mode.
        if (!(mob instanceof Enemy) && !(mob instanceof net.minecraft.world.entity.TamableAnimal)
                && !(mob instanceof net.minecraft.world.entity.npc.AbstractVillager)
                && mob.goalSelector.getAvailableGoals().stream().noneMatch(g -> g.getGoal() instanceof CollectorProvokedGoal)) {
            mob.goalSelector.addGoal(3, new CollectorProvokedGoal(mob));
        }
        if (!(mob instanceof Enemy)) return;
        if (mob.goalSelector.getAvailableGoals().stream()
                .anyMatch(goal -> goal.getGoal() instanceof CollectorAvoidGoal)) return;
        mob.goalSelector.addGoal(1,new CollectorAvoidGoal(mob));
    }
    /** Safe fallback melee AI for peaceful animals, which have no native attack goal. */
    private static final class CollectorProvokedGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final PathfinderMob provoked;
        private int cooldown;
        CollectorProvokedGoal(PathfinderMob mob){
            provoked=mob;
            setFlags(java.util.EnumSet.of(Flag.MOVE,Flag.LOOK));
        }
        @Override public boolean canUse(){
            return provoked.getTarget() instanceof Player player && fullSet(player)
                    && CollectorEquipmentData.fearMode(player)==2;
        }
        @Override public boolean canContinueToUse(){return canUse();}
        @Override public void tick(){
            if(!(provoked.getTarget() instanceof Player target))return;
            provoked.getLookControl().setLookAt(target,30,30);
            provoked.getNavigation().moveTo(target,1.2D);
            if(cooldown>0)cooldown--;
            if(cooldown==0 && provoked.distanceToSqr(target)<4.0D){
                target.hurt(provoked.damageSources().mobAttack(provoked),2.0F);
                cooldown=25;
            }
        }
        @Override public void stop(){
            if(provoked.getTarget() instanceof Player target
                    && (!fullSet(target) || CollectorEquipmentData.fearMode(target)!=2))
                provoked.setTarget(null);
        }
    }
    public static void onTargetChange(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob instanceof Enemy) || exempt(mob)) return;
        if (event.getNewTarget() instanceof Player player && fullSet(player) && CollectorEquipmentData.fearMode(player) == 1)
            event.setCanceled(true);
    }
    public static void onMobTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || exempt(mob)
                || !(mob.level() instanceof ServerLevel level)) return;
        if (mob.tickCount % 5==0 && mob instanceof Enemy
                && mob.getTarget() instanceof Player player && fullSet(player)
                && CollectorEquipmentData.fearMode(player)==1) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
        if (mob.tickCount % 20 != 0) return;
        if (mob instanceof net.minecraft.world.entity.TamableAnimal
                || mob instanceof net.minecraft.world.entity.npc.AbstractVillager) return;
        for (Player player : level.players()) {
            if (fullSet(player) && CollectorEquipmentData.fearMode(player)==2
                    && player.distanceToSqr(mob)<=256 && mob.canAttack(player)) {
                mob.setTarget(player);
                break;
            }
        }
    }
}
