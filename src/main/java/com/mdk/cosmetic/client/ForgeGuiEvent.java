package com.mdk.cosmetic.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.eventbus.api.Event;
public final class ForgeGuiEvent {
 public static final class Post extends Event {
  private final GuiGraphics graphics;
  public Post(GuiGraphics graphics){this.graphics=graphics;}
  public GuiGraphics getGuiGraphics(){return graphics;}
 }
}
