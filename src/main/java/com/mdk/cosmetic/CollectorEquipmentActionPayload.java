package com.mdk.cosmetic;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.network.chat.Component;
import com.mdk.cosmetic.platform.ForgePayloads.Registration;

/** The only two key actions; menu opens are server-validated. */
public record CollectorEquipmentActionPayload(int action) implements CustomPacketPayload {
    public static final Type<CollectorEquipmentActionPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MDKCosmetic.MOD_ID,"equipment_keys"));
    public static final StreamCodec<ByteBuf,CollectorEquipmentActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, CollectorEquipmentActionPayload::action, CollectorEquipmentActionPayload::new);
    @Override public Type<? extends CustomPacketPayload> type(){ return TYPE; }
    public static void register(Registration event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, (payload,ctx)->{
            if (!(ctx.player() instanceof ServerPlayer player) || player.isSpectator()) return;
            if (payload.action() == 0) {
                player.openMenu(new SimpleMenuProvider((id,inv,p)->new CollectorEquipmentMenu(id,inv),
                        Component.translatable("menu.mdkcosmetic.collector_equipment")));
            } else if (payload.action() == 1) {
                ItemStackHolder.open(player);
            }
        });
    }
    private static final class ItemStackHolder {
        static void open(ServerPlayer player) {
            var stack = CollectorEquipmentData.backpack(player);
            if (!(stack.getItem() instanceof CollectorBackpackItem)) {
                player.displayClientMessage(Component.translatable("message.mdkcosmetic.no_equipped_backpack"),true);
                return;
            }
            if (CollectorBackpackData.linked(stack) && CollectorBackpackData.remoteCore(stack,com.mdk.cosmetic.platform.ServerAccess.get(player)) == null) {
                player.displayClientMessage(Component.translatable("message.mdkcosmetic.backpack_memory_unavailable"),true);
                return;
            }
            player.openMenu(new SimpleMenuProvider((id,inv,p)->new CollectorBackpackMenu(id,inv,true),
                    Component.translatable(CollectorBackpackData.linked(stack)
                            ? "menu.mdkcosmetic.collector_memory_terminal"
                            : "menu.mdkcosmetic.collector_backpack")));
        }
    }
}
