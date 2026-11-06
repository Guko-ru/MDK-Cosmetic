package com.mdk.cosmetic.client;

import com.mdk.cosmetic.CollectorEquipmentActionPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Two configurable native key mappings. B is valid only with an equipped backpack. */
public final class CollectorEquipmentKeys {
    private static final String CATEGORY = "key.categories.mdkcosmetic";
    public static final KeyMapping EQUIPMENT = new KeyMapping("key.mdkcosmetic.equipment",
            InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_V,CATEGORY);
    public static final KeyMapping BACKPACK = new KeyMapping("key.mdkcosmetic.equipped_backpack",
            InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_B,CATEGORY);
    private CollectorEquipmentKeys() {}
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(EQUIPMENT);
        event.register(BACKPACK);
    }
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        while (EQUIPMENT.consumeClick()) net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new CollectorEquipmentActionPayload(0));
        while (BACKPACK.consumeClick()) net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new CollectorEquipmentActionPayload(1));
    }
}
