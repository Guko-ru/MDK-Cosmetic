package com.mdk.cosmetic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import com.mdk.cosmetic.platform.Networking;

/** First awakening uses the vanilla title; long dialogues are small and wrapped HUD overlays. */
public final class CollectorTitles {
    private static final double RANGE_SQUARED=70*70;
    private CollectorTitles() {}
    public static void announce(ServerLevel level,Vec3 origin,String key,ChatFormatting color) {
        for(ServerPlayer player:level.players()) {
            if(player.distanceToSqr(origin)>RANGE_SQUARED)continue;
            if(!key.equals("title.mdkcosmetic.awakening")) {
                Integer rgb=color.getColor();
                Networking.sendToPlayer(player,new CollectorDialoguePayload(key,rgb==null?0xC18FFF:rgb));
            } else {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(8,42,12));
                player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(key).withStyle(color)));
            }
        }
    }
}
