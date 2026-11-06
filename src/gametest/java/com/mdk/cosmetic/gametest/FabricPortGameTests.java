package com.mdk.cosmetic.gametest;

import com.mdk.cosmetic.MDKCosmetic;
import com.mdk.cosmetic.platform.PersistentData;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import net.minecraft.core.BlockPos;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
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
public final class FabricPortGameTests {
    @GameTest
    public void creativeTabsAreVisibleAndPopulated(GameTestHelper helper){
        net.minecraft.world.item.CreativeModeTabs.tryRebuildTabContents(helper.getLevel().enabledFeatures(),true,helper.getLevel().registryAccess());
        var cosmetic=MDKCosmetic.COSMETIC_TAB.get();var decor=MDKCosmetic.FUR_BLOCKS_TAB.get();
        var visible=net.minecraft.world.item.CreativeModeTabs.tabs();
        if(!visible.contains(cosmetic)||!visible.contains(decor)||cosmetic.getDisplayItems().size()<60||decor.getDisplayItems().size()<1000)throw new AssertionError("Both MDK creative tabs must be visible and populated");
        helper.succeed();
    }

    @GameTest
    public void foxShearingAndPersistentCooldown(GameTestHelper helper){
        var fox=helper.spawn(EntityType.FOX,2,2,2);
        var player=helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.SHEARS));
        var first=UseEntityCallback.EVENT.invoker().interact(player,helper.getLevel(),InteractionHand.MAIN_HAND,fox,null);
        assertTrue(helper,first==InteractionResult.SUCCESS,"Fabric interaction must consume shearing");
        int before=helper.getLevel().getEntitiesOfClass(ItemEntity.class,fox.getBoundingBox().inflate(4),e->e.getItem().is(MDKCosmetic.FOX_FUR.get())).size();
        assertTrue(helper,before==1,"First shearing must produce exactly one fur");
        UseEntityCallback.EVENT.invoker().interact(player,helper.getLevel(),InteractionHand.MAIN_HAND,fox,null);
        int after=helper.getLevel().getEntitiesOfClass(ItemEntity.class,fox.getBoundingBox().inflate(4),e->e.getItem().is(MDKCosmetic.FOX_FUR.get())).size();
        assertTrue(helper,after==before,"Shearing cooldown must prevent a second fur");
        var tag=com.mdk.cosmetic.platform.LegacyValues.saveEntity(fox);
        var restored=EntityType.FOX.create(helper.getLevel(),net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);com.mdk.cosmetic.platform.LegacyValues.loadEntity(restored,tag);
        assertTrue(helper,PersistentData.of(restored).getLongOr("mdkcosmetic.nextFoxFurShear",0L)>System.currentTimeMillis(),"Cooldown must survive entity save/load");
        helper.succeed();
    }

    @GameTest
    public void collectorOreRequiresMultitool(GameTestHelper helper){
        var player=helper.makeMockPlayer(GameType.SURVIVAL);
        var ore=MDKCosmetic.COLLECTOR_ORE_STONE.get().defaultBlockState();
        player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.NETHERITE_PICKAXE));
        assertTrue(helper,player.getDestroySpeed(ore)==0,"Ordinary pickaxe must not mine Collector ore");
        assertTrue(helper,!PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(helper.getLevel(),player,BlockPos.ZERO,ore,null),"Fabric block break callback must reject ordinary tool");
        player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(MDKCosmetic.COLLECTOR_PICKAXE.get()));
        assertTrue(helper,player.getDestroySpeed(ore)>0,"Collector multitool must mine Collector ore");
        assertTrue(helper,PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(helper.getLevel(),player,BlockPos.ZERO,ore,null),"Fabric block break callback must allow Collector tool");
        helper.succeed();
    }

    @GameTest
    public void sakuraReachIsHeadOnly(GameTestHelper helper){
        var stack=new ItemStack(MDKCosmetic.SAKURA_THINKER.get());
        double[] reach={0};
        stack.forEachModifier(EquipmentSlot.HEAD,(attribute,modifier)->{if(attribute.equals(Attributes.BLOCK_INTERACTION_RANGE))reach[0]+=modifier.amount();});
        assertTrue(helper,reach[0]==2,"Sakura hat must provide exactly +2 block reach on head");
        reach[0]=0;
        stack.forEachModifier(EquipmentSlot.MAINHAND,(attribute,modifier)->{if(attribute.equals(Attributes.BLOCK_INTERACTION_RANGE))reach[0]+=modifier.amount();});
        assertTrue(helper,reach[0]==0,"Sakura reach must not apply while held in hand");
        helper.succeed();
    }

    @GameTest
    public void entitiesAndLootResourcesLoad(GameTestHelper helper){
        for(var type:java.util.List.of(MDKCosmetic.COLLECTOR_SOUL_ENTITY.get(),MDKCosmetic.UNO_ENTITY.get(),MDKCosmetic.COLLECTOR_HEART_ENTITY.get())){
            var entity=type.create(helper.getLevel(),net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
            assertTrue(helper,entity!=null,"Registered custom entity must instantiate");
            assertTrue(helper,entity.getAttribute(Attributes.MAX_HEALTH)!=null,"Entity attributes must be registered on Fabric");
        }
        var registry=helper.getLevel().registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
        for(String id:java.util.List.of("collector_ore_overworld","collector_ore_nether","collector_ore_end","collector_ore_end_surface")){
            assertTrue(helper,registry.containsKey(Identifier.fromNamespaceAndPath("mdkcosmetic",id)),"Ore placement resource must load: "+id);
        }
        var key=ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse("mdkcosmetic:chests/collector_book"));
        var table=helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
        assertTrue(helper,table!=net.minecraft.world.level.storage.loot.LootTable.EMPTY,"Collector chest loot table must load");
        helper.succeed();
    }
    @GameTest
    public void allDataFilesLoad(GameTestHelper helper) throws java.io.IOException {
        try(var in=FabricPortGameTests.class.getResourceAsStream("/mdkcosmetic-expected-content.json")){
            var expected=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            var server=helper.getLevel().getServer();
            for(var value:expected.getAsJsonArray("recipe")){
                var id=Identifier.parse(value.getAsString());
                assertTrue(helper,server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE,id)).isPresent(),"Recipe file must decode and register: "+id);
            }
            for(var value:expected.getAsJsonArray("advancement")){
                var id=Identifier.parse(value.getAsString());
                assertTrue(helper,server.getAdvancements().get(id)!=null,"Advancement file must decode and register: "+id);
            }
            for(var value:expected.getAsJsonArray("loot_table")){
                var id=Identifier.parse(value.getAsString());
                assertTrue(helper,server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,id))!=net.minecraft.world.level.storage.loot.LootTable.EMPTY,"Loot table file must decode and register: "+id);
            }
        }
        helper.succeed();
    }

    @GameTest
    public void equipmentCombatAndProtectionSurviveMigration(GameTestHelper helper){
        var level=helper.getLevel();
        var player=helper.makeMockPlayer(GameType.SURVIVAL);
        var hat=new ItemStack(MDKCosmetic.FOX_HAT.get());
        assertTrue(helper,hat.getMaxDamage()==110,"Hat durability must remain 110");
        assertTrue(helper,!hat.has(net.minecraft.core.component.DataComponents.ENCHANTABLE),"Cosmetic hat must remain non-enchantable");
        player.setItemInHand(InteractionHand.MAIN_HAND,hat);
        hat.use(level,player,InteractionHand.MAIN_HAND);
        assertTrue(helper,player.getItemBySlot(EquipmentSlot.HEAD).is(MDKCosmetic.FOX_HAT.get()),"Native equippable component must equip a hat on use");
        assertTrue(helper,new ItemStack(MDKCosmetic.COLLECTOR_ORE_STONE.get()).getItem().getDescriptionId().startsWith("block.mdkcosmetic."),"Block items must retain block translation keys");
        var tool=new ItemStack(MDKCosmetic.COLLECTOR_PICKAXE.get());
        for(var block:java.util.List.of(net.minecraft.world.level.block.Blocks.STONE,net.minecraft.world.level.block.Blocks.OAK_LOG,net.minecraft.world.level.block.Blocks.DIRT)){
            assertTrue(helper,tool.getDestroySpeed(block.defaultBlockState())==11F,"Multitool must retain mining speed for all three tool categories");
        }
        var input=new net.minecraft.world.item.crafting.SingleRecipeInput(new ItemStack(Items.IRON_ORE));
        var smelt=level.recipeAccess().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMELTING,input,level);
        assertTrue(helper,smelt.orElseThrow().value().assemble(input).is(Items.IRON_INGOT),"Backpack smelting must use the new recipe assembly API");
        assertTrue(helper,level.fuelValues().burnDuration(new ItemStack(Items.COAL))>0,"Backpack fuel duration must resolve from the world");
        var soul=MDKCosmetic.COLLECTOR_SOUL_ENTITY.get().create(level,net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        var warden=EntityType.WARDEN.create(level,net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
        assertTrue(helper,soul.isAlliedTo(warden),"Soul must remain allied with Wardens");
        assertTrue(helper,!soul.hurtServer(level,level.damageSources().mobAttack(warden),8F),"Warden attacks must not damage the soul");
        assertTrue(helper,soul.hurtServer(level,level.damageSources().generic(),1F),"Ordinary server damage must still trigger soul injury behavior");
        var relic=new ItemEntity(level,0,0,0,new ItemStack(MDKCosmetic.COLLECTOR_PICKAXE.get()));
        assertTrue(helper,!relic.hurtServer(level,level.damageSources().lava(),100F),"Collector relic must survive lava through the migrated invulnerability hook");
        helper.succeed();
    }

    private static void assertTrue(GameTestHelper helper,boolean condition,String message){helper.assertTrue(condition,net.minecraft.network.chat.Component.literal(message));}
    @GameTest
    public void inventoriesAndWorldDataSurviveSaveAndRemoval(GameTestHelper helper) throws java.io.IOException {
        var level=helper.getLevel();var registries=level.registryAccess();
        var bag=new ItemStack(MDKCosmetic.COLLECTOR_BACKPACK_REINFORCED.get());
        var items=com.mdk.cosmetic.CollectorBackpackData.items(bag,registries);
        var diamonds=new ItemStack(Items.DIAMOND,17);
        diamonds.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Saved diamonds"));
        items.set(0,diamonds);items.set(items.size()-1,new ItemStack(Items.EMERALD,23));
        com.mdk.cosmetic.CollectorBackpackData.saveItems(bag,items,registries);
        com.mdk.cosmetic.CollectorBackpackData.setUpgradeItem(bag,0,new ItemStack(MDKCosmetic.COLLECTOR_BACKPACK_MAGNET.get()),registries);
        var saved=(CompoundTag)com.mdk.cosmetic.platform.StackNbt.save(bag,registries);
        var restored=com.mdk.cosmetic.platform.StackNbt.parse(registries,saved);
        var loaded=com.mdk.cosmetic.CollectorBackpackData.items(restored,registries);
        assertTrue(helper,ItemStack.isSameItemSameComponents(loaded.get(0),diamonds)&&loaded.get(0).getCount()==17,"Backpack must retain item components and counts after NBT serialization");
        assertTrue(helper,loaded.get(44).is(Items.EMERALD)&&loaded.get(44).getCount()==23,"Backpack must retain its final storage slot");
        assertTrue(helper,com.mdk.cosmetic.CollectorBackpackData.hasMagnet(restored,registries),"Backpack must retain its installed module");
        assertTrue(helper,com.mdk.cosmetic.platform.StackNbt.parse(registries,(CompoundTag)com.mdk.cosmetic.platform.StackNbt.save(ItemStack.EMPTY,registries)).isEmpty(),"Empty item stacks must round-trip without throwing");
        var pos=helper.absolutePos(new BlockPos(2,2,2));
        level.setBlock(pos,MDKCosmetic.ESSENCE_WORKTABLE.get().defaultBlockState(),3);
        var table=(com.mdk.cosmetic.EssenceWorktableBlockEntity)level.getBlockEntity(pos);
        table.setItem(0,new ItemStack(Items.AMETHYST_SHARD,19));
        level.destroyBlock(pos,false);
        int dropped=level.getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(pos).inflate(2),e->e.getItem().is(Items.AMETHYST_SHARD)).stream().mapToInt(e->e.getItem().getCount()).sum();
        assertTrue(helper,dropped==19,"Breaking worktable must drop every stored item exactly once");
        var corePos=helper.absolutePos(new BlockPos(5,2,2));
        level.setBlock(corePos,MDKCosmetic.COLLECTOR_MEMORY_CORE.get().defaultBlockState(),3);
        var core=(com.mdk.cosmetic.CollectorMemoryCoreBlockEntity)level.getBlockEntity(corePos);
        core.setItem(0,new ItemStack(Items.GOLD_INGOT,13));
        var identity=core.identity();
        level.destroyBlock(corePos,true);
        var coreDrops=level.getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(corePos).inflate(2),e->e.getItem().is(MDKCosmetic.COLLECTOR_MEMORY_CORE_ITEM.get()));
        assertTrue(helper,coreDrops.size()==1,"Breaking memory core must create one portable core");
        var memory=coreDrops.getFirst().getItem().get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty("MDKMemory");
        var copy=new com.mdk.cosmetic.CollectorMemoryCoreBlockEntity(corePos,MDKCosmetic.COLLECTOR_MEMORY_CORE.get().defaultBlockState());copy.importMemory(memory,registries);
        assertTrue(helper,copy.identity().equals(identity)&&copy.getItem(0).is(Items.GOLD_INGOT)&&copy.getItem(0).getCount()==13,"Portable memory core must retain identity and furnace inventory");
        assertTrue(helper,level.getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(corePos).inflate(2),e->e.getItem().is(Items.GOLD_INGOT)).isEmpty(),"Memory core must not duplicate its furnace contents into loose drops");
        var stress=com.mdk.cosmetic.CollectorStressData.forLevel(level);stress.store(corePos,61,true);level.getDataStorage().saveAndJoin();
        var dataFolder=level.dimension().identifier().resolveAgainst(level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("dimensions")).resolve("data");
        var dataFile=net.minecraft.resources.Identifier.withDefaultNamespace("mdkcosmetic_collector_stress.dat").resolveAgainst(dataFolder);
        var disk=level.getDataStorage().readTagFromDisk(dataFile,null,0);
        assertTrue(helper,disk.getCompoundOrEmpty("data").getListOrEmpty("Storages").compoundStream().anyMatch(t->t.getLongOr("Position",0L)==corePos.asLong()&&t.getIntOr("Stress",0)==61&&t.getBooleanOr("Activated",false)),"Native SavedData codec must persist existing storage schema on disk");
        helper.succeed();
    }
}
