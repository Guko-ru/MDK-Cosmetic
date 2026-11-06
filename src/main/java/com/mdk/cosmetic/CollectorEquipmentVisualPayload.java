package com.mdk.cosmetic;

import com.mdk.cosmetic.client.CollectorEquipmentVisuals;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Small visual-only packet; clients never determine what is actually equipped. */
public record CollectorEquipmentVisualPayload(String uuid, int model, boolean linked) implements CustomPacketPayload {
    public static final Type<CollectorEquipmentVisualPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MDKCosmetic.MOD_ID,"equipment_visual"));
    public static final StreamCodec<ByteBuf,CollectorEquipmentVisualPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,CollectorEquipmentVisualPayload::uuid,
            ByteBufCodecs.INT,CollectorEquipmentVisualPayload::model,
            ByteBufCodecs.BOOL,CollectorEquipmentVisualPayload::linked,
            CollectorEquipmentVisualPayload::new);
    @Override public Type<? extends CustomPacketPayload> type(){ return TYPE; }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, STREAM_CODEC,
                (payload,ctx)->CollectorEquipmentVisuals.set(payload.uuid(), payload.model(),payload.linked()));
    }
}
