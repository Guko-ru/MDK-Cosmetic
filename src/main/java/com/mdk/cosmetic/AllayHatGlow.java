package com.mdk.cosmetic;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;

/** Subtle vanilla particles; no detached decorative model cubes. */
public final class AllayHatGlow {
    private AllayHatGlow() { }
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.player();
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (player.tickCount % 16 != 0) return;
        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(MDKCosmetic.ALLAY_HAT.get())) return;
        double angle = serverLevel.getRandom().nextDouble() * Math.PI * 2;
        double radius = 0.35 + serverLevel.getRandom().nextDouble() * 0.25;
        serverLevel.sendParticles(ParticleTypes.END_ROD,
                player.getX() + Math.cos(angle) * radius,
                player.getY() + player.getEyeHeight() + 0.25 + serverLevel.getRandom().nextDouble() * 0.20,
                player.getZ() + Math.sin(angle) * radius,
                1, 0, 0, 0, 0.004);
    }
}
