package com.mdk.cosmetic.client;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mdk.cosmetic.platform.Networking;
public final class FabricNetworking {
    private FabricNetworking() {}
    @SuppressWarnings({"unchecked","rawtypes"}) public static void initialize(){
        Networking.CLIENT_HANDLERS.forEach((type,handler)->ClientPlayNetworking.registerGlobalReceiver((CustomPacketPayload.Type)type,
            (payload,context)->handler.accept((CustomPacketPayload)payload,new Networking.Context(context.player()))));
    }
    public static void send(CustomPacketPayload payload){ClientPlayNetworking.send(payload);}
}
