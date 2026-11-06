package com.mdk.cosmetic.platform;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.event.MutableEvent;
import net.minecraftforge.eventbus.api.bus.EventBus;
public final class ForgeEntityTicks {
 private ForgeEntityTicks(){}
 public static final class EntityTickEvent {
  public static final class Post extends MutableEvent {
  public static final EventBus<Post> BUS=EventBus.create(Post.class);
   private final Entity entity;
   public Post(Entity entity){this.entity=entity;}
   public Entity getEntity(){return entity;}
  }
 }
}
