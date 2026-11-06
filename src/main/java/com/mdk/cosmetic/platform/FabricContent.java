package com.mdk.cosmetic.platform;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
/** Fabric equivalents of all original biome and loot modifiers, retaining exact tables and probabilities. */
public final class FabricContent {
 private FabricContent() {}
 public static void initialize(){
  BiomeModifications.addFeature(BiomeSelectors.tag(TagKey.create(Registries.BIOME,Identifier.parse("c:is_end"))),GenerationStep.Decoration.UNDERGROUND_ORES,ResourceKey.create(Registries.PLACED_FEATURE,Identifier.parse("mdkcosmetic:collector_ore_end")));
  BiomeModifications.addFeature(BiomeSelectors.tag(TagKey.create(Registries.BIOME,Identifier.parse("c:is_end"))),GenerationStep.Decoration.UNDERGROUND_ORES,ResourceKey.create(Registries.PLACED_FEATURE,Identifier.parse("mdkcosmetic:collector_ore_end_surface")));
  BiomeModifications.addFeature(BiomeSelectors.tag(TagKey.create(Registries.BIOME,Identifier.parse("c:is_nether"))),GenerationStep.Decoration.UNDERGROUND_ORES,ResourceKey.create(Registries.PLACED_FEATURE,Identifier.parse("mdkcosmetic:collector_ore_nether")));
  BiomeModifications.addFeature(BiomeSelectors.tag(TagKey.create(Registries.BIOME,Identifier.parse("c:is_overworld"))),GenerationStep.Decoration.UNDERGROUND_ORES,ResourceKey.create(Registries.PLACED_FEATURE,Identifier.parse("mdkcosmetic:collector_ore_overworld")));
  LootTableEvents.MODIFY.register((key,builder,source,registries)->{
   if(key.identifier().equals(Identifier.parse("minecraft:chests/ancient_city")))builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(LootItemRandomChanceCondition.randomChance(0.2F)).add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse("mdkcosmetic:chests/collector_book")))));
   if(key.identifier().equals(Identifier.parse("minecraft:chests/abandoned_mineshaft")))builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(LootItemRandomChanceCondition.randomChance(0.08F)).add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse("mdkcosmetic:chests/collector_book")))));
   if(key.identifier().equals(Identifier.parse("minecraft:chests/stronghold_library")))builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(LootItemRandomChanceCondition.randomChance(0.12F)).add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse("mdkcosmetic:chests/collector_book")))));
   if(key.identifier().equals(Identifier.parse("minecraft:chests/trial_chambers/reward")))builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(LootItemRandomChanceCondition.randomChance(0.1F)).add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse("mdkcosmetic:chests/collector_book")))));
   if(key.identifier().equals(Identifier.parse("minecraft:chests/village/village_plains_house")))builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(LootItemRandomChanceCondition.randomChance(0.04F)).add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse("mdkcosmetic:chests/collector_book")))));
   if(key.identifier().equals(Identifier.parse("minecraft:chests/village/village_desert_house")))builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(LootItemRandomChanceCondition.randomChance(0.04F)).add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse("mdkcosmetic:chests/collector_book")))));
   if(key.identifier().equals(Identifier.parse("minecraft:chests/village/village_savanna_house")))builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(LootItemRandomChanceCondition.randomChance(0.04F)).add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse("mdkcosmetic:chests/collector_book")))));
   if(key.identifier().equals(Identifier.parse("minecraft:chests/village/village_snowy_house")))builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(LootItemRandomChanceCondition.randomChance(0.04F)).add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse("mdkcosmetic:chests/collector_book")))));
   if(key.identifier().equals(Identifier.parse("minecraft:chests/village/village_taiga_house")))builder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(LootItemRandomChanceCondition.randomChance(0.04F)).add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse("mdkcosmetic:chests/collector_book")))));
  });
 }
}
