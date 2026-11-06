package com.mdk.cosmetic.platform;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
public final class EntityDrops {
 private EntityDrops() {}
 public static ItemEntity spawn(Entity entity,ItemStack stack){return entity.level() instanceof ServerLevel level?entity.spawnAtLocation(level,stack):null;}
 public static ItemEntity spawn(Entity entity,ItemStack stack,float offset){return entity.level() instanceof ServerLevel level?entity.spawnAtLocation(level,stack,offset):null;}
 public static ItemEntity spawn(Entity entity,ItemLike item){return entity.level() instanceof ServerLevel level?entity.spawnAtLocation(level,item):null;}
}
