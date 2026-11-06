package com.mdk.cosmetic.client;
import java.util.*;
import net.minecraft.world.item.Item;
public final class ForgeArmorExtensions {
 public static final Map<Item,ForgeArmorRenderer> RENDERERS=new IdentityHashMap<>();
 public static final class Registration {
  public void registerItem(ArmorAppearance appearance,Item... items){var renderer=new ForgeArmorRenderer(appearance);for(var item:items)RENDERERS.put(item,renderer);}
 }
}
