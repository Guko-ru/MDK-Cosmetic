package com.mdk.cosmetic.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.eventbus.api.event.MutableEvent;
import net.minecraftforge.eventbus.api.bus.EventBus;
public final class ForgeGuiEvent {
 public static final class Post extends MutableEvent {
  public static final EventBus<Post> BUS=EventBus.create(Post.class);
  private final GuiGraphics graphics;
  public Post(GuiGraphics graphics){this.graphics=graphics;}
  public GuiGraphics getGuiGraphics(){return graphics;}
 }
}
