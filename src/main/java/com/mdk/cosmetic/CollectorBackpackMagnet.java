package com.mdk.cosmetic;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import com.mdk.cosmetic.platform.Events.PlayerTickEvent;

/** Server-authoritative pickup magnet with item/component whitelist or blacklist. */
public final class CollectorBackpackMagnet {
    private CollectorBackpackMagnet(){}
    public static void onTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player) || player.tickCount%10!=0)return;
        // A menu keeps its own in-memory slot snapshot; do not race it with world pickups.
        if(player.containerMenu instanceof CollectorBackpackMenu)return;
        for(int slot=0;slot<=36;slot++){
            boolean equipped=slot==36;
            ItemStack backpack=equipped ? CollectorEquipmentData.backpack(player)
                    : player.getInventory().getItem(slot);
            if(!(backpack.getItem() instanceof CollectorBackpackItem)
                    || !CollectorBackpackData.hasMagnet(backpack, player.registryAccess())
                    || !CollectorBackpackData.magnetEnabled(backpack))continue;
            CollectorMemoryCoreBlockEntity remote=CollectorBackpackData.linked(backpack)
                    ?CollectorBackpackData.remoteCore(backpack,com.mdk.cosmetic.platform.ServerAccess.get(player)):null;
            if(CollectorBackpackData.linked(backpack) && remote==null)continue;
            List<ItemStack> stored=remote==null?CollectorBackpackData.items(backpack,player.registryAccess()):null;
            boolean changed=false;
            for(ItemEntity entity:player.level().getEntitiesOfClass(ItemEntity.class,player.getBoundingBox().inflate(4.5D))){
                if(!entity.isAlive())continue;
                if(entity.getOwner()!=null && entity.getOwner()!=player)continue;
                if(((com.mdk.cosmetic.platform.PickupTarget)entity).mdk$getPickupTarget()!=null && !((com.mdk.cosmetic.platform.PickupTarget)entity).mdk$getPickupTarget().equals(player.getUUID()))continue;
                ItemStack dropped=entity.getItem();
                if(dropped.isEmpty()||!CollectorBackpackData.accepts(backpack,dropped,player.registryAccess()))continue;
                int remaining=dropped.getCount();
                if(remote!=null)remaining-=remote.deposit(dropped);
                else {
                    for(int i=0;i<CollectorBackpackData.capacity(backpack)&&remaining>0;i++){
                        ItemStack own=stored.get(i);
                        if(own.isEmpty() || !ItemStack.isSameItemSameComponents(own,dropped))continue;
                        int amount=Math.min(remaining,own.getMaxStackSize()-own.getCount());
                        if(amount>0){own.grow(amount);remaining-=amount;changed=true;}
                    }
                    for(int i=0;i<CollectorBackpackData.capacity(backpack)&&remaining>0;i++){
                        if(!stored.get(i).isEmpty())continue;
                        int amount=Math.min(remaining,dropped.getMaxStackSize());
                        stored.set(i,dropped.copyWithCount(amount));remaining-=amount;changed=true;
                    }
                }
                if(remaining==0)entity.discard();
                else if(remaining<dropped.getCount())entity.setItem(dropped.copyWithCount(remaining));
            }
            if(changed && stored!=null){
                CollectorBackpackData.saveItems(backpack,stored,player.registryAccess());
                if(equipped)CollectorEquipmentData.setBackpack(player,backpack);
            }
        }
    }
}
