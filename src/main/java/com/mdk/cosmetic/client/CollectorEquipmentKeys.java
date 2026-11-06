package com.mdk.cosmetic.client;

import com.mdk.cosmetic.CollectorEquipmentActionPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Two configurable native key mappings. B is valid only with an equipped backpack. */
public final class CollectorEquipmentKeys {
    private static final KeyMapping.Category CATEGORY=KeyMapping.Category.register(net.minecraft.resources.Identifier.parse("mdkcosmetic:equipment"));
    public static final KeyMapping EQUIPMENT = new KeyMapping("key.mdkcosmetic.equipment",
            InputConstants.Type.KEYBOARD,com.mojang.blaze3d.platform.InputConstants.KEY_V,CATEGORY);
    public static final KeyMapping BACKPACK = new KeyMapping("key.mdkcosmetic.equipped_backpack",
            InputConstants.Type.KEYBOARD,com.mojang.blaze3d.platform.InputConstants.KEY_B,CATEGORY);
    private CollectorEquipmentKeys() {}
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(EQUIPMENT);
        event.register(BACKPACK);
    }
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gui.screen() != null) return;
        while (EQUIPMENT.consumeClick()) net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new CollectorEquipmentActionPayload(0));
        while (BACKPACK.consumeClick()) net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new CollectorEquipmentActionPayload(1));
    }
}
