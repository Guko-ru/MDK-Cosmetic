package com.mdk.cosmetic.platform;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

public final class FabricHooks {
    private FabricHooks() {}
    private static final ThreadLocal<ArrayDeque<DropCapture>> DROP_CAPTURES=ThreadLocal.withInitial(ArrayDeque::new);
    private record DropCapture(LivingEntity owner,DamageSource source,List<ItemEntity> drops) {}
    public static void beginDrops(LivingEntity entity,DamageSource source){DROP_CAPTURES.get().push(new DropCapture(entity,source,new ArrayList<>()));}
    public static void endDrops(LivingEntity entity){
        var scopes=DROP_CAPTURES.get();if(scopes.isEmpty()||scopes.peek().owner()!=entity)throw new IllegalStateException("Unbalanced death loot capture");
        var scope=scopes.pop();var before=List.copyOf(scope.drops());
        var event=new Events.LivingDropsEvent(entity,scope.source(),scope.drops());Events.dispatch(event);
        if(event.isCanceled())scope.drops().clear();
        for(var item:before)if(!scope.drops().contains(item))item.discard();
        for(var item:scope.drops())if(!before.contains(item))entity.level().addFreshEntity(item);
        if(scopes.isEmpty())DROP_CAPTURES.remove();
    }
    public static void initialize(){
        ServerEntityEvents.ENTITY_LOAD.register((entity,level)->{
            var scopes=DROP_CAPTURES.get();
            if(entity instanceof ItemEntity item&&!scopes.isEmpty())scopes.peek().drops().add(item);
            Events.dispatch(new Events.EntityJoinLevelEvent(entity));
        });
        ServerTickEvents.END_SERVER_TICK.register(server->Events.dispatch(new Events.ServerTickEvent.Post(server)));
        UseEntityCallback.EVENT.register((player,level,hand,target,hit)->{
            var event=new Events.PlayerInteractEvent.EntityInteract(player,target,hand);Events.dispatch(event);
            return event.isCanceled()?event.getCancellationResult():InteractionResult.PASS;
        });
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity,source,amount)->{
            var event=new Events.LivingDeathEvent(entity,source);Events.dispatch(event);return !event.isCanceled();
        });
        PlayerBlockBreakEvents.BEFORE.register((world,player,pos,state,blockEntity)->{
            var event=new Events.BlockEvent.BreakEvent(player,state);Events.dispatch(event);return !event.isCanceled();
        });
        ServerPlayerEvents.COPY_FROM.register((oldPlayer,newPlayer,alive)->Events.dispatch(new Events.PlayerEvent.Clone(newPlayer,oldPlayer)));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer,newPlayer,alive)->Events.dispatch(new Events.PlayerEvent.PlayerRespawnEvent(newPlayer)));
        ServerPlayConnectionEvents.JOIN.register((handler,sender,server)->Events.dispatch(new Events.PlayerEvent.PlayerLoggedInEvent(handler.player)));
    }
}
