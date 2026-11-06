package com.mdk.cosmetic.client;
import java.util.IdentityHashMap;
import java.util.Map;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
/** Bridges the original item color functions into Minecraft's native client-item tint source. */
public final class CosmeticItemTints {
 @FunctionalInterface public interface Color {int getColor(ItemStack stack,int index);}
 private static final Map<Item,Color> COLORS=new IdentityHashMap<>();
 public static void register(Color color,Item... items){for(var item:items)COLORS.put(item,color);}
 public record Source(int index) implements ItemTintSource {
  public static final MapCodec<Source> CODEC=Codec.INT.fieldOf("index").xmap(Source::new,Source::index);
  @Override public int calculate(ItemStack stack,ClientLevel level,LivingEntity holder){var color=COLORS.get(stack.getItem());return net.minecraft.util.ARGB.opaque(color==null?0xFFFFFFFF:color.getColor(stack,index));}
  @Override public MapCodec<Source> type(){return CODEC;}
 }
}
