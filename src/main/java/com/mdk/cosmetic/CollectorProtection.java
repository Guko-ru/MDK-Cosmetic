package com.mdk.cosmetic;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import com.mdk.cosmetic.platform.Events.EntityInvulnerabilityCheckEvent;
import com.mdk.cosmetic.platform.Events.EntityJoinLevelEvent;
import com.mdk.cosmetic.platform.Events.ExplosionEvent;

/**
 * Collector relics persist through fire, lava, and ordinary explosions.
 *
 * The soul-storage stress meltdown intentionally removes only its own multiblock
 * blocks BEFORE creating the visual damaging explosion, so it remains functional.
 * Does not prevent normal player mining, item pickup or item despawn.
 */
public final class CollectorProtection {
    private CollectorProtection() {}

    private static boolean protectedPath(String path) {
        return path.startsWith("collector_")
                || path.startsWith("mystery_")
                || path.endsWith("_mystery_block")
                || path.startsWith("power_essence")
                || path.equals("charged_power_essence")
                || path.equals("power_concentrate")
                || path.equals("essence_worktable")
                || path.equals("greed_orb")
                || path.equals("productivity_orb");
    }

    private static boolean protectedId(Identifier id) {
        return id != null && MDKCosmetic.MOD_ID.equals(id.getNamespace()) && protectedPath(id.getPath());
    }

    public static boolean isProtected(ItemStack stack) {
        return !stack.isEmpty() && protectedId(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static boolean isProtected(BlockState state) {
        return protectedId(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    /** The stack component also makes vanilla ItemEntity's fire and lava checks protect the dropped item. */
    public static void onItemSpawn(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ItemEntity dropped)) return;
        ItemStack stack = dropped.getItem();
        if (isProtected(stack) && !stack.has(DataComponents.DAMAGE_RESISTANT)) {
            stack.set(DataComponents.DAMAGE_RESISTANT, new net.minecraft.world.item.component.DamageResistant(dropped.level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getOrThrow(net.minecraft.tags.DamageTypeTags.IS_FIRE)));
            dropped.setItem(stack);
        }
    }

    /** Covers existing drops from old saves, and explosive damage from other mods. */
    public static void onInvulnerabilityCheck(EntityInvulnerabilityCheckEvent event) {
        if (!(event.getEntity() instanceof ItemEntity dropped) || !isProtected(dropped.getItem())) return;
        if (event.getSource().is(DamageTypeTags.IS_FIRE) || event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
            event.setInvulnerable(true);
        }
    }

    /** The stress meltdown removes its own blocks explicitly before firing the explosion. */
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel().isClientSide()) return;
        event.getAffectedBlocks().removeIf(pos -> isProtected(event.getLevel().getBlockState(pos)));
    }
}
