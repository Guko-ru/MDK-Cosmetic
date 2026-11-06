package com.mdk.cosmetic;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

/** Server-side, once per death. Supports melee weapons and thrown enchanted tridents. */
public final class CollectorEssenceDrops {
    private CollectorEssenceDrops() { }
    public static final ResourceKey<Enchantment> COLLECTOR = ResourceKey.create(Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(MDKCosmetic.MOD_ID, "collector"));

    public static void onDrops(LivingDropsEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof LivingEntity holder) || attacker == victim) return;
        ItemStack weapon = event.getSource().getDirectEntity() instanceof ThrownTrident trident
                ? trident.getWeaponItem() : holder.getMainHandItem();
        if (weapon.isEmpty() || !weapon.is(MDKCosmetic.COLLECTOR_WEAPONS)) return;
        Holder<Enchantment> enchantment = victim.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(COLLECTOR);
        if (EnchantmentHelper.getItemEnchantmentLevel(enchantment, weapon) <= 0) return;

        int count = 1;
        double chance;
        if (victim instanceof WitherBoss || victim instanceof EnderDragon) { chance = 1.0; count = 4; }
        else if (victim instanceof ElderGuardian || victim instanceof Ravager || victim instanceof Evoker
                || victim instanceof Vindicator || victim instanceof PiglinBrute) chance = 0.65;
        else if (victim instanceof Monster) chance = 0.35;
        else chance = 0.10;
        if (victim.getRandom().nextDouble() >= chance) return;
        // Greed affects only shards dropped by this enchantment, not normal mob loot.
        if (holder instanceof net.minecraft.world.entity.player.Player player
                && player.hasEffect(MDKCosmetic.GREED_AURA.holder())) count *= 2;
        event.getDrops().add(new ItemEntity(victim.level(), victim.getX(), victim.getY()+0.20, victim.getZ(),
                new ItemStack(MDKCosmetic.POWER_ESSENCE.get(), count)));
        if (victim.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ENCHANT, victim.getX(), victim.getY()+0.85, victim.getZ(),
                    12, 0.3, 0.4, 0.3, 0.03);
        }
    }
}
