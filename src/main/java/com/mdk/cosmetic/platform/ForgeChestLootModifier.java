package com.mdk.cosmetic.platform;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
/** Native Forge loot modifier: retains the original chest conditions and probabilities. */
public final class ForgeChestLootModifier extends LootModifier {
 public static final MapCodec<ForgeChestLootModifier> CODEC=RecordCodecBuilder.mapCodec(instance->codecStart(instance).and(ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("table").forGetter(value->value.table)).apply(instance,ForgeChestLootModifier::new));
 private final ResourceKey<LootTable> table;
 public ForgeChestLootModifier(LootItemCondition[] conditions,ResourceKey<LootTable> table){super(conditions);this.table=table;}
 public static void register(IEventBus bus){
  var serializers=DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,"mdkcosmetic");
  serializers.register("add_chest_table",()->CODEC);serializers.register(bus);
 }
 @Override protected ObjectArrayList<ItemStack> doApply(LootTable originalTable,ObjectArrayList<ItemStack> generated,LootContext context){
  context.getResolver().get(table).ifPresent(holder->holder.value().getRandomItemsRaw(context,generated::add));return generated;
 }
 @Override public MapCodec<? extends IGlobalLootModifier> codec(){return CODEC;}
}
