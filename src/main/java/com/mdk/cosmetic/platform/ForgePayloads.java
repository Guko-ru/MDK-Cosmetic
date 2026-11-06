package com.mdk.cosmetic.platform;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
public final class ForgePayloads {
 private static final Map<CustomPacketPayload.Type<?>,Channel<CustomPacketPayload>> CHANNELS=new HashMap<>();
 private ForgePayloads(){}
 public record Context(Player player){}
 public static final class Registration {
  public Registration registrar(String version){return this;}
  public <T extends CustomPacketPayload> void playToClient(CustomPacketPayload.Type<T> type,StreamCodec<? super RegistryFriendlyByteBuf,T> codec,BiConsumer<T,Context> handler){register(type,codec,handler,false);}
  public <T extends CustomPacketPayload> void playToServer(CustomPacketPayload.Type<T> type,StreamCodec<? super RegistryFriendlyByteBuf,T> codec,BiConsumer<T,Context> handler){register(type,codec,handler,true);}
  private <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type,StreamCodec<? super RegistryFriendlyByteBuf,T> codec,BiConsumer<T,Context> handler,boolean serverbound){
   var protocol=ChannelBuilder.named(type.id()).networkProtocolVersion(1).payloadChannel().play();
   var flow=serverbound?protocol.serverbound():protocol.clientbound();
   StreamCodec<RegistryFriendlyByteBuf,T> adapted=StreamCodec.of(codec::encode,codec::decode);
   var channel=flow.addMain(type,adapted,(payload,ctx)->handler.accept(payload,new Context(ctx.getSender()))).build();
   if(CHANNELS.putIfAbsent(type,channel)!=null)throw new IllegalStateException("Duplicate MDK payload "+type.id());
  }
 }
 private static Channel<CustomPacketPayload> channel(CustomPacketPayload payload){var channel=CHANNELS.get(payload.type());if(channel==null)throw new IllegalStateException("Unregistered MDK payload "+payload.type().id());return channel;}
 public static void sendToServer(CustomPacketPayload payload){channel(payload).send(payload,PacketDistributor.SERVER.noArg());}
 public static void sendToPlayer(ServerPlayer player,CustomPacketPayload payload){channel(payload).send(payload,PacketDistributor.PLAYER.with(player));}
}
