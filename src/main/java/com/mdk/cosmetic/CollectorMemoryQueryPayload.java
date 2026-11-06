package com.mdk.cosmetic;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import com.mdk.cosmetic.platform.ForgePayloads.Registration;

/** Localized search candidates, matched by the client, checked against real storage by the server. */
public record CollectorMemoryQueryPayload(int menuId, String query, String matchingItemIds)
        implements CustomPacketPayload {
    public static final Type<CollectorMemoryQueryPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MDKCosmetic.MOD_ID, "collector_memory_query"));
    public static final StreamCodec<ByteBuf, CollectorMemoryQueryPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CollectorMemoryQueryPayload::menuId,
            ByteBufCodecs.STRING_UTF8, CollectorMemoryQueryPayload::query,
            ByteBufCodecs.STRING_UTF8, CollectorMemoryQueryPayload::matchingItemIds,
            CollectorMemoryQueryPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void register(Registration event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof CollectorLinkMenu menu
                    && menu.containerId == payload.menuId()
                    && payload.query().length() <= 64 && payload.matchingItemIds().length() <= 16000) {
                menu.search(payload.query(), payload.matchingItemIds(), player);
            }
        });
    }
}
