package com.mdk.cosmetic;

import com.mdk.cosmetic.client.UnoEncounterOverlay;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Client-only screen encounter cue; no control packets or commands. */
public record UnoEncounterPayload(int blackoutTicks,int shakeTicks) implements CustomPacketPayload {
    public static final Type<UnoEncounterPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MDKCosmetic.MOD_ID,"uno_encounter"));
    public static final StreamCodec<ByteBuf,UnoEncounterPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,UnoEncounterPayload::blackoutTicks,
            ByteBufCodecs.INT,UnoEncounterPayload::shakeTicks,UnoEncounterPayload::new);
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE,STREAM_CODEC,
                (payload,ctx) -> UnoEncounterOverlay.receive(payload.blackoutTicks(),payload.shakeTicks()));
    }
}
