package com.mdk.cosmetic;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import com.mdk.cosmetic.platform.Networking.RegisterPayloadHandlersEvent;

/** Client-localized search terms; actual inventory changes are validated by the server. */
public record CollectorBackpackQueryPayload(int menuId,String query,String matchingIds) implements CustomPacketPayload {
    public static final Type<CollectorBackpackQueryPayload> TYPE=new Type<>(
            Identifier.fromNamespaceAndPath(MDKCosmetic.MOD_ID,"backpack_search"));
    public static final StreamCodec<ByteBuf,CollectorBackpackQueryPayload> STREAM_CODEC=StreamCodec.composite(
            ByteBufCodecs.INT,CollectorBackpackQueryPayload::menuId,
            ByteBufCodecs.STRING_UTF8,CollectorBackpackQueryPayload::query,
            ByteBufCodecs.STRING_UTF8,CollectorBackpackQueryPayload::matchingIds,
            CollectorBackpackQueryPayload::new);
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    public static void register(RegisterPayloadHandlersEvent event){
        event.registrar("1").playToServer(TYPE,STREAM_CODEC,(payload,context)->{
            if(context.player() instanceof ServerPlayer player
                    && player.containerMenu instanceof CollectorBackpackMenu menu
                    && menu.containerId==payload.menuId() && payload.query().length()<=64
                    && payload.matchingIds().length()<=16000){
                menu.search(payload.query(),payload.matchingIds(),player);
            }
        });
    }
}
