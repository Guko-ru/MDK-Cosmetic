package com.mdk.cosmetic.gametest;

import com.mdk.cosmetic.MDKCosmetic;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

/** Real Minecraft/Fabric integration tests; this test mod is excluded from the release JAR. */
@net.minecraftforge.gametest.GameTestHolder("mdkcosmetic_validation")
public final class NativePortGameTests {
    @GameTest(template="mdkcosmetic_validation:empty")
    public static void creativeTabsAreVisibleAndPopulated(GameTestHelper helper){
        net.minecraft.world.item.CreativeModeTabs.tryRebuildTabContents(helper.getLevel().enabledFeatures(),true,helper.getLevel().registryAccess());
        var cosmetic=MDKCosmetic.COSMETIC_TAB.get();var decor=MDKCosmetic.FUR_BLOCKS_TAB.get();
        var visible=net.minecraft.world.item.CreativeModeTabs.tabs();
        if(!visible.contains(cosmetic)||!visible.contains(decor)||cosmetic.getDisplayItems().size()<60||decor.getDisplayItems().size()<1000)throw new AssertionError("Both MDK creative tabs must be visible and populated");
        helper.succeed();
    }

    @GameTest(template="mdkcosmetic_validation:empty")
    public static void foxShearingAndPersistentCooldown(GameTestHelper helper){
        var fox=helper.spawn(EntityType.FOX,2,2,2);
        var player=helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.SHEARS));
        var first=interact(player,fox);
        helper.assertTrue(first==InteractionResult.SUCCESS,"Fabric interaction must consume shearing");
        int before=helper.getLevel().getEntitiesOfClass(ItemEntity.class,fox.getBoundingBox().inflate(4),e->e.getItem().is(MDKCosmetic.FOX_FUR.get())).size();
        helper.assertTrue(before==1,"First shearing must produce exactly one fur");
        interact(player,fox);
        int after=helper.getLevel().getEntitiesOfClass(ItemEntity.class,fox.getBoundingBox().inflate(4),e->e.getItem().is(MDKCosmetic.FOX_FUR.get())).size();
        helper.assertTrue(after==before,"Shearing cooldown must prevent a second fur");
        var tag=fox.saveWithoutId(new CompoundTag());
        var restored=EntityType.FOX.create(helper.getLevel());restored.load(tag);
        helper.assertTrue(restored.getPersistentData().getLong("mdkcosmetic.nextFoxFurShear")>System.currentTimeMillis(),"Cooldown must survive entity save/load");
        helper.succeed();
    }

    @GameTest(template="mdkcosmetic_validation:empty")
    public static void collectorOreRequiresMultitool(GameTestHelper helper){
        var player=helper.makeMockPlayer(GameType.SURVIVAL);
        var ore=MDKCosmetic.COLLECTOR_ORE_STONE.get().defaultBlockState();
        player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.NETHERITE_PICKAXE));
        helper.assertTrue(player.getDestroySpeed(ore)==0,"Ordinary pickaxe must not mine Collector ore");
        helper.assertTrue(!mayBreak(helper.getLevel(),player,ore),"Fabric block break callback must reject ordinary tool");
        player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(MDKCosmetic.COLLECTOR_PICKAXE.get()));
        helper.assertTrue(player.getDestroySpeed(ore)>0,"Collector multitool must mine Collector ore");
        helper.assertTrue(mayBreak(helper.getLevel(),player,ore),"Fabric block break callback must allow Collector tool");
        helper.succeed();
    }

    @GameTest(template="mdkcosmetic_validation:empty")
    public static void sakuraReachIsHeadOnly(GameTestHelper helper){
        var stack=new ItemStack(MDKCosmetic.SAKURA_THINKER.get());
        double[] reach={0};
        stack.forEachModifier(EquipmentSlot.HEAD,(attribute,modifier)->{if(attribute.equals(Attributes.BLOCK_INTERACTION_RANGE))reach[0]+=modifier.amount();});
        helper.assertTrue(reach[0]==2,"Sakura hat must provide exactly +2 block reach on head");
        reach[0]=0;
        stack.forEachModifier(EquipmentSlot.MAINHAND,(attribute,modifier)->{if(attribute.equals(Attributes.BLOCK_INTERACTION_RANGE))reach[0]+=modifier.amount();});
        helper.assertTrue(reach[0]==0,"Sakura reach must not apply while held in hand");
        helper.succeed();
    }

    @GameTest(template="mdkcosmetic_validation:empty")
    public static void entitiesAndLootResourcesLoad(GameTestHelper helper){
        for(var type:java.util.List.of(MDKCosmetic.COLLECTOR_SOUL_ENTITY.get(),MDKCosmetic.UNO_ENTITY.get(),MDKCosmetic.COLLECTOR_HEART_ENTITY.get())){
            var entity=type.create(helper.getLevel());
            helper.assertTrue(entity!=null,"Registered custom entity must instantiate");
            helper.assertTrue(entity.getAttribute(Attributes.MAX_HEALTH)!=null,"Entity attributes must be registered on Fabric");
        }
        var registry=helper.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        for(String id:java.util.List.of("collector_ore_overworld","collector_ore_nether","collector_ore_end","collector_ore_end_surface")){
            helper.assertTrue(registry.containsKey(ResourceLocation.fromNamespaceAndPath("mdkcosmetic",id)),"Ore placement resource must load: "+id);
        }
        var key=ResourceKey.create(Registries.LOOT_TABLE,ResourceLocation.parse("mdkcosmetic:chests/collector_book"));
        var table=helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
        helper.assertTrue(table!=net.minecraft.world.level.storage.loot.LootTable.EMPTY,"Collector chest loot table must load");
        helper.succeed();
    }
    @GameTest(template="mdkcosmetic_validation:empty")
    public static void allDataFilesLoad(GameTestHelper helper) throws java.io.IOException {
        assertNativeChestLoot(helper);
        try(var in=NativePortGameTests.class.getResourceAsStream("/mdkcosmetic-expected-content.json")){
            var expected=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            var server=helper.getLevel().getServer();
            for(var value:expected.getAsJsonArray("recipe")){
                var id=ResourceLocation.parse(value.getAsString());
                helper.assertTrue(server.getRecipeManager().byKey(id).isPresent(),"Recipe file must decode and register: "+id);
            }
            for(var value:expected.getAsJsonArray("advancement")){
                var id=ResourceLocation.parse(value.getAsString());
                helper.assertTrue(server.getAdvancements().get(id)!=null,"Advancement file must decode and register: "+id);
            }
            for(var value:expected.getAsJsonArray("loot_table")){
                var id=ResourceLocation.parse(value.getAsString());
                helper.assertTrue(server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,id))!=net.minecraft.world.level.storage.loot.LootTable.EMPTY,"Loot table file must decode and register: "+id);
            }
        }
        helper.succeed();
    }
 private static InteractionResult interact(net.minecraft.world.entity.player.Player player,net.minecraft.world.entity.Entity target){var event=new net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract(player,InteractionHand.MAIN_HAND,target);net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);return event.getCancellationResult();}
 private static boolean mayBreak(net.minecraft.world.level.Level level,net.minecraft.world.entity.player.Player player,net.minecraft.world.level.block.state.BlockState state){var event=new net.minecraftforge.event.level.BlockEvent.BreakEvent(level,BlockPos.ZERO,state,player);net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);return !event.isCanceled();}

 private static void assertNativeChestLoot(GameTestHelper helper){
  var level=helper.getLevel();var server=level.getServer();
  try{
   var accessor=net.minecraftforge.common.ForgeInternalHandler.class.getDeclaredMethod("getLootModifierManager");accessor.setAccessible(true);
   var manager=(net.minecraftforge.common.loot.LootModifierManager)accessor.invoke(null);
   if(manager.getAllLootMods().stream().filter(value->value instanceof com.mdk.cosmetic.platform.ForgeChestLootModifier).count()!=9)throw new AssertionError("All nine native Forge chest modifiers must load");
  }catch(ReflectiveOperationException error){throw new AssertionError(error);}
  var params=new net.minecraft.world.level.storage.loot.LootParams.Builder(level).withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,net.minecraft.world.phys.Vec3.ZERO).create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
  var table=server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,ResourceLocation.parse("minecraft:chests/abandoned_mineshaft")));
  var collector=level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(ResourceKey.create(Registries.ENCHANTMENT,ResourceLocation.parse("mdkcosmetic:collector")));
  int found=0;for(long seed=1;seed<=256;seed++)for(var item:table.getRandomItems(params,seed))if(item.getOrDefault(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS,net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY).getLevel(collector)>0){found++;break;}
  if(found<1||found>=64)throw new AssertionError("Seeded mineshaft Collector book chance must remain near original 8%, observed "+found+"/256");
  org.slf4j.LoggerFactory.getLogger("mdkcosmetic-test").info("MDK_NATIVE_CHEST_LOOT_OK: all 9 Forge modifiers loaded; original chest book chance retained");
 }
}
