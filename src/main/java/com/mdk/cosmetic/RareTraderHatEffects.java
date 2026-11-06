package com.mdk.cosmetic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import com.mdk.cosmetic.platform.ForgeGameplayEvents.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;

/** Each bonus applies only while a real wearable occupies the HEAD slot. */
public final class RareTraderHatEffects {
    private RareTraderHatEffects() { }
    private static final ResourceLocation SAKURA_REACH_ID =
            ResourceLocation.fromNamespaceAndPath(MDKCosmetic.MOD_ID, "sakura_block_reach");

    public static void onAttributeModifiers(ItemAttributeModifierEvent event) {
        if (event.getItemStack().is(MDKCosmetic.SAKURA_THINKER.get())) {
            event.addModifier(Attributes.BLOCK_INTERACTION_RANGE,
                    new AttributeModifier(SAKURA_REACH_ID, 2.0D, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.HEAD);
        }

    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.player();
        if (!(player.level() instanceof ServerLevel server)) return;
        if (player.tickCount % 20 != 0) return;
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(MDKCosmetic.SAKURA_THINKER.get())) {
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 35, 0, true, false, true));
            for (int i = 0; i < 3; i++) {
                double a = server.random.nextDouble() * Math.PI * 2.0D;
                double r = 0.22D + server.random.nextDouble() * 0.42D;
                server.sendParticles(ParticleTypes.CHERRY_LEAVES,
                        player.getX() + Math.cos(a) * r,
                        player.getY() + player.getEyeHeight() + 0.15D + server.random.nextDouble() * 0.95D,
                        player.getZ() + Math.sin(a) * r,
                        1, 0.04D, 0.10D, 0.04D, 0.012D);
            }
        }

    }
}
