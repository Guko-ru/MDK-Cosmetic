package com.mdk.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.Comparator;

/** Exactly one Uno per killed Ender Dragon, starting at zero HP, not after the 200-tick death animation. */
public final class UnoDragonEncounter {
    private static final String TRIGGERED = "mdkcosmetic_uno_dragon_triggered";
    private UnoDragonEncounter() { }

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)
                || !(dragon.level() instanceof ServerLevel level)
                || level.dimension()!=Level.END || dragon.getHealth()>0.0F
                || com.mdk.cosmetic.platform.PersistentData.of(dragon).getBooleanOr(TRIGGERED,false)) return;

        // The killer is preferred, otherwise the nearest eligible End player.
        ServerPlayer target=level.players().stream()
                .filter(p -> p.isAlive() && !p.isSpectator() && !p.isCreative())
                .min(Comparator.comparingDouble(p -> p.distanceToSqr(dragon)))
                .orElse(null);
        if (target == null) return;

        UnoEntity uno=MDKCosmetic.UNO_ENTITY.get().create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        if (uno==null) return;
        boolean placed=false;
        for(int radius=4;radius<=20 && !placed;radius+=2) {
            for(int i=0;i<14;i++) {
                double a=level.random.nextDouble()*Math.PI*2D;
                double x=target.getX()+Math.cos(a)*radius;
                double z=target.getZ()+Math.sin(a)*radius;
                for(int offset=2;offset>=-5;offset--) {
                    double y=Math.floor(target.getY())+offset;
                    BlockPos feet=BlockPos.containing(x,y,z);
                    if(!level.hasChunkAt(feet) || !level.getBlockState(feet.below()).blocksMotion()) continue;
                    uno.snapTo(x,y,z,level.random.nextFloat()*360F,0F);
                    if(level.noCollision(uno,uno.getBoundingBox())
                            && uno.getBoundingBox().maxY<=level.getMaxY()) {
                        placed=true;
                        break;
                    }
                }
                if(placed) break;
            }
        }
        // Fallback for players flying while landing the finishing blow: find safe
        // End-stone surface nearby instead of waiting for them to touch the ground.
        if (!placed) {
            for (int i=0;i<40;i++) {
                double a=level.random.nextDouble()*Math.PI*2.0D;
                double radius=5.0D+level.random.nextDouble()*15.0D;
                double x=target.getX()+Math.cos(a)*radius;
                double z=target.getZ()+Math.sin(a)*radius;
                int ix=(int)Math.floor(x),iz=(int)Math.floor(z);
                int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,ix,iz);
                BlockPos feet=new BlockPos(ix,y,iz);
                if (!level.hasChunkAt(feet) || !level.getBlockState(feet.below()).blocksMotion()) continue;
                uno.snapTo(x,y,z,level.random.nextFloat()*360F,0F);
                if (level.noCollision(uno,uno.getBoundingBox())) { placed=true;break; }
            }
        }
        if (!placed) return; // Never spawn above End void; retry while dragon remains dying.

        if(!level.addFreshEntity(uno)) return;
        com.mdk.cosmetic.platform.PersistentData.of(dragon).putBoolean(TRIGGERED,true);
        uno.markDragonEncounter(target);
        level.playSound(null,uno.blockPosition(),SoundEvents.WARDEN_ROAR,SoundSource.HOSTILE,2.6F,0.55F);
        level.playSound(null,uno.blockPosition(),SoundEvents.WITHER_SPAWN,SoundSource.HOSTILE,1.4F,0.55F);
        // Only players actually present at the End battle see the cinematic.
        for (ServerPlayer viewer:level.players()) {
            if (viewer.distanceToSqr(uno)<128D*128D && viewer.isAlive()) {
                PacketDistributor.sendToPlayer(viewer,new UnoEncounterPayload(40,20));
            }
        }
    }
}
