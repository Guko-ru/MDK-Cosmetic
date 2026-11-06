package com.mdk.cosmetic.platform;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Payload identifiers and codecs are preserved across loaders. */
public final class Networking {
    private Networking() {}
    public static final Map<CustomPacketPayload.Type<?>,BiConsumer<CustomPacketPayload,Context>> CLIENT_HANDLERS=new LinkedHashMap<>();
    public record Context(Player player) {}
    public static final class RegisterPayloadHandlersEvent {
        public Registrar registrar(String version){return new Registrar();}
    }
    public static final class Registrar {
        public <T extends CustomPacketPayload> void playToServer(CustomPacketPayload.Type<T> type,StreamCodec<? super RegistryFriendlyByteBuf,T> codec,BiConsumer<T,Context> handler){
            PayloadTypeRegistry.playC2S().register(type,codec);
            ServerPlayNetworking.registerGlobalReceiver(type,(payload,context)->handler.accept(payload,new Context(context.player())));
        }
        @SuppressWarnings("unchecked") public <T extends CustomPacketPayload> void playToClient(CustomPacketPayload.Type<T> type,StreamCodec<? super RegistryFriendlyByteBuf,T> codec,BiConsumer<T,Context> handler){
            PayloadTypeRegistry.playS2C().register(type,codec);
            CLIENT_HANDLERS.put(type,(payload,context)->handler.accept((T)payload,context));
        }
    }
    public static void sendToPlayer(ServerPlayer player,CustomPacketPayload payload){ServerPlayNetworking.send(player,payload);}
    public static void sendToServer(CustomPacketPayload payload){com.mdk.cosmetic.client.FabricNetworking.send(payload);}
}
