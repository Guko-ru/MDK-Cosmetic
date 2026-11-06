package com.mdk.cosmetic;

import java.util.EnumMap;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.resources.ResourceKey;
import com.mdk.cosmetic.platform.EnchantedBooks;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import com.mdk.cosmetic.platform.CosmeticArmorItem;
import net.minecraft.world.item.equipment.ArmorType;
import com.mdk.cosmetic.platform.ArmorDefinition;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.item.crafting.Ingredient;
import com.mdk.cosmetic.platform.RegistryEntry;
import com.mdk.cosmetic.platform.RegistryBatch;

/** MDK Cosmetic registry. New refinement GUI in v1.18.6 retains legacy registry IDs. */
@Mod(MDKCosmetic.MOD_ID)
public final class MDKCosmetic {
    public static final String MOD_ID = "mdkcosmetic";
    public static final ResourceLocation COLLECTOR_ENCHANTMENT_ID =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "collector");
    public static final TagKey<Item> COLLECTOR_WEAPONS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "collector_weapons"));
    private static final RegistryBatch<ArmorDefinition> ARMOR_MATERIALS =
            RegistryBatch.createValues(MOD_ID);
    private static final RegistryBatch<MobEffect> MOB_EFFECTS =
            RegistryBatch.create(Registries.MOB_EFFECT, MOD_ID);
    public static final RegistryEntry<MobEffect, GreedAuraEffect> GREED_AURA =
            MOB_EFFECTS.register("greed_aura", GreedAuraEffect::new);
    public static final RegistryEntry<MobEffect, ProductivityAuraEffect> PRODUCTIVITY_AURA =
            MOB_EFFECTS.register("productivity_aura", ProductivityAuraEffect::new);
    private static final RegistryBatch<StructureType<?>> STRUCTURE_TYPES =
            RegistryBatch.create(Registries.STRUCTURE_TYPE, MOD_ID);
    private static final RegistryBatch<StructurePieceType> STRUCTURE_PIECES =
            RegistryBatch.create(Registries.STRUCTURE_PIECE, MOD_ID);
    public static final RegistryEntry<StructureType<?>, StructureType<CollectorOriginStructure>> COLLECTOR_ORIGIN_STRUCTURE_TYPE =
            STRUCTURE_TYPES.register("collector_origin", () -> () -> CollectorOriginStructure.CODEC);
    public static final RegistryEntry<StructurePieceType, StructurePieceType> COLLECTOR_ORIGIN_PIECE_TYPE =
            STRUCTURE_PIECES.register("collector_origin_piece", () -> (StructurePieceType.ContextlessType) CollectorOriginPiece::new);
    private static final RegistryBatch<Item> ITEMS = RegistryBatch.create(Registries.ITEM, MOD_ID);
    private static final RegistryBatch<EntityType<?>> ENTITY_TYPES =
            RegistryBatch.create(Registries.ENTITY_TYPE, MOD_ID);
    public static final RegistryEntry<EntityType<?>, EntityType<CollectorSoulEntity>> COLLECTOR_SOUL_ENTITY =
            ENTITY_TYPES.register("collector_soul_entity", () -> EntityType.Builder.of(
                    CollectorSoulEntity::new, MobCategory.MONSTER)
                    .sized(0.95F,0.95F).clientTrackingRange(9).updateInterval(2)
                    .build(com.mdk.cosmetic.platform.RegistryContext.entityKey()));
    public static final RegistryEntry<EntityType<?>, EntityType<UnoEntity>> UNO_ENTITY =
            ENTITY_TYPES.register("uno", () -> EntityType.Builder.of(
                    UnoEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(9).updateInterval(2)
                    .build(com.mdk.cosmetic.platform.RegistryContext.entityKey()));

    public static final RegistryEntry<EntityType<?>, EntityType<CollectorHeartEntity>> COLLECTOR_HEART_ENTITY =
            ENTITY_TYPES.register("collector_heart", () -> EntityType.Builder.of(
                    CollectorHeartEntity::new, MobCategory.MONSTER)
                    .sized(1.25F, 1.25F).clientTrackingRange(10).updateInterval(2)
                    .build(com.mdk.cosmetic.platform.RegistryContext.entityKey()));

    private static final RegistryBatch<Block> BLOCKS = RegistryBatch.create(Registries.BLOCK, MOD_ID);
    // All items from the mod appear in one dedicated creative inventory tab.
    private static final RegistryBatch<BlockEntityType<?>> BLOCK_ENTITIES =
            RegistryBatch.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    private static final RegistryBatch<MenuType<?>> MENUS =
            RegistryBatch.create(Registries.MENU, MOD_ID);
    private static final RegistryBatch<CreativeModeTab> CREATIVE_TABS =
            RegistryBatch.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    /** Green default item name for every cosmetic hat, including pre-existing item stacks. */
    private static Item.Properties hatProperties(String registryPath) {
        return new Item.Properties().stacksTo(1).durability(110)
                .component(DataComponents.ITEM_NAME,
                        Component.translatable("item." + MOD_ID + "." + registryPath)
                                .withStyle(ChatFormatting.GREEN));
    }

    // Collector armor: +2 armor points over corresponding netherite piece.
    // Netherite: helmet 3, chestplate 8, leggings 6, boots 3.
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> COLLECTOR_ARMOR_MATERIAL =
            ARMOR_MATERIALS.register("collector_armor", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        map.put(ArmorType.HELMET, 5);
                        map.put(ArmorType.CHESTPLATE, 10);
                        map.put(ArmorType.LEGGINGS, 8);
                        map.put(ArmorType.BOOTS, 5);
                        map.put(ArmorType.BODY, 0);
                    }), 18, SoundEvents.ARMOR_EQUIP_NETHERITE,
                    net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/collector_armor")),
                    List.of(
                            // Dark Collector armor plate, never recolored.
                            new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "collector_armor")),
                            // Accent-only armor pixels: tinted according to the vanilla smithing trim component.
                            new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "collector_armor"), "_accent", true)
                    ),
                    5.0F, 0.1F));
    // Equipped Collector helmet uses the actual imported geometry rather than the flat vanilla helmet shell.
    // Same attributes as the ordinary Collector armor. The separate material only changes 3D UV layers.
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> COLLECTOR_HELMET_GEO_MATERIAL =
            ARMOR_MATERIALS.register("collector_helmet_geo", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                        map.put(ArmorType.HELMET, 5);
                    }), 18, SoundEvents.ARMOR_EQUIP_NETHERITE,
                    net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/collector_helmet_geo")),
                    List.of(new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "collector_helmet_geo")),
                            new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "collector_helmet_geo"), "_accent", true)),
                    5.0F, 0.1F));


    // Rare wearable hats traded by wandering merchants.
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> SAKURA_THINKER_MATERIAL =
            ARMOR_MATERIALS.register("sakura_thinker", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 12, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/sakura_thinker")),
                    List.of(new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "sakura_thinker"))),
                    0F, 0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> SAKURA_THINKER =
            ITEMS.register("sakura_thinker", () -> new CosmeticArmorItem(SAKURA_THINKER_MATERIAL.get(), ArmorType.HELMET,
                    new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE)
                            .durability(ArmorType.HELMET.getDurability(8))
                            .component(DataComponents.ITEM_NAME,
                                    Component.translatable("item.mdkcosmetic.sakura_thinker")
                                            .withStyle(ChatFormatting.AQUA))));

    public static final RegistryEntry<Item, SpawnEggItem> COLLECTOR_SOUL_SPAWN_EGG =
            ITEMS.register("collector_soul_spawn_egg", () -> new SpawnEggItem(new Item.Properties().spawnEgg(COLLECTOR_SOUL_ENTITY.get())));
    public static final RegistryEntry<Item, SpawnEggItem> UNO_SPAWN_EGG =
            ITEMS.register("uno_spawn_egg", () -> new SpawnEggItem(new Item.Properties().spawnEgg(UNO_ENTITY.get())));

    public static final RegistryEntry<Item, CosmeticArmorItem> COLLECTOR_HELMET =
            ITEMS.register("collector_helmet", () -> new CosmeticArmorItem(
                    COLLECTOR_HELMET_GEO_MATERIAL.get(), ArmorType.HELMET,
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).durability(ArmorType.HELMET.getDurability(40))));
    public static final RegistryEntry<Item, CosmeticArmorItem> COLLECTOR_CHESTPLATE =
            ITEMS.register("collector_chestplate", () -> new CosmeticArmorItem(
                    COLLECTOR_ARMOR_MATERIAL.get(), ArmorType.CHESTPLATE,
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).durability(ArmorType.CHESTPLATE.getDurability(40))));
    public static final RegistryEntry<Item, CosmeticArmorItem> COLLECTOR_LEGGINGS =
            ITEMS.register("collector_leggings", () -> new CosmeticArmorItem(
                    COLLECTOR_ARMOR_MATERIAL.get(), ArmorType.LEGGINGS,
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).durability(ArmorType.LEGGINGS.getDurability(40))));
    public static final RegistryEntry<Item, CosmeticArmorItem> COLLECTOR_BOOTS =
            ITEMS.register("collector_boots", () -> new CosmeticArmorItem(
                    COLLECTOR_ARMOR_MATERIAL.get(), ArmorType.BOOTS,
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC).durability(ArmorType.BOOTS.getDurability(40))));

    /** Netherite mining speed = 9; collector multitool = 11 (+2). */
    // Minecraft 1.21.1: implement Tier directly rather than depending on SimpleTier.
    // The netherite incorrect-block tag retains full netherite mining capability.
    public static final ToolMaterial COLLECTOR_TOOL_TIER = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,2600,13.5F,6.0F,20,net.minecraft.tags.ItemTags.REPAIRS_NETHERITE_ARMOR);
    public static final RegistryEntry<Item, CollectorSwordItem> COLLECTOR_SWORD =
            ITEMS.register("collector_sword", () -> new CollectorSwordItem(COLLECTOR_TOOL_TIER,
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)
                        ));
    public static final RegistryEntry<Item, CollectorMultitoolItem> COLLECTOR_PICKAXE =
            ITEMS.register("collector_pickaxe", () -> new CollectorMultitoolItem(
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)
                        .durability(COLLECTOR_TOOL_TIER.durability())
                        .component(net.minecraft.core.component.DataComponents.TOOL, CollectorMultitoolItem.createToolComponent())
                        .attributes(net.minecraft.world.item.component.ItemAttributeModifiers.builder().add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,new net.minecraft.world.entity.ai.attributes.AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,7,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND).add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,new net.minecraft.world.entity.ai.attributes.AttributeModifier(Item.BASE_ATTACK_SPEED_ID,-2.8,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND).build())));
    public static final RegistryEntry<Item, ProductivityOrbItem> PRODUCTIVITY_ORB =
            ITEMS.register("productivity_orb", () -> new ProductivityOrbItem(new Item.Properties()
                    .stacksTo(16).rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final RegistryEntry<Block, Block> COLLECTOR_ORE_STONE =
            BLOCKS.register("collector_ore_stone", () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).strength(7F,15F).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryEntry<Block, Block> COLLECTOR_ORE_DEEPSLATE =
            BLOCKS.register("collector_ore_deepslate", () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE).strength(9F,15F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));
    public static final RegistryEntry<Block, Block> COLLECTOR_ORE_NETHERRACK =
            BLOCKS.register("collector_ore_netherrack", () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NETHER).strength(7F,15F).requiresCorrectToolForDrops().sound(SoundType.NETHER_ORE)));
    public static final RegistryEntry<Block, Block> COLLECTOR_ORE_END_STONE =
            BLOCKS.register("collector_ore_end_stone", () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND).strength(8F,15F).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final RegistryEntry<Item, BlockItem> COLLECTOR_ORE_STONE_ITEM = ITEMS.register("collector_ore_stone",
            () -> new BlockItem(COLLECTOR_ORE_STONE.get(),new Item.Properties()));
    public static final RegistryEntry<Item, BlockItem> COLLECTOR_ORE_DEEPSLATE_ITEM = ITEMS.register("collector_ore_deepslate",
            () -> new BlockItem(COLLECTOR_ORE_DEEPSLATE.get(),new Item.Properties()));
    public static final RegistryEntry<Item, BlockItem> COLLECTOR_ORE_NETHERRACK_ITEM = ITEMS.register("collector_ore_netherrack",
            () -> new BlockItem(COLLECTOR_ORE_NETHERRACK.get(),new Item.Properties()));
    public static final RegistryEntry<Item, BlockItem> COLLECTOR_ORE_END_STONE_ITEM = ITEMS.register("collector_ore_end_stone",
            () -> new BlockItem(COLLECTOR_ORE_END_STONE.get(),new Item.Properties()));

    public static final RegistryEntry<Item, GreedOrbItem> GREED_ORB =
            ITEMS.register("greed_orb", () -> new GreedOrbItem(new Item.Properties()
                    .stacksTo(16).rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> FOX_MATERIAL =
            ARMOR_MATERIALS.register("fox_hat", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/fox_hat")),
                    List.of(new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "fox_hat"))),
                    0.0F, 0.0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> FOX_HAT =
            ITEMS.register("fox_hat", () -> new CosmeticArmorItem(FOX_MATERIAL.get(), ArmorType.HELMET,
                    hatProperties("fox_hat")));
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> PANDA_MATERIAL =
            ARMOR_MATERIALS.register("panda_hat", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/panda_hat")),
                    List.of(new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "panda_hat"))),
                    0.0F, 0.0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> PANDA_HAT =
            ITEMS.register("panda_hat", () -> new CosmeticArmorItem(PANDA_MATERIAL.get(), ArmorType.HELMET,
                    hatProperties("panda_hat")));
    public static final RegistryEntry<Item, Item> PANDA_FUR =
            ITEMS.register("panda_fur", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> CAT_MATERIAL =
            ARMOR_MATERIALS.register("cat_ears", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/cat_ears")),
                    List.of(
                            new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "cat_ears"), "_dyed", true),
                            new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "cat_ears"), "_fixed", false)),
                    0.0F, 0.0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> CAT_EARS =
            ITEMS.register("cat_ears", () -> new CosmeticArmorItem(CAT_MATERIAL.get(), ArmorType.HELMET,
                    hatProperties("cat_ears")));
    public static final RegistryEntry<Item, Item> CAT_FUR =
            ITEMS.register("cat_fur", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> ALLAY_MATERIAL =
            ARMOR_MATERIALS.register("allay_hat", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/allay_hat")),
                    List.of(new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "allay_hat"))),
                    0.0F, 0.0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> ALLAY_HAT =
            ITEMS.register("allay_hat", () -> new CosmeticArmorItem(ALLAY_MATERIAL.get(), ArmorType.HELMET,
                    hatProperties("allay_hat")));
    public static final RegistryEntry<Item, Item> ALLAY_FIBERS =
            ITEMS.register("allay_fibers", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> AXOLOTL_MATERIAL =
            ARMOR_MATERIALS.register("axolotl_hat", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/axolotl_hat")),
                    List.of(
                            new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "axolotl_hat"), "_dyed", true),
                            new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "axolotl_hat"), "_fixed", false)),
                    0.0F, 0.0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> AXOLOTL_HAT =
            ITEMS.register("axolotl_hat", () -> new CosmeticArmorItem(AXOLOTL_MATERIAL.get(), ArmorType.HELMET,
                    hatProperties("axolotl_hat")));
    public static final RegistryEntry<Item, Item> AXOLOTL_SHED =
            ITEMS.register("axolotl_shed", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> RABBIT_MATERIAL =
            ARMOR_MATERIALS.register("rabbit_ears", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/rabbit_ears")),
                    List.of(
                            new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "rabbit_ears"), "_dyed", true),
                            new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "rabbit_ears"), "_fixed", false)),
                    0.0F, 0.0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> RABBIT_EARS =
            ITEMS.register("rabbit_ears", () -> new CosmeticArmorItem(RABBIT_MATERIAL.get(), ArmorType.HELMET,
                    hatProperties("rabbit_ears")));
    public static final RegistryEntry<Item, Item> RABBIT_FUR =
            ITEMS.register("rabbit_fur", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> OCTOPUS_MATERIAL =
            ARMOR_MATERIALS.register("octopus_hat", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/octopus_hat")),
                    List.of(new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "octopus_hat"))),
                    0.0F, 0.0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> OCTOPUS_HAT =
            ITEMS.register("octopus_hat", () -> new CosmeticArmorItem(OCTOPUS_MATERIAL.get(), ArmorType.HELMET,
                    hatProperties("octopus_hat")));
    public static final RegistryEntry<Item, Item> SQUID_INK_CLOT =
            ITEMS.register("squid_ink_clot", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> GLOW_SQUID_MATERIAL =
            ARMOR_MATERIALS.register("glow_squid_hat", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/glow_squid_hat")),
                    List.of(new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "glow_squid_hat"))),
                    0.0F, 0.0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> GLOW_SQUID_HAT =
            ITEMS.register("glow_squid_hat", () -> new CosmeticArmorItem(GLOW_SQUID_MATERIAL.get(), ArmorType.HELMET,
                    hatProperties("glow_squid_hat")));
    public static final RegistryEntry<Item, Item> GLOW_SQUID_INK_CLOT =
            ITEMS.register("glow_squid_ink_clot", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> CAMEL_MATERIAL =
            ARMOR_MATERIALS.register("camel_hat", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/camel_hat")),
                    List.of(new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "camel_hat"))),
                    0.0F, 0.0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> CAMEL_HAT =
            ITEMS.register("camel_hat", () -> new CosmeticArmorItem(CAMEL_MATERIAL.get(), ArmorType.HELMET,
                    hatProperties("camel_hat")));
    public static final RegistryEntry<Item, Item> CAMEL_FUR =
            ITEMS.register("camel_fur", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> COW_MATERIAL =
            ARMOR_MATERIALS.register("cow_hat", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/cow_hat")),
                    List.of(new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "cow_hat"))),
                    0.0F, 0.0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> COW_HAT =
            ITEMS.register("cow_hat", () -> new CosmeticArmorItem(COW_MATERIAL.get(), ArmorType.HELMET,
                    hatProperties("cow_hat")));
    public static final RegistryEntry<Item, Item> COW_FUR =
            ITEMS.register("cow_fur", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> MOOSHROOM_MATERIAL =
            ARMOR_MATERIALS.register("mooshroom_hat", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/mooshroom_hat")),
                    List.of(new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "mooshroom_hat"))),
                    0.0F, 0.0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> MOOSHROOM_HAT =
            ITEMS.register("mooshroom_hat", () -> new CosmeticArmorItem(MOOSHROOM_MATERIAL.get(), ArmorType.HELMET,
                    hatProperties("mooshroom_hat")));
    public static final RegistryEntry<Item, Item> MOOSHROOM_FUR =
            ITEMS.register("mooshroom_fur", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> POLAR_BEAR_MATERIAL =
            ARMOR_MATERIALS.register("polar_bear_hat", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/polar_bear_hat")),
                    List.of(new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "polar_bear_hat"))),
                    0.0F, 0.0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> POLAR_BEAR_HAT =
            ITEMS.register("polar_bear_hat", () -> new CosmeticArmorItem(POLAR_BEAR_MATERIAL.get(), ArmorType.HELMET,
                    hatProperties("polar_bear_hat")));
    public static final RegistryEntry<Item, Item> POLAR_BEAR_FUR =
            ITEMS.register("polar_bear_fur", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> LLAMA_MATERIAL =
            ARMOR_MATERIALS.register("llama_hat", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/llama_hat")),
                    List.of(new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "llama_hat"))),
                    0.0F, 0.0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> LLAMA_HAT =
            ITEMS.register("llama_hat", () -> new CosmeticArmorItem(LLAMA_MATERIAL.get(), ArmorType.HELMET,
                    hatProperties("llama_hat")));
    public static final RegistryEntry<Item, Item> LLAMA_FUR =
            ITEMS.register("llama_fur", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> SHEEP_MATERIAL =
            ARMOR_MATERIALS.register("sheep_hat", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/sheep_hat")),
                    List.of(
                            new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "sheep_hat"), "_dyed", true),
                            new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "sheep_hat"), "_fixed", false)),
                    0.0F, 0.0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> SHEEP_HAT =
            ITEMS.register("sheep_hat", () -> new CosmeticArmorItem(SHEEP_MATERIAL.get(), ArmorType.HELMET,
                    hatProperties("sheep_hat")));
    // Legacy item ID retained for old saves; no longer obtainable, traded or shown in Creative.
    public static final RegistryEntry<Item, Item> SHEEP_FLEECE =
            ITEMS.register("sheep_fleece", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> WOLF_MATERIAL =
            ARMOR_MATERIALS.register("wolf_hat", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/wolf_hat")),
                    List.of(new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "wolf_hat"), "_dyed", true),
                            new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "wolf_hat"), "_fixed", false)),
                    0.0F, 0.0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> WOLF_HAT =
            ITEMS.register("wolf_hat", () -> new CosmeticArmorItem(WOLF_MATERIAL.get(), ArmorType.HELMET,
                    hatProperties("wolf_hat")));
    public static final RegistryEntry<Item, Item> WOLF_FUR =
            ITEMS.register("wolf_fur", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> HORSE_MATERIAL =
            ARMOR_MATERIALS.register("horse_hat", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/horse_hat")),
                    List.of(
                        new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "horse_hat"), "_dyed", true),
                        new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "horse_hat"), "_fixed", false)
                    ), 0.0F, 0.0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> HORSE_HAT =
            ITEMS.register("horse_hat", () -> new CosmeticArmorItem(HORSE_MATERIAL.get(), ArmorType.HELMET,
                    hatProperties("horse_hat")));
    public static final RegistryEntry<Item, Item> HORSE_FUR =
            ITEMS.register("horse_fur", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<ArmorDefinition, ArmorDefinition> DONKEY_MATERIAL =
            ARMOR_MATERIALS.register("donkey_hat", () -> new ArmorDefinition(
                    Util.make(new EnumMap<>(ArmorType.class), map -> {
                        for (ArmorType type : ArmorType.values()) map.put(type, 0);
                    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.tags.TagKey.create(Registries.ITEM,ResourceLocation.fromNamespaceAndPath(MOD_ID,"repairs/donkey_hat")),
                    List.of(
                        new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "donkey_hat"), "_dyed", true),
                        new ArmorDefinition.Layer(ResourceLocation.fromNamespaceAndPath(MOD_ID, "donkey_hat"), "_fixed", false)
                    ), 0.0F, 0.0F));
    public static final RegistryEntry<Item, CosmeticArmorItem> DONKEY_HAT =
            ITEMS.register("donkey_hat", () -> new CosmeticArmorItem(DONKEY_MATERIAL.get(), ArmorType.HELMET,
                    hatProperties("donkey_hat")));
    public static final RegistryEntry<Item, Item> DONKEY_FUR =
            ITEMS.register("donkey_fur", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<Item, Item> FOX_FUR =
            ITEMS.register("fox_fur", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<Block, TailoringTableBlock> TAILORING_TABLE =
            BLOCKS.register("tailoring_table", () -> new TailoringTableBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F)
                            .requiresCorrectToolForDrops().lightLevel(state -> 6).noOcclusion()));
    public static final RegistryEntry<Item, BlockItem> TAILORING_TABLE_ITEM =
            ITEMS.register("tailoring_table", () -> new BlockItem(TAILORING_TABLE.get(), new Item.Properties()));

    public static final RegistryEntry<Item, Item> POWER_ESSENCE =
            ITEMS.register("power_essence", () -> new Item(new Item.Properties().stacksTo(64)
                    .component(DataComponents.ITEM_NAME, Component.translatable("item.mdkcosmetic.power_essence")
                            .withStyle(ChatFormatting.AQUA))));
    public static final RegistryEntry<Item, Item> POWER_CONCENTRATE =
            ITEMS.register("power_concentrate", () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryEntry<Item, Item> CHARGED_POWER_ESSENCE =
            ITEMS.register("charged_power_essence", () -> new Item(new Item.Properties().stacksTo(64)
                    .rarity(net.minecraft.world.item.Rarity.UNCOMMON)
                    .component(DataComponents.ITEM_NAME, Component.translatable("item.mdkcosmetic.charged_power_essence")
                            .withStyle(ChatFormatting.LIGHT_PURPLE))));
    public static final RegistryEntry<Block, EssenceWorktableBlock> ESSENCE_WORKTABLE =
            BLOCKS.register("essence_worktable", () -> new EssenceWorktableBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(3.0F)
                            .requiresCorrectToolForDrops().lightLevel(state -> 8).noOcclusion()));
    public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<EssenceWorktableBlockEntity>> ESSENCE_WORKTABLE_BE =
            BLOCK_ENTITIES.register("essence_worktable", () ->
                    new BlockEntityType<>(EssenceWorktableBlockEntity::new, java.util.Set.of(ESSENCE_WORKTABLE.get())));
    public static final RegistryEntry<MenuType<?>, MenuType<EssenceWorktableMenu>> ESSENCE_WORKTABLE_MENU =
            MENUS.register("essence_worktable", () -> new MenuType<>(EssenceWorktableMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryEntry<Item, BlockItem> ESSENCE_WORKTABLE_ITEM =
            ITEMS.register("essence_worktable", () -> new BlockItem(ESSENCE_WORKTABLE.get(), new Item.Properties()));


    // Collection of Mysteries multiblock (3x3x3)
    public static final RegistryEntry<Item, Item> COLLECTOR_SOUL =
            ITEMS.register("collector_soul", () -> new Item(new Item.Properties().stacksTo(64)
                    .rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryEntry<Block, CollectorCasingBlock> COLLECTOR_CASING =
            BLOCKS.register("collector_casing", () -> new CollectorCasingBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                            .strength(3.5F, 8F).requiresCorrectToolForDrops()
                            .sound(net.minecraft.world.level.block.SoundType.METAL)));
    public static final RegistryEntry<Item, BlockItem> COLLECTOR_CASING_ITEM =
            ITEMS.register("collector_casing", () -> new BlockItem(COLLECTOR_CASING.get(), new Item.Properties()));
    public static final RegistryEntry<Block, CollectorSoulStorageBlock> COLLECTOR_SOUL_STORAGE =
            BLOCKS.register("collector_soul_storage", () -> new CollectorSoulStorageBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                            .strength(3.5F, 8F).requiresCorrectToolForDrops()
                            .sound(net.minecraft.world.level.block.SoundType.AMETHYST)
                            .lightLevel(state -> 6)));
    public static final RegistryEntry<Item, CollectorStorageItem> COLLECTOR_SOUL_STORAGE_ITEM =
            ITEMS.register("collector_soul_storage", () -> new CollectorStorageItem(COLLECTOR_SOUL_STORAGE.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryEntry<Block, CollectorLinkBlock> COLLECTOR_LINK =
            BLOCKS.register("collector_link", () -> new CollectorLinkBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                            .strength(4.0F, 9F).requiresCorrectToolForDrops()
                            .sound(net.minecraft.world.level.block.SoundType.METAL)
                            .lightLevel(state -> state.getValue(CollectorLinkBlock.ACTIVE) ? 10 : 2)));
    public static final RegistryEntry<Item, BlockItem> COLLECTOR_LINK_ITEM =
            ITEMS.register("collector_link", () -> new BlockItem(COLLECTOR_LINK.get(), new Item.Properties()));
    public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<CollectorLinkBlockEntity>> COLLECTOR_LINK_BE =
            BLOCK_ENTITIES.register("collector_link", () -> new BlockEntityType<>(
                    CollectorLinkBlockEntity::new, java.util.Set.of(COLLECTOR_LINK.get())));

    public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<CollectorSoulStorageBlockEntity>> COLLECTOR_SOUL_STORAGE_BE =
            BLOCK_ENTITIES.register("collector_soul_storage", () -> new BlockEntityType<>(
                    CollectorSoulStorageBlockEntity::new, java.util.Set.of(COLLECTOR_SOUL_STORAGE.get())));

    public static final RegistryEntry<MenuType<?>, MenuType<CollectorLinkMenu>> COLLECTOR_LINK_MENU =
            MENUS.register("collector_link_menu", () -> new MenuType<>(CollectorLinkMenu::new, FeatureFlags.DEFAULT_FLAGS));

    // Second-stage Collector Memory, built around the original 3x3x3 multiblock.
    // Portable Collector backpacks: 27 / 45 slots, configurable filters, magnet and memory binding.
    public static final RegistryEntry<Item, CollectorBackpackItem> COLLECTOR_BACKPACK_BASIC =
            ITEMS.register("collector_backpack_basic", () -> new CollectorBackpackItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryEntry<Item, CollectorBackpackItem> COLLECTOR_BACKPACK_REINFORCED =
            ITEMS.register("collector_backpack_reinforced", () -> new CollectorBackpackItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryEntry<Item, Item> COLLECTOR_BACKPACK_FILTER =
            ITEMS.register("collector_backpack_filter", () -> new Item(new Item.Properties()));
    public static final RegistryEntry<Item, Item> COLLECTOR_BACKPACK_MAGNET =
            ITEMS.register("collector_backpack_magnet", () -> new StyledNameItem(new Item.Properties().stacksTo(1), ChatFormatting.AQUA));
    public static final RegistryEntry<Item, Item> COLLECTOR_BACKPACK_CRAFTING =
            ITEMS.register("collector_backpack_crafting", () -> new StyledNameItem(new Item.Properties().stacksTo(1), ChatFormatting.GREEN));
    public static final RegistryEntry<Item, Item> COLLECTOR_BACKPACK_FURNACE =
            ITEMS.register("collector_backpack_furnace", () -> new StyledNameItem(new Item.Properties().stacksTo(1), ChatFormatting.GREEN));
    public static final RegistryEntry<Item, Item> COLLECTOR_BACKPACK_TOTEM =
            ITEMS.register("collector_backpack_totem", () -> new StyledNameItem(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE), ChatFormatting.AQUA));
    public static final RegistryEntry<Item, Item> COLLECTOR_BACKPACK_DEATH_VAULT =
            ITEMS.register("collector_backpack_death_vault", () -> new StyledNameItem(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC), ChatFormatting.LIGHT_PURPLE));
    public static final RegistryEntry<Item, SpawnEggItem> COLLECTOR_HEART_SPAWN_EGG =
            ITEMS.register("collector_heart_spawn_egg", () -> new SpawnEggItem(new Item.Properties().spawnEgg(COLLECTOR_HEART_ENTITY.get())));
    public static final RegistryEntry<Item, Item> COLLECTOR_BACKPACK_LINK_KEY =
            ITEMS.register("collector_backpack_link_key", () -> new Item(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final RegistryEntry<MenuType<?>, MenuType<CollectorBackpackMenu>> COLLECTOR_BACKPACK_MENU =
            MENUS.register("collector_backpack", () -> new MenuType<>(CollectorBackpackMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryEntry<MenuType<?>, MenuType<CollectorBackpackMenu>> COLLECTOR_EQUIPPED_BACKPACK_MENU =
            MENUS.register("collector_equipped_backpack", () -> new MenuType<>((id, inv) ->
                    new CollectorBackpackMenu(id, inv, true), FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryEntry<MenuType<?>, MenuType<CollectorEquipmentMenu>> COLLECTOR_EQUIPMENT_MENU =
            MENUS.register("collector_equipment", () -> new MenuType<>(CollectorEquipmentMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final RegistryEntry<Item, Item> COLLECTOR_MEMORY =
            ITEMS.register("collector_memory", () -> new Item(new Item.Properties().stacksTo(1)
                    .rarity(net.minecraft.world.item.Rarity.EPIC)));
    // Final tier: meteor material from the Collector's origin, only the Collector pickaxe can mine it.
    public static final RegistryEntry<Block, CollectorMeteorBlock> COLLECTOR_METEOR_BLOCK =
            BLOCKS.register("collector_meteor_block", () -> new CollectorMeteorBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                            .strength(12.0F, 1200.0F).requiresCorrectToolForDrops()
                            .sound(SoundType.NETHERITE_BLOCK).lightLevel(state -> 8)));
    public static final RegistryEntry<Item, BlockItem> COLLECTOR_METEOR_BLOCK_ITEM =
            ITEMS.register("collector_meteor_block", () -> new BlockItem(COLLECTOR_METEOR_BLOCK.get(), new Item.Properties()));
    public static final RegistryEntry<Block, CollectorVoidPortalBlock> COLLECTOR_VOID_PORTAL =
            BLOCKS.register("collector_void_portal", () -> new CollectorVoidPortalBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                            .noCollision().noOcclusion().strength(-1.0F, 3600000.0F)
                            .lightLevel(state -> 12)));
    public static final RegistryEntry<Block, CollectorVoidReturnBlock> COLLECTOR_VOID_RETURN =
            BLOCKS.register("collector_void_return", () -> new CollectorVoidReturnBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                            .noCollision().noOcclusion().strength(-1.0F, 3600000.0F)
                            .lightLevel(state -> 15)));

    public static final RegistryEntry<Block, CollectorMemoryCasingBlock> COLLECTOR_MEMORY_CASING =
            BLOCKS.register("collector_memory_casing", () -> new CollectorMemoryCasingBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                            .strength(5.0F, 1200.0F).requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)));
    public static final RegistryEntry<Item, BlockItem> COLLECTOR_MEMORY_CASING_ITEM =
            ITEMS.register("collector_memory_casing", () -> new BlockItem(COLLECTOR_MEMORY_CASING.get(),
                    new Item.Properties()));
    public static final RegistryEntry<Block, CollectorMemoryCoreBlock> COLLECTOR_MEMORY_CORE =
            BLOCKS.register("collector_memory_core", () -> new CollectorMemoryCoreBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                            .strength(6.0F, 1200.0F).requiresCorrectToolForDrops()
                            .sound(SoundType.AMETHYST).lightLevel(state -> 9)));
    public static final RegistryEntry<Item, CollectorMemoryCoreItem> COLLECTOR_MEMORY_CORE_ITEM =
            ITEMS.register("collector_memory_core", () -> new CollectorMemoryCoreItem(COLLECTOR_MEMORY_CORE.get(),
                    new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<CollectorMemoryCoreBlockEntity>> COLLECTOR_MEMORY_CORE_BE =
            BLOCK_ENTITIES.register("collector_memory_core", () -> new BlockEntityType<>(
                    CollectorMemoryCoreBlockEntity::new, java.util.Set.of(COLLECTOR_MEMORY_CORE.get())));
    public static final RegistryEntry<MenuType<?>, MenuType<CollectorMemoryMenu>> COLLECTOR_MEMORY_MENU =
            MENUS.register("collector_memory", () -> new MenuType<>(CollectorMemoryMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryEntry<MenuType<?>, MenuType<CollectorSmelterMenu>> COLLECTOR_SMELTER_MENU =
            MENUS.register("collector_smelter", () -> new MenuType<>(CollectorSmelterMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> COSMETIC_TAB =
            CREATIVE_TABS.register("mdk_cosmetic", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mdkcosmetic.mdk_cosmetic")).withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> new ItemStack(TAILORING_TABLE_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        // 1. Decorative worktables
                        output.accept(TAILORING_TABLE_ITEM.get());
                        output.accept(ESSENCE_WORKTABLE_ITEM.get());

                        // 2. Flat materials and processed resources
                        output.accept(FOX_FUR.get());
                        output.accept(PANDA_FUR.get());
                        output.accept(CAT_FUR.get());
                        output.accept(ALLAY_FIBERS.get());
                        output.accept(AXOLOTL_SHED.get());
                        output.accept(RABBIT_FUR.get());
                        output.accept(SQUID_INK_CLOT.get());
                        output.accept(GLOW_SQUID_INK_CLOT.get());
                        output.accept(CAMEL_FUR.get());
                        output.accept(COW_FUR.get());
                        output.accept(MOOSHROOM_FUR.get());
                        output.accept(POLAR_BEAR_FUR.get());
                        output.accept(LLAMA_FUR.get());
                        output.accept(WOLF_FUR.get());
                        output.accept(HORSE_FUR.get());
                        output.accept(DONKEY_FUR.get());
                        output.accept(POWER_ESSENCE.get());
                        // Legacy concentrate remains registered to preserve old saves, but is no longer produced.
                        output.accept(CHARGED_POWER_ESSENCE.get());
                        output.accept(COLLECTOR_SOUL.get());
                        output.accept(COLLECTOR_CASING_ITEM.get());
                        output.accept(COLLECTOR_SOUL_STORAGE_ITEM.get());
                        output.accept(COLLECTOR_LINK_ITEM.get());
                        output.accept(COLLECTOR_MEMORY.get());
                        output.accept(COLLECTOR_MEMORY_CASING_ITEM.get());
                        output.accept(COLLECTOR_METEOR_BLOCK_ITEM.get());
                        output.accept(COLLECTOR_MEMORY_CORE_ITEM.get());
                        output.accept(COLLECTOR_BACKPACK_BASIC.get());
                        output.accept(COLLECTOR_BACKPACK_REINFORCED.get());
                        // Legacy filter remains registered for save compatibility, not offered to new players.
                        output.accept(COLLECTOR_BACKPACK_CRAFTING.get());
                        output.accept(COLLECTOR_BACKPACK_FURNACE.get());
                        output.accept(COLLECTOR_BACKPACK_TOTEM.get());
                        output.accept(COLLECTOR_BACKPACK_DEATH_VAULT.get());
                        output.accept(COLLECTOR_HEART_SPAWN_EGG.get());
                        output.accept(COLLECTOR_BACKPACK_MAGNET.get());
                        output.accept(COLLECTOR_BACKPACK_LINK_KEY.get());

                        // 3. Wearable 3D headgear and future equipment
                        output.accept(FOX_HAT.get());
                        output.accept(PANDA_HAT.get());
                        output.accept(CAT_EARS.get());
                        output.accept(ALLAY_HAT.get());
                        output.accept(AXOLOTL_HAT.get());
                        output.accept(RABBIT_EARS.get());
                        output.accept(OCTOPUS_HAT.get());
                        output.accept(GLOW_SQUID_HAT.get());
                        output.accept(CAMEL_HAT.get());
                        output.accept(COW_HAT.get());
                        output.accept(MOOSHROOM_HAT.get());
                        output.accept(POLAR_BEAR_HAT.get());
                        output.accept(LLAMA_HAT.get());
                        output.accept(SHEEP_HAT.get());
                        output.accept(WOLF_HAT.get());
                        output.accept(HORSE_HAT.get());
                        output.accept(DONKEY_HAT.get());
                        output.accept(SAKURA_THINKER.get());
                        output.accept(COLLECTOR_HELMET.get());
                        output.accept(COLLECTOR_CHESTPLATE.get());
                        output.accept(COLLECTOR_LEGGINGS.get());
                        output.accept(COLLECTOR_BOOTS.get());
                        output.accept(GREED_ORB.get());
                        output.accept(PRODUCTIVITY_ORB.get());
                        output.accept(COLLECTOR_SWORD.get());
                        output.accept(COLLECTOR_PICKAXE.get());
                        output.accept(COLLECTOR_SOUL_SPAWN_EGG.get());
                        output.accept(UNO_SPAWN_EGG.get());

                        // 4. Treasure-only enchantment is still available for creative testing.
                        parameters.holders().lookup(Registries.ENCHANTMENT).ifPresent(registry ->
                                registry.get(ResourceKey.create(Registries.ENCHANTMENT,
                                        ResourceLocation.fromNamespaceAndPath(MOD_ID, "collector")))
                                        .ifPresent(holder -> output.accept(
                                                EnchantedBooks.createForEnchantment(
                                                        new EnchantmentInstance(holder, 1)))));
                    })
                    .build());

    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> FUR_BLOCKS_TAB =
            CREATIVE_TABS.register("mdk_fur_blocks", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mdkcosmetic.mdk_fur_blocks")).withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> new ItemStack(FurDecorBlocks.firstIcon()))
                    .displayItems((parameters, output) -> { FurDecorBlocks.fillCreative(output); MysteryBlocks.CREATIVE_ITEMS.forEach(v -> output.accept(v.get()));
                        output.accept(COLLECTOR_ORE_STONE_ITEM.get());
                        output.accept(COLLECTOR_ORE_DEEPSLATE_ITEM.get());
                        output.accept(COLLECTOR_ORE_NETHERRACK_ITEM.get());
                        output.accept(COLLECTOR_ORE_END_STONE_ITEM.get()); })
                    .build());

    public MDKCosmetic(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext context) {
        BusGroup modBus=context.getModBusGroup();
        com.mdk.cosmetic.platform.ForgeChestLootModifier.register(modBus);
        if(net.minecraftforge.fml.loading.FMLEnvironment.dist==net.minecraftforge.api.distmarker.Dist.CLIENT)new com.mdk.cosmetic.client.FoxHatClient(modBus);
        FurDecorBlocks.register(BLOCKS, ITEMS);
        MysteryBlocks.register(BLOCKS, ITEMS);
        CollectorDialoguePayload.register(new com.mdk.cosmetic.platform.ForgePayloads.Registration());
        CollectorMemoryQueryPayload.register(new com.mdk.cosmetic.platform.ForgePayloads.Registration());
        CollectorBackpackQueryPayload.register(new com.mdk.cosmetic.platform.ForgePayloads.Registration());
        CollectorEquipmentActionPayload.register(new com.mdk.cosmetic.platform.ForgePayloads.Registration());
        CollectorEquipmentVisualPayload.register(new com.mdk.cosmetic.platform.ForgePayloads.Registration());
        UnoEncounterPayload.register(new com.mdk.cosmetic.platform.ForgePayloads.Registration());
        STRUCTURE_TYPES.register(modBus);
        STRUCTURE_PIECES.register(modBus);
        ENTITY_TYPES.register(modBus);
        net.minecraftforge.event.entity.EntityAttributeCreationEvent.BUS.addListener((EntityAttributeCreationEvent event)-> {
                event.put(COLLECTOR_SOUL_ENTITY.get(), CollectorSoulEntity.attributes().build());
                event.put(UNO_ENTITY.get(), UnoEntity.attributes().build());
                event.put(COLLECTOR_HEART_ENTITY.get(), CollectorHeartEntity.attributes().build());
        });
        ARMOR_MATERIALS.register(modBus);
        MOB_EFFECTS.register(modBus);
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        ITEMS.register(modBus);
        CREATIVE_TABS.register(modBus);
        net.minecraftforge.event.entity.EntityJoinLevelEvent.BUS.addListener(CollectorProtection::onItemSpawn);
        com.mdk.cosmetic.platform.ForgeGameplayEvents.EntityInvulnerabilityCheckEvent.BUS.addListener(CollectorProtection::onInvulnerabilityCheck);
        net.minecraftforge.event.level.ExplosionEvent.Detonate.BUS.addListener(CollectorProtection::onExplosion);
        net.minecraftforge.event.level.BlockEvent.BreakEvent.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.level.BlockEvent.BreakEvent>)CollectorMeteorBlock::onBreak);
        net.minecraftforge.event.TickEvent.PlayerTickEvent.Post.BUS.addListener(CollectorEquipmentEffects::onTick);
        net.minecraftforge.event.entity.living.LivingFallEvent.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.living.LivingFallEvent>)CollectorEquipmentEffects::onFall);
        net.minecraftforge.event.entity.player.PlayerEvent.Clone.BUS.addListener(CollectorEquipmentEffects::onClone);
        net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent.BUS.addListener(CollectorEquipmentEffects::onLogin);
        net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent.BUS.addListener(CollectorEquipmentEffects::onRespawn);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(CollectorEquipmentEffects::onDrops);
        net.minecraftforge.event.TickEvent.PlayerTickEvent.Post.BUS.addListener(CollectorBackpackMagnet::onTick);
        net.minecraftforge.event.TickEvent.PlayerTickEvent.Post.BUS.addListener(CollectorBackpackFurnace::onTick);
        net.minecraftforge.event.entity.living.LivingDeathEvent.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.living.LivingDeathEvent>)CollectorBackpackTotem::onDeath);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(CollectorBackpackDeathVault::onDrops);
        com.mdk.cosmetic.platform.ForgeEntityTicks.EntityTickEvent.Post.BUS.addListener(CollectorHeartWorldEvents::onEntityTick);
        net.minecraftforge.event.entity.living.LivingDamageEvent.BUS.addListener(CollectorHeartWorldEvents::onHurt);
        net.minecraftforge.event.level.BlockEvent.BreakEvent.BUS.addListener(CollectorVoidAchievements::onBlockBreak);
        net.minecraftforge.event.TickEvent.ServerTickEvent.Post.BUS.addListener(CollectorSoulWorldEvents::onServerTick);
        com.mdk.cosmetic.platform.ForgeEntityTicks.EntityTickEvent.Post.BUS.addListener(UnoDragonEncounter::onEntityTick);
        net.minecraftforge.event.entity.living.LivingChangeTargetEvent.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.living.LivingChangeTargetEvent>)CollectorSoulWorldEvents::onWardenChangeTarget);
        com.mdk.cosmetic.platform.ForgeEntityTicks.EntityTickEvent.Post.BUS.addListener(CollectorSoulWorldEvents::onWardenTick);
        net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract>)FoxFurShearing::onInteract);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(FoxFurShearing::onFoxDrops);
        net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract>)PandaFurShearing::onInteract);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(PandaFurShearing::onPandaDrops);
        net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract>)CatFurShearing::onInteract);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(CatFurShearing::onCatDrops);
        net.minecraftforge.event.entity.EntityJoinLevelEvent.BUS.addListener(AnimalHatAttraction::onEntityJoin);
        net.minecraftforge.event.entity.EntityJoinLevelEvent.BUS.addListener(HostileHatSpawns::onJoin);
        net.minecraftforge.event.entity.EntityJoinLevelEvent.BUS.addListener(CollectorArmorFear::onMobJoin);
        net.minecraftforge.event.entity.living.LivingChangeTargetEvent.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.living.LivingChangeTargetEvent>)CollectorArmorFear::onTargetChange);
        com.mdk.cosmetic.platform.ForgeEntityTicks.EntityTickEvent.Post.BUS.addListener(CollectorArmorFear::onMobTick);
        // Per-player advancement counters, rare challenges and collection checks.
        net.minecraftforge.event.entity.player.PlayerEvent.ItemPickupEvent.BUS.addListener(CollectorAdvancementEvents::onPickup);
        net.minecraftforge.event.entity.living.LivingDeathEvent.BUS.addListener(CollectorAdvancementEvents::onKill);
        net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent.BUS.addListener(CollectorAdvancementEvents::onCraft);
        net.minecraftforge.event.TickEvent.PlayerTickEvent.Post.BUS.addListener(CollectorAdvancementEvents::onTick);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(CollectorEssenceDrops::onDrops);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(UnoMemoryDrops::onDrops);
        net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.BUS.addListener(CollectorWanderingTrader::onInteract);
        net.minecraftforge.event.TickEvent.PlayerTickEvent.Post.BUS.addListener(RareTraderHatEffects::onPlayerTick);
        com.mdk.cosmetic.platform.ForgeGameplayEvents.ItemAttributeModifierEvent.BUS.addListener(RareTraderHatEffects::onAttributeModifiers);
        net.minecraftforge.event.TickEvent.PlayerTickEvent.Post.BUS.addListener(CollectorSwordWave::onPlayerTick);
        net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed.BUS.addListener(CollectorOres::onBreakSpeed);
        net.minecraftforge.event.level.BlockEvent.BreakEvent.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.level.BlockEvent.BreakEvent>)CollectorOres::onBreak);
        com.mdk.cosmetic.platform.ForgeGameplayEvents.BlockDropsEvent.BUS.addListener(CollectorOres::onDrops);
        net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract>)AllayFiberHarvest::onInteract);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(AllayFiberHarvest::onDrops);
        net.minecraftforge.event.TickEvent.PlayerTickEvent.Post.BUS.addListener(AllayHatGlow::onPlayerTick);
        com.mdk.cosmetic.platform.ForgeEntityTicks.EntityTickEvent.Post.BUS.addListener(AllayHatAttraction::onAllayTick);
        net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract>)AxolotlSkinHarvest::onInteract);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(AxolotlSkinHarvest::onDrops);
        net.minecraftforge.event.TickEvent.PlayerTickEvent.Post.BUS.addListener(AxolotlHatBubbles::onPlayerTick);
        com.mdk.cosmetic.platform.ForgeEntityTicks.EntityTickEvent.Post.BUS.addListener(AxolotlHatAttraction::onAxolotlTick);
        net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract>)RabbitFurShearing::onInteract);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(RabbitFurShearing::onRabbitDrops);
        net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract>)SquidInkHarvest::onInteract);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(SquidInkHarvest::onDrops);
        com.mdk.cosmetic.platform.ForgeEntityTicks.EntityTickEvent.Post.BUS.addListener(SquidHatAttraction::onSquidTick);
        net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract>)GlowSquidInkHarvest::onInteract);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(GlowSquidInkHarvest::onDrops);
        com.mdk.cosmetic.platform.ForgeEntityTicks.EntityTickEvent.Post.BUS.addListener(GlowSquidHatAttraction::onGlowSquidTick);
        net.minecraftforge.event.TickEvent.PlayerTickEvent.Post.BUS.addListener(GlowSquidHatGlow::onPlayerTick);
        net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract>)WolfFurShearing::onInteract);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(WolfFurShearing::onWolfDrops);
        net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract>)HorseFurShearing::onInteract);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(HorseFurShearing::onDrops);
        net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract>)DonkeyFurShearing::onInteract);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(DonkeyFurShearing::onDrops);
        net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract>)CamelFurShearing::onInteract);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(CamelFurShearing::onCamelDrops);
        com.mdk.cosmetic.platform.ForgeEntityTicks.EntityTickEvent.Post.BUS.addListener(CamelHatAttraction::onCamelTick);
        net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract>)CowFurShearing::onInteract);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(CowFurShearing::onCowDrops);
        net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract>)MooshroomFurShearing::onInteract);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(MooshroomFurShearing::onDrops);
        net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract>)PolarBearFurShearing::onInteract);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(PolarBearFurShearing::onDrops);
        net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.BUS.addListener((java.util.function.Predicate<net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract>)LlamaFurShearing::onInteract);
        net.minecraftforge.event.entity.living.LivingDropsEvent.BUS.addListener(LlamaFurShearing::onDrops);
    }

}
