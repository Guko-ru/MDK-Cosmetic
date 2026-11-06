package com.mdk.cosmetic.platform;
public final class ServerAccess {public static net.minecraft.server.MinecraftServer get(net.minecraft.world.entity.Entity entity){return entity.level() instanceof net.minecraft.server.level.ServerLevel level?level.getServer():null;}}
