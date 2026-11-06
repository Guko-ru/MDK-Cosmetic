package com.mdk.cosmetic.platform;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;
public final class ForgeEntityTicks {
 private ForgeEntityTicks(){}
 public static final class EntityTickEvent {
  public static final class Post extends Event {
   private final Entity entity;
   public Post(Entity entity){this.entity=entity;}
   public Entity getEntity(){return entity;}
  }
 }
}
