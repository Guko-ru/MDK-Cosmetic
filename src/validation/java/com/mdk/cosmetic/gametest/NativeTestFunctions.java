package com.mdk.cosmetic.gametest;
public final class NativeTestFunctions {
 public static void register(net.minecraftforge.eventbus.api.bus.BusGroup bus){
  var functions=net.minecraftforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.TEST_FUNCTION,"mdkcosmetic_validation");
  functions.register("creative_tabs_are_visible_and_populated",()->helper->{try{NativePortGameTests.creativeTabsAreVisibleAndPopulated(helper);}catch(java.lang.Exception error){throw new RuntimeException(error);}});
  functions.register("fox_shearing_and_persistent_cooldown",()->helper->{try{NativePortGameTests.foxShearingAndPersistentCooldown(helper);}catch(java.lang.Exception error){throw new RuntimeException(error);}});
  functions.register("collector_ore_requires_multitool",()->helper->{try{NativePortGameTests.collectorOreRequiresMultitool(helper);}catch(java.lang.Exception error){throw new RuntimeException(error);}});
  functions.register("sakura_reach_is_head_only",()->helper->{try{NativePortGameTests.sakuraReachIsHeadOnly(helper);}catch(java.lang.Exception error){throw new RuntimeException(error);}});
  functions.register("entities_and_loot_resources_load",()->helper->{try{NativePortGameTests.entitiesAndLootResourcesLoad(helper);}catch(java.lang.Exception error){throw new RuntimeException(error);}});
  functions.register("all_data_files_load",()->helper->{try{NativePortGameTests.allDataFilesLoad(helper);}catch(java.lang.Exception error){throw new RuntimeException(error);}});
  functions.register("equipment_combat_and_protection_survive_migration",()->helper->{try{NativePortGameTests.equipmentCombatAndProtectionSurviveMigration(helper);}catch(java.lang.Exception error){throw new RuntimeException(error);}});
  functions.register("inventories_and_world_data_survive_save_and_removal",()->helper->{try{NativePortGameTests.inventoriesAndWorldDataSurviveSaveAndRemoval(helper);}catch(java.lang.Exception error){throw new RuntimeException(error);}});
  functions.register(bus);
 }
}
