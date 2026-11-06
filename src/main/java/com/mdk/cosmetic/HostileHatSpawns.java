package com.mdk.cosmetic;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

/** Rare cosmetics on natural/spawner/spawn-egg hostile mobs; persists to avoid reroll on chunk reload. */
public final class HostileHatSpawns {
    private static final String ROLLED = "mdk_cosmetic_hat_roll_v1";
    private static final float CHANCE = 0.02F;
    private HostileHatSpawns() { }

    public static boolean eligible(Mob mob) {
        return mob instanceof Zombie || mob instanceof Skeleton || mob instanceof WitherSkeleton
                || mob instanceof ZombifiedPiglin || mob instanceof Piglin;
    }

    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob) || !eligible(mob)) return;
        CompoundTag tag = mob.getPersistentData();
        if (tag.getBoolean(ROLLED)) return;
        tag.putBoolean(ROLLED, true);
        if (!mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty() || mob.getRandom().nextFloat() >= CHANCE) return;
        List<Item> hats = List.of(
                MDKCosmetic.FOX_HAT.get(), MDKCosmetic.PANDA_HAT.get(), MDKCosmetic.CAT_EARS.get(),
                MDKCosmetic.ALLAY_HAT.get(), MDKCosmetic.AXOLOTL_HAT.get(), MDKCosmetic.RABBIT_EARS.get(),
                MDKCosmetic.OCTOPUS_HAT.get(), MDKCosmetic.GLOW_SQUID_HAT.get(), MDKCosmetic.CAMEL_HAT.get(),
                MDKCosmetic.COW_HAT.get(), MDKCosmetic.MOOSHROOM_HAT.get(), MDKCosmetic.POLAR_BEAR_HAT.get(),
                MDKCosmetic.LLAMA_HAT.get(), MDKCosmetic.SHEEP_HAT.get(), MDKCosmetic.WOLF_HAT.get(),
                MDKCosmetic.HORSE_HAT.get(), MDKCosmetic.DONKEY_HAT.get());
        mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(hats.get(mob.getRandom().nextInt(hats.size()))));
        mob.setDropChance(EquipmentSlot.HEAD, 0.08F);
    }
}
