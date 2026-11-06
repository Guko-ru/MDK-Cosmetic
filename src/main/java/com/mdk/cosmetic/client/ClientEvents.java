package com.mdk.cosmetic.client;

import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import com.mdk.cosmetic.client.CosmeticItemTints.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import com.mdk.cosmetic.platform.ArmorDefinition;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ClientEvents {
    private ClientEvents() {}
    public static final class RenderGuiEvent { public record Post(GuiGraphics getGuiGraphics) {} }
    public static final class ClientTickEvent { public record Post() {} }
    public record ItemTooltipEvent(ItemStack getItemStack,List<Component> getToolTip) {}
    public static final class RegisterKeyMappingsEvent {
        public void register(KeyMapping key){KeyBindingHelper.registerKeyBinding(key);}
    }
    public static final class RegisterColorHandlersEvent {
        public static final class Item {
            public void register(Color color,net.minecraft.world.item.Item... items){CosmeticItemTints.register(color,items);}
        }
    }
    public static final class RegisterMenuScreensEvent {
        public <M extends AbstractContainerMenu,S extends Screen & MenuAccess<M>> void register(MenuType<M> menu,MenuScreens.ScreenConstructor<M,S> factory){MenuScreens.register(menu,factory);}
    }
    public interface ArmorAppearance {
        default HumanoidModel<?> getHumanoidArmorModel(net.minecraft.client.renderer.entity.state.HumanoidRenderState entity,ItemStack stack,EquipmentSlot slot,HumanoidModel<?> original){return original;}
        default int getDefaultDyeColor(ItemStack stack){return 0xFFA06540;}
        default int getArmorLayerTintColor(ItemStack stack,net.minecraft.client.renderer.entity.state.HumanoidRenderState entity,ArmorDefinition.Layer layer,int index,int fallback){return fallback;}
    }
    public static final class RegisterClientExtensionsEvent {
        public void registerItem(ArmorAppearance appearance,Item... items){ArmorRenderer.register(new FabricArmorRenderer(appearance),items);}
    }
    public static final class EntityRenderersEvent {
        public static final class RegisterLayerDefinitions {
            public void registerLayerDefinition(ModelLayerLocation layer,Supplier<LayerDefinition> definition){EntityModelLayerRegistry.registerModelLayer(layer,definition::get);}
        }
        public static final class RegisterRenderers {
            public <E extends Entity> void registerEntityRenderer(EntityType<? extends E> type,EntityRendererProvider<E> factory){EntityRendererRegistry.register(type,factory);}
        }
        public record AddLayers(EntityRenderer<?,?> renderer) {
            public List<PlayerSkin.Model> getSkins(){return List.of(PlayerSkin.Model.WIDE);}
            public EntityRenderer<?,?> getSkin(PlayerSkin.Model ignored){return renderer;}
        }
    }
    public static final class ViewportEvent {
        public static final class ComputeCameraAngles {
            private float pitch,yaw,roll;
            public ComputeCameraAngles(float pitch,float yaw,float roll){this.pitch=pitch;this.yaw=yaw;this.roll=roll;}
            public float getPitch(){return pitch;} public void setPitch(float v){pitch=v;}
            public float getYaw(){return yaw;} public void setYaw(float v){yaw=v;}
            public float getRoll(){return roll;} public void setRoll(float v){roll=v;}
        }
    }
}
