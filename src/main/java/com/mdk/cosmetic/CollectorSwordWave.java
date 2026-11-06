package com.mdk.cosmetic;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import java.util.*;

/** Animated radial wave that starts at the wielder, never pushes the wielder. */
public final class CollectorSwordWave {
    private static final Map<UUID,Wave> WAVES=new HashMap<>();
    private record Wave(Vec3 origin,ServerLevel level,int tick,Set<UUID> hit) {}
    private CollectorSwordWave() {}
    public static void start(ServerPlayer player) {
        WAVES.put(player.getUUID(),new Wave(player.position().add(0,0.6,0),(ServerLevel)player.level(),0,new HashSet<>()));
        player.level().playSound(null,player.blockPosition(),SoundEvents.WARDEN_SONIC_BOOM,
                SoundSource.PLAYERS,0.8F,1.45F);
    }
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if(!(event.player() instanceof ServerPlayer player))return;
        Wave state=WAVES.get(player.getUUID());
        if(state==null)return;
        if(state.level()!=player.level() || !player.isAlive()) {WAVES.remove(player.getUUID());return;}
        int tick=state.tick()+1;
        double radius=tick;
        Vec3 origin=state.origin();
        if(tick%2==0 || tick==1) for(int i=0;i<48;i++) {
            double angle=Math.PI*2*i/48;
            state.level().sendParticles(i%4==0?ParticleTypes.END_ROD:ParticleTypes.REVERSE_PORTAL,
                origin.x+Math.cos(angle)*radius,origin.y,origin.z+Math.sin(angle)*radius,
                1,0,0.05,0,0.01);
        }
        for(LivingEntity target:state.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(origin,origin).inflate(21),e->e.isAlive() && e!=player && !e.isSpectator())) {
            if(target instanceof Warden) continue; // same ally immunity as Collector Soul
            double dx=target.getX()-origin.x,dz=target.getZ()-origin.z;
            double d=Math.sqrt(dx*dx+dz*dz);
            if(d>radius+0.5 || d>20 || !state.hit().add(target.getUUID()))continue;
            if(d<0.01){dx=1;dz=0;d=1;}
            double force=3.4+(20-d)*0.035;
            target.setDeltaMovement(dx/d*force,Math.max(0.65,target.getDeltaMovement().y),dz/d*force);
            target.needsSync=true; target.syncVelocity=true;
            state.level().sendParticles(ParticleTypes.CLOUD,target.getX(),target.getY()+0.5,target.getZ(),
                15,0.4,0.3,0.4,0.08);
        }
        if(tick>=20)WAVES.remove(player.getUUID());
        else WAVES.put(player.getUUID(),new Wave(origin,state.level(),tick,state.hit()));
    }
}
