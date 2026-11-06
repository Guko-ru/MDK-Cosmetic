package com.mdk.cosmetic.gametest;

import com.mdk.cosmetic.MDKCosmetic;
import com.mdk.cosmetic.platform.PersistentData;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
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
public final class FabricPortGameTests implements FabricGameTest {
    @GameTest(template=FabricGameTest.EMPTY_STRUCTURE)
    public void creativeTabsAreVisibleAndPopulated(GameTestHelper helper){
        net.minecraft.world.item.CreativeModeTabs.tryRebuildTabContents(helper.getLevel().enabledFeatures(),true,helper.getLevel().registryAccess());
        var cosmetic=MDKCosmetic.COSMETIC_TAB.get();var decor=MDKCosmetic.FUR_BLOCKS_TAB.get();
        var visible=net.minecraft.world.item.CreativeModeTabs.tabs();
        if(!visible.contains(cosmetic)||!visible.contains(decor)||cosmetic.getDisplayItems().size()<60||decor.getDisplayItems().size()<1000)throw new AssertionError("Both MDK creative tabs must be visible and populated");
        helper.succeed();
    }

    @GameTest(template=FabricGameTest.EMPTY_STRUCTURE)
    public void foxShearingAndPersistentCooldown(GameTestHelper helper){
        var fox=helper.spawn(EntityType.FOX,2,2,2);
        var player=helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.SHEARS));
        var first=UseEntityCallback.EVENT.invoker().interact(player,helper.getLevel(),InteractionHand.MAIN_HAND,fox,null);
        helper.assertTrue(first==InteractionResult.SUCCESS,"Fabric interaction must consume shearing");
        int before=helper.getLevel().getEntitiesOfClass(ItemEntity.class,fox.getBoundingBox().inflate(4),e->e.getItem().is(MDKCosmetic.FOX_FUR.get())).size();
        helper.assertTrue(before==1,"First shearing must produce exactly one fur");
        UseEntityCallback.EVENT.invoker().interact(player,helper.getLevel(),InteractionHand.MAIN_HAND,fox,null);
        int after=helper.getLevel().getEntitiesOfClass(ItemEntity.class,fox.getBoundingBox().inflate(4),e->e.getItem().is(MDKCosmetic.FOX_FUR.get())).size();
        helper.assertTrue(after==before,"Shearing cooldown must prevent a second fur");
        var tag=fox.saveWithoutId(new CompoundTag());
        var restored=EntityType.FOX.create(helper.getLevel());restored.load(tag);
        helper.assertTrue(PersistentData.of(restored).getLong("mdkcosmetic.nextFoxFurShear")>System.currentTimeMillis(),"Cooldown must survive entity save/load");
        helper.succeed();
    }

    @GameTest(template=FabricGameTest.EMPTY_STRUCTURE)
    public void collectorOreRequiresMultitool(GameTestHelper helper){
        var player=helper.makeMockPlayer(GameType.SURVIVAL);
        var ore=MDKCosmetic.COLLECTOR_ORE_STONE.get().defaultBlockState();
        player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.NETHERITE_PICKAXE));
        helper.assertTrue(player.getDestroySpeed(ore)==0,"Ordinary pickaxe must not mine Collector ore");
        helper.assertTrue(!PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(helper.getLevel(),player,BlockPos.ZERO,ore,null),"Fabric block break callback must reject ordinary tool");
        player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(MDKCosmetic.COLLECTOR_PICKAXE.get()));
        helper.assertTrue(player.getDestroySpeed(ore)>0,"Collector multitool must mine Collector ore");
        helper.assertTrue(PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(helper.getLevel(),player,BlockPos.ZERO,ore,null),"Fabric block break callback must allow Collector tool");
        helper.succeed();
    }

    @GameTest(template=FabricGameTest.EMPTY_STRUCTURE)
    public void sakuraReachIsHeadOnly(GameTestHelper helper){
        var stack=new ItemStack(MDKCosmetic.SAKURA_THINKER.get());
        double[] reach={0};
        stack.forEachModifier(EquipmentSlot.HEAD,(attribute,modifier)->{if(attribute.equals(Attributes.BLOCK_INTERACTION_RANGE))reach[0]+=modifier.amount();});
        helper.assertTrue(reach[0]==2,"Sakura hat must provide exactly +2 block reach on head");
        reach[0]=0;
        stack.forEachModifier(EquipmentSlot.MAINHAND,(attribute,modifier)->{if(attribute.equals(Attributes.BLOCK_INTERACTION_RANGE))reach[0]+=modifier.amount();});
        helper.assertTrue(reach[0]==0,"Sakura reach must not apply while held in hand");
        helper.succeed();
    }

    @GameTest(template=FabricGameTest.EMPTY_STRUCTURE)
    public void entitiesAndLootResourcesLoad(GameTestHelper helper){
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
    @GameTest(template=FabricGameTest.EMPTY_STRUCTURE)
    public void allDataFilesLoad(GameTestHelper helper) throws java.io.IOException {
        try(var in=FabricPortGameTests.class.getResourceAsStream("/mdkcosmetic-expected-content.json")){
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
}
