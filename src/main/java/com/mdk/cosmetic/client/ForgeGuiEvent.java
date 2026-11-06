package com.mdk.cosmetic.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraftforge.eventbus.api.event.MutableEvent;
import net.minecraftforge.eventbus.api.bus.EventBus;
public final class ForgeGuiEvent {
 public static final class Post extends MutableEvent {
  public static final EventBus<Post> BUS=EventBus.create(Post.class);
  private final GuiGraphicsExtractor graphics;
  public Post(GuiGraphicsExtractor graphics){this.graphics=graphics;}
  public GuiGraphicsExtractor getGuiGraphics(){return graphics;}
 }
}
