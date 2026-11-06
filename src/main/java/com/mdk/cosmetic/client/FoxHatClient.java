package com.mdk.cosmetic.client;

import com.mdk.cosmetic.MDKCosmetic;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import com.mdk.cosmetic.platform.ArmorDefinition;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

// Client-only entrypoint: dedicated servers never load model classes.
@Mod(value = MDKCosmetic.MOD_ID, dist = Dist.CLIENT)
public final class FoxHatClient {
    public FoxHatClient(IEventBus modBus) {
        modBus.addListener((RegisterClientExtensionsEvent event)->registerClientExtensions(new NativeClientExtensions(event)));
        modBus.addListener((RegisterColorHandlersEvent.ItemTintSources event)->{
            event.register(net.minecraft.resources.ResourceLocation.parse("mdkcosmetic:cosmetic_color"),CosmeticItemTints.Source.CODEC);
            registerItemColors(new ItemColorRegistration());
        });
        modBus.addListener(FoxHatClient::registerScreens);
        modBus.addListener(CollectorEquipmentKeys::register);
        NeoForge.EVENT_BUS.addListener(CollectorEquipmentKeys::onTick);
        modBus.addListener(FoxHatClient::registerPlayerHelmetLayers);
        NeoForge.EVENT_BUS.addListener(CosmeticHatTooltip::onTooltip);
        NeoForge.EVENT_BUS.addListener(CollectorDialogueOverlay::onRenderGui);
        NeoForge.EVENT_BUS.addListener(GreedVignetteOverlay::onRenderGui);
        NeoForge.EVENT_BUS.addListener(UnoEncounterOverlay::onRenderGui);
        NeoForge.EVENT_BUS.addListener(UnoEncounterOverlay::onCameraAngles);
    }

    private static void registerPlayerHelmetLayers(net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers event) {
        for (net.minecraft.client.resources.PlayerSkin.Model skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer renderer) {
                renderer.addLayer(new CollectorHelmetHeadLayer<>(renderer));
                renderer.addLayer(new CollectorBackpackLayer(renderer));
            }
        }
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MDKCosmetic.ESSENCE_WORKTABLE_MENU.get(), EssenceWorktableScreen::new);
        event.register(MDKCosmetic.COLLECTOR_LINK_MENU.get(), CollectorLinkScreen::new);
        event.register(MDKCosmetic.COLLECTOR_MEMORY_MENU.get(), CollectorMemoryScreen::new);
        event.register(MDKCosmetic.COLLECTOR_BACKPACK_MENU.get(), CollectorBackpackScreen::new);
        event.register(MDKCosmetic.COLLECTOR_EQUIPPED_BACKPACK_MENU.get(), CollectorBackpackScreen::new);
        event.register(MDKCosmetic.COLLECTOR_EQUIPMENT_MENU.get(), CollectorEquipmentScreen::new);
        event.register(MDKCosmetic.COLLECTOR_SMELTER_MENU.get(), CollectorSmelterScreen::new);
    }

    private static void registerItemColors(ItemColorRegistration event) {
        // Generated armor item icons tint layer 1; the Collector 3D helmet tints only its secondary overlay (index 0).
        event.register((stack, tintIndex) -> tintIndex == 0
                ? CollectorTrimColors.color(stack) : 0xFFFFFFFF, MDKCosmetic.COLLECTOR_HELMET.get());
        event.register((stack, tintIndex) -> tintIndex == 1
                ? CollectorTrimColors.color(stack) : 0xFFFFFFFF,
                MDKCosmetic.COLLECTOR_CHESTPLATE.get(), MDKCosmetic.COLLECTOR_LEGGINGS.get(),
                MDKCosmetic.COLLECTOR_BOOTS.get());
        event.register((stack, tintIndex) -> tintIndex == 0
                ? (0xFF000000 | DyedItemColor.getOrDefault(stack, 0x303038))
                : 0xFFFFFFFF, MDKCosmetic.CAT_EARS.get());
        event.register((stack, tintIndex) -> tintIndex == 0
                ? (0xFF000000 | DyedItemColor.getOrDefault(stack, 0xF4B5D2))
                : 0xFFFFFFFF, MDKCosmetic.AXOLOTL_HAT.get());
        event.register((stack, tintIndex) -> tintIndex == 0
                ? (0xFF000000 | DyedItemColor.getOrDefault(stack, 0xF1EBE1))
                : 0xFFFFFFFF, MDKCosmetic.RABBIT_EARS.get());
        event.register((stack, tintIndex) -> tintIndex == 0
                ? (0xFF000000 | DyedItemColor.getOrDefault(stack, 0xF1F1F1))
                : 0xFFFFFFFF, MDKCosmetic.SHEEP_HAT.get());
        event.register((stack, tintIndex) -> tintIndex == 0
                ? (0xFF000000 | DyedItemColor.getOrDefault(stack, 0xB4B4B4))
                : 0xFFFFFFFF, MDKCosmetic.WOLF_HAT.get());
        event.register((stack, tintIndex) -> tintIndex == 0
                ? (0xFF000000 | DyedItemColor.getOrDefault(stack, 0xFFF4EEE5))
                : 0xFFFFFFFF, MDKCosmetic.HORSE_HAT.get());
        event.register((stack, tintIndex) -> tintIndex == 0
                ? (0xFF000000 | DyedItemColor.getOrDefault(stack, 0xFFE5D6B9))
                : 0xFFFFFFFF, MDKCosmetic.DONKEY_HAT.get());
        event.register((stack, tintIndex) -> tintIndex == 1 ? 0xFFF2E8FF : 0xFFFFFFFF,
                MDKCosmetic.COLLECTOR_BACKPACK_CRAFTING.get(),
                MDKCosmetic.COLLECTOR_BACKPACK_FURNACE.get(),
                MDKCosmetic.COLLECTOR_BACKPACK_TOTEM.get());
        event.register((stack, tintIndex) -> tintIndex == 1 ? 0xFFDDF4FF : 0xFFFFFFFF,
                MDKCosmetic.COLLECTOR_BACKPACK_MAGNET.get());
    }

    private static void registerClientExtensions(NativeClientExtensions event) {
        // The Collector armor uses vanilla humanoid UV layers (128x64).
        // IMPORTANT: do not substitute the previous 256x256 cubic armor model here.
        // NeoForge renders the armor layer textures on the original humanoid model.
        // Keep standard humanoid armor geometry; tint only the second armor material layer.
        final SakuraThinkerModel sakuraThinkerModel = new SakuraThinkerModel();
        final ArmorAppearance collectorArmorExtensions = new ArmorAppearance() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                // The exact 3D item model is now rendered by CollectorHelmetHeadLayer.
                // Its old 74-part baked armor conversion is intentionally not used.
                return original;
            }

            @Override
            public int getDefaultDyeColor(ItemStack stack) {
                return CollectorTrimColors.color(stack);
            }

            @Override
            public int getArmorLayerTintColor(ItemStack stack, net.minecraft.client.renderer.entity.state.HumanoidRenderState entity,
                    ArmorDefinition.Layer layer, int layerIdx, int fallbackColor) {
                if (stack.is(MDKCosmetic.COLLECTOR_HELMET.get())) return 0;
                return layer.dyeable() ? CollectorTrimColors.color(stack) : 0xFFFFFFFF;
            }
        };
        event.registerItem(new ArmorAppearance() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.HEAD ? sakuraThinkerModel : original;
            }
        }, MDKCosmetic.SAKURA_THINKER.get());
        event.registerItem(collectorArmorExtensions, MDKCosmetic.COLLECTOR_HELMET.get(),
                MDKCosmetic.COLLECTOR_CHESTPLATE.get(), MDKCosmetic.COLLECTOR_LEGGINGS.get(),
                MDKCosmetic.COLLECTOR_BOOTS.get());
        final FoxHatModel hatModel = new FoxHatModel();
        final PandaHatModel pandaHatModel = new PandaHatModel();
        final CatEarsModel catEarsModel = new CatEarsModel();
        final AllayHatModel allayHatModel = new AllayHatModel();
        final AxolotlHatModel axolotlHatModel = new AxolotlHatModel();
        final RabbitEarsModel rabbitEarsModel = new RabbitEarsModel();
        final OctopusHatModel octopusHatModel = new OctopusHatModel();
        final CamelHatModel camelHatModel = new CamelHatModel();
        final CowHatModel cowHatModel = new CowHatModel();
        final MooshroomHatModel mooshroomHatModel = new MooshroomHatModel();
        final PolarBearHatModel polarBearHatModel = new PolarBearHatModel();
        final LlamaHatModel llamaHatModel = new LlamaHatModel();
        final SheepHatModel sheepHatModel = new SheepHatModel();
        final WolfHatModel wolfHatModel = new WolfHatModel();
        final HorseRidingHatModel horseHatModel = new HorseRidingHatModel();
        final DonkeyTravelHatModel donkeyHatModel = new DonkeyTravelHatModel();
        event.registerItem(new ArmorAppearance() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                // NeoForge copies the original humanoid head pose onto the replacement model.
                return slot == EquipmentSlot.HEAD ? hatModel : original;
            }
        }, MDKCosmetic.FOX_HAT.get());
        event.registerItem(new ArmorAppearance() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.HEAD ? pandaHatModel : original;
            }
        }, MDKCosmetic.PANDA_HAT.get());
        event.registerItem(new ArmorAppearance() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.HEAD ? catEarsModel : original;
            }

            // Used only for the tintable exterior. Keep the unchanged pink inner-ear
            // texture in the second (non-dyeable) armor layer.
            @Override
            public int getDefaultDyeColor(ItemStack stack) {
                return 0xFF303038; // Original undyed charcoal-black cat ears
            }

            @Override
            public int getArmorLayerTintColor(ItemStack stack, net.minecraft.client.renderer.entity.state.HumanoidRenderState entity,
                    ArmorDefinition.Layer layer, int layerIdx, int fallbackColor) {
                return layer.dyeable()
                        ? (0xFF000000 | DyedItemColor.getOrDefault(stack, 0x303038))
                        : 0xFFFFFFFF;
            }
        }, MDKCosmetic.CAT_EARS.get());
        event.registerItem(new ArmorAppearance() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.HEAD ? allayHatModel : original;
            }
        }, MDKCosmetic.ALLAY_HAT.get());
        event.registerItem(new ArmorAppearance() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.HEAD ? axolotlHatModel : original;
            }
            @Override
            public int getDefaultDyeColor(ItemStack stack) {
                return 0xFFF4B5D2;
            }
            @Override
            public int getArmorLayerTintColor(ItemStack stack, net.minecraft.client.renderer.entity.state.HumanoidRenderState entity,
                    ArmorDefinition.Layer layer, int layerIdx, int fallbackColor) {
                return layer.dyeable()
                        ? (0xFF000000 | DyedItemColor.getOrDefault(stack, 0xF4B5D2))
                        : 0xFFFFFFFF;
            }
        }, MDKCosmetic.AXOLOTL_HAT.get());
        event.registerItem(new ArmorAppearance() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.HEAD ? rabbitEarsModel : original;
            }
            @Override
            public int getDefaultDyeColor(ItemStack stack) {
                return 0xFFF1EBE1; // Original undyed off-white rabbit ears.
            }
            @Override
            public int getArmorLayerTintColor(ItemStack stack, net.minecraft.client.renderer.entity.state.HumanoidRenderState entity,
                    ArmorDefinition.Layer layer, int layerIdx, int fallbackColor) {
                return layer.dyeable()
                        ? (0xFF000000 | DyedItemColor.getOrDefault(stack, 0xF1EBE1))
                        : 0xFFFFFFFF;
            }
        }, MDKCosmetic.RABBIT_EARS.get());
        event.registerItem(new ArmorAppearance() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.HEAD ? octopusHatModel : original;
            }
        }, MDKCosmetic.OCTOPUS_HAT.get());
        event.registerItem(new ArmorAppearance() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.HEAD ? octopusHatModel : original;
            }
        }, MDKCosmetic.GLOW_SQUID_HAT.get());
        event.registerItem(new ArmorAppearance() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.HEAD ? camelHatModel : original;
            }
        }, MDKCosmetic.CAMEL_HAT.get());
        event.registerItem(new ArmorAppearance() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.HEAD ? cowHatModel : original;
            }
        }, MDKCosmetic.COW_HAT.get());
        event.registerItem(new ArmorAppearance() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.HEAD ? mooshroomHatModel : original;
            }
        }, MDKCosmetic.MOOSHROOM_HAT.get());
        event.registerItem(new ArmorAppearance() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.HEAD ? polarBearHatModel : original;
            }
        }, MDKCosmetic.POLAR_BEAR_HAT.get());
        event.registerItem(new ArmorAppearance() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.HEAD ? llamaHatModel : original;
            }
        }, MDKCosmetic.LLAMA_HAT.get());
        event.registerItem(new ArmorAppearance() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.HEAD ? sheepHatModel : original;
            }
            @Override
            public int getDefaultDyeColor(ItemStack stack) {
                return 0xFFF1F1F1;
            }
            @Override
            public int getArmorLayerTintColor(ItemStack stack, net.minecraft.client.renderer.entity.state.HumanoidRenderState entity,
                    ArmorDefinition.Layer layer, int layerIdx, int fallbackColor) {
                return layer.dyeable()
                        ? (0xFF000000 | DyedItemColor.getOrDefault(stack, 0xF1F1F1))
                        : 0xFFFFFFFF;
            }
        }, MDKCosmetic.SHEEP_HAT.get());
        event.registerItem(new ArmorAppearance() {
            @Override public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.HEAD ? wolfHatModel : original;
            }
            @Override public int getDefaultDyeColor(ItemStack stack) { return 0xFFB4B4B4; }
            @Override public int getArmorLayerTintColor(ItemStack stack, net.minecraft.client.renderer.entity.state.HumanoidRenderState entity,
                    ArmorDefinition.Layer layer, int layerIdx, int fallbackColor) {
                return layer.dyeable()
                        ? (0xFF000000 | DyedItemColor.getOrDefault(stack, 0xB4B4B4))
                        : 0xFFFFFFFF;
            }
        }, MDKCosmetic.WOLF_HAT.get());
        event.registerItem(new ArmorAppearance() {
            @Override public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.HEAD ? horseHatModel : original;
            }
            @Override public int getDefaultDyeColor(ItemStack stack) { return 0xFFF4EEE5; }
            @Override public int getArmorLayerTintColor(ItemStack stack, net.minecraft.client.renderer.entity.state.HumanoidRenderState entity,
                    ArmorDefinition.Layer layer, int layerIdx, int fallbackColor) {
                return layer.dyeable()
                        ? (0xFF000000 | DyedItemColor.getOrDefault(stack, 0xF4EEE5))
                        : 0xFFFFFFFF;
            }
        }, MDKCosmetic.HORSE_HAT.get());
        event.registerItem(new ArmorAppearance() {
            @Override public HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.HEAD ? donkeyHatModel : original;
            }
            @Override public int getDefaultDyeColor(ItemStack stack) { return 0xFFE5D6B9; }
            @Override public int getArmorLayerTintColor(ItemStack stack, net.minecraft.client.renderer.entity.state.HumanoidRenderState entity,
                    ArmorDefinition.Layer layer, int layerIdx, int fallbackColor) {
                return layer.dyeable()
                        ? (0xFF000000 | DyedItemColor.getOrDefault(stack, 0xE5D6B9))
                        : 0xFFFFFFFF;
            }
        }, MDKCosmetic.DONKEY_HAT.get());
    }
}
