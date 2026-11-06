package com.mdk.cosmetic;

import com.mdk.cosmetic.client.CollectorDialogueOverlay;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.mdk.cosmetic.platform.Networking.RegisterPayloadHandlersEvent;

/** Dedicated overlay message for short, properly wrapped, small soul dialogue. */
public record CollectorDialoguePayload(String message,int color) implements CustomPacketPayload {
    public static final Type<CollectorDialoguePayload> TYPE=new Type<>(
            ResourceLocation.fromNamespaceAndPath(MDKCosmetic.MOD_ID,"collector_dialogue"));
    public static final StreamCodec<ByteBuf,CollectorDialoguePayload> STREAM_CODEC=StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,CollectorDialoguePayload::message,
            ByteBufCodecs.INT,CollectorDialoguePayload::color,CollectorDialoguePayload::new);
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE,STREAM_CODEC,
                (payload,ctx) -> CollectorDialogueOverlay.receive(payload.message(),payload.color()));
    }
}
