package com.mdk.cosmetic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;

/** Server-side armor abilities and persistence, including death-safe backpack handling. */
public final class CollectorEquipmentEffects {
    private CollectorEquipmentEffects() {}
    private static final ResourceLocation SPEED_ID = ResourceLocation.fromNamespaceAndPath(MDKCosmetic.MOD_ID,"collector_leg_speed");

    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 60 == 0) CollectorEquipmentData.syncVisual(player);
        boolean helmet = player.getItemBySlot(EquipmentSlot.HEAD).is(MDKCosmetic.COLLECTOR_HELMET.get());
        boolean chest = player.getItemBySlot(EquipmentSlot.CHEST).is(MDKCosmetic.COLLECTOR_CHESTPLATE.get());
        boolean pants = player.getItemBySlot(EquipmentSlot.LEGS).is(MDKCosmetic.COLLECTOR_LEGGINGS.get());

        if (player.tickCount % 20 == 0) {
            boolean nv = helmet && CollectorEquipmentData.flag(player,"NightVision");
            if (nv) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,240,0,false,false,false));
                CollectorEquipmentData.flag(player,"NVApplied",true);
            } else if (CollectorEquipmentData.flag(player,"NVApplied")) {
                player.removeEffect(MobEffects.NIGHT_VISION);
                CollectorEquipmentData.flag(player,"NVApplied",false);
            }
            var move = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (move != null) {
                move.removeModifier(SPEED_ID);
                if (pants) {
                    int setting = Math.clamp(CollectorEquipmentData.get(player,"Speed"),0,10);
                    if (setting > 0) move.addTransientModifier(new AttributeModifier(SPEED_ID,setting/10.0D,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
            }
        }
        if (player.isCreative() || player.isSpectator()) return;
        boolean requested = chest && CollectorEquipmentData.flag(player,"Flight");
        int remaining = CollectorEquipmentData.get(player,"FlightRemaining");
        CollectorEquipmentMenu menu = player.containerMenu instanceof CollectorEquipmentMenu open ? open : null;
        ItemStack fuel = menu == null ? CollectorEquipmentData.fuel(player) : menu.fuelStack();
        boolean allowed = requested && (remaining>0 || !fuel.isEmpty());
        if (player.getAbilities().mayfly != allowed) {
            player.getAbilities().mayfly = allowed;
            if (!allowed) player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
        if (allowed && player.getAbilities().flying) {
            if (remaining<=0) {
                if (fuel.isEmpty()) return;
                if (menu != null) {
                    if (!menu.consumeFuel()) return;
                } else {
                    fuel.shrink(1);
                    CollectorEquipmentData.setFuel(player,fuel);
                }
                remaining = 3600;
            }
            remaining--;
            CollectorEquipmentData.set(player,"FlightRemaining",remaining);
        }
    }

    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && player.getItemBySlot(EquipmentSlot.FEET).is(MDKCosmetic.COLLECTOR_BOOTS.get())
                && CollectorEquipmentData.flag(player,"NoFall")) event.setCanceled(true);
    }

    /** Equipment is dropped with other drops, so existing death-insurance can transfer all at once. */
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.isCanceled()) return;
        if (((net.minecraft.server.level.ServerLevel)player.level()).getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;
        ItemStack backpack = CollectorEquipmentData.backpack(player);
        if (backpack.isEmpty()) return;
        event.getDrops().add(new ItemEntity(player.level(), player.getX(),player.getY()+0.4D,player.getZ(),backpack.copy()));
        CollectorEquipmentData.setBackpack(player,ItemStack.EMPTY);
    }
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) CollectorEquipmentData.syncVisual(player);
    }
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) CollectorEquipmentData.syncVisual(player);
    }
    public static void onClone(PlayerEvent.Clone event) {
        CollectorEquipmentData.copyTo(event.getOriginal(), event.getEntity());
    }
}
