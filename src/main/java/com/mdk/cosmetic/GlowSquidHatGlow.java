package com.mdk.cosmetic;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;

/** Soft glow-squid shimmer around the player's head while the hat is worn. */
public final class GlowSquidHatGlow {
    private GlowSquidHatGlow() { }
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.player();
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (player.tickCount % 10 != 0) return;
        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(MDKCosmetic.GLOW_SQUID_HAT.get())) return;
        double angle = serverLevel.getRandom().nextDouble() * Math.PI * 2;
        double radius = 0.26 + serverLevel.getRandom().nextDouble() * 0.20;
        serverLevel.sendParticles(ParticleTypes.GLOW,
                player.getX() + Math.cos(angle) * radius,
                player.getY() + player.getEyeHeight() + 0.10 + serverLevel.getRandom().nextDouble() * 0.28,
                player.getZ() + Math.sin(angle) * radius,
                1, 0.0, 0.0, 0.0, 0.0);
    }
}
