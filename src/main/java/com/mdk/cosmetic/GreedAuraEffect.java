package com.mdk.cosmetic;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Cosmetic HUD marker; no gameplay modifiers. Duration follows the Greed Orb buffs. */
public final class GreedAuraEffect extends MobEffect {
    public GreedAuraEffect() { super(MobEffectCategory.BENEFICIAL, 0x961D4B); }
}
