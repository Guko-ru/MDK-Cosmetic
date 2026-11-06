package com.mdk.cosmetic;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;

/** Gentle decorative bubbles near the wearer, even on land. */
public final class AxolotlHatBubbles {
    private AxolotlHatBubbles() { }
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.player;
        if (!(player.level() instanceof ServerLevel server) || player.tickCount % 14 != 0) return;
        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(MDKCosmetic.AXOLOTL_HAT.get())) return;
        double a = server.random.nextDouble()*Math.PI*2D;
        double r = 0.25D + server.random.nextDouble()*0.30D;
        server.sendParticles(player.isInWater() ? ParticleTypes.BUBBLE : ParticleTypes.BUBBLE_POP,
                player.getX()+Math.cos(a)*r,
                player.getY()+player.getEyeHeight()+0.10D,
                player.getZ()+Math.sin(a)*r,
                1,0D,0.05D,0D,0.01D);
    }
}
