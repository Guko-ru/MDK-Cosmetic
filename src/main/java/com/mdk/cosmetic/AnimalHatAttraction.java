package com.mdk.cosmetic;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Adds the same custom AI goal to vanilla foxes and pandas on the logical server. */
public final class AnimalHatAttraction {
    // Weak keys prevent retaining unloaded mobs, and avoid duplicate goals when
    // an entity is added to a Level more than once during its in-memory lifetime.
    private static final Set<Mob> REGISTERED = Collections.newSetFromMap(new WeakHashMap<>());

    private AnimalHatAttraction() { }

    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof Fox fox && REGISTERED.add(fox)) {
            fox.goalSelector.addGoal(1, new HatAttractionGoal(fox, MDKCosmetic.FOX_HAT.get(), 1.15D));
        } else if (event.getEntity() instanceof Cat cat && REGISTERED.add(cat)) {
            cat.goalSelector.addGoal(1, new HatAttractionGoal(cat, MDKCosmetic.CAT_EARS.get(), 1.10D));
        } else if (event.getEntity() instanceof Ocelot ocelot && REGISTERED.add(ocelot)) {
            ocelot.goalSelector.addGoal(1, new HatAttractionGoal(ocelot, MDKCosmetic.CAT_EARS.get(), 1.10D));
        } else if (event.getEntity() instanceof Rabbit rabbit && REGISTERED.add(rabbit)) {
            rabbit.goalSelector.addGoal(1, new HatAttractionGoal(rabbit, MDKCosmetic.RABBIT_EARS.get(), 1.20D));
        } else if (event.getEntity() instanceof Panda panda && REGISTERED.add(panda)) {
            panda.goalSelector.addGoal(1, new HatAttractionGoal(panda, MDKCosmetic.PANDA_HAT.get(), 1.00D));
        } else if (event.getEntity() instanceof Wolf wolf && REGISTERED.add(wolf)) {
            wolf.goalSelector.addGoal(5, new WolfHatAttractionGoal(wolf));
        } else if (event.getEntity() instanceof Sheep sheep && REGISTERED.add(sheep)) {
            sheep.goalSelector.addGoal(1, new HatAttractionGoal(sheep, MDKCosmetic.SHEEP_HAT.get(), 1.10D));
        } else if (event.getEntity() instanceof Horse horse && REGISTERED.add(horse)) {
            horse.goalSelector.addGoal(4, new HatAttractionGoal(horse, MDKCosmetic.HORSE_HAT.get(), 1.10D));
        } else if (event.getEntity() instanceof Donkey donkey && REGISTERED.add(donkey)) {
            donkey.goalSelector.addGoal(4, new HatAttractionGoal(donkey, MDKCosmetic.DONKEY_HAT.get(), 1.05D));
        } else if (event.getEntity() instanceof Llama llama && REGISTERED.add(llama)) {
            llama.goalSelector.addGoal(2, new LlamaHatAttractionGoal(llama));
        } else if (event.getEntity() instanceof PolarBear bear && REGISTERED.add(bear)) {
            // Lower priority than vanilla combat goals. No pacification of protective bears.
            bear.goalSelector.addGoal(5, new PolarBearHatAttractionGoal(bear));
        } else if (event.getEntity() instanceof MushroomCow mooshroom && REGISTERED.add(mooshroom)) {
            mooshroom.goalSelector.addGoal(1, new HatAttractionGoal(mooshroom, MDKCosmetic.MOOSHROOM_HAT.get(), 1.10D));
        } else if (event.getEntity() instanceof Cow cow && REGISTERED.add(cow)) {
            cow.goalSelector.addGoal(1, new HatAttractionGoal(cow, MDKCosmetic.COW_HAT.get(), 1.10D));
        }
    }
}
