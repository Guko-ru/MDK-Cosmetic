package com.mdk.cosmetic;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Furnace-like interface; actual cooking is server-side in the memory core. */
public final class CollectorSmelterMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;
    private final CollectorLinkBlockEntity link;
    public CollectorSmelterMenu(int id,Inventory inv){
        this(id,inv,new SimpleContainer(3),new SimpleContainerData(4),null);
    }
    public CollectorSmelterMenu(int id,Inventory inv,Container container,ContainerData data,CollectorLinkBlockEntity link){
        super(MDKCosmetic.COLLECTOR_SMELTER_MENU.get(),id);
        this.container=container;this.data=data;this.link=link;
        addSlot(new Slot(container,0,56,34));
        addSlot(new Slot(container,1,56,70));
        addSlot(new Slot(container,2,116,34){
            @Override public boolean mayPlace(ItemStack stack){return false;}
        });
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)
            addSlot(new Slot(inv,9+row*9+col,8+col*18,112+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inv,col,8+col*18,170));
        addDataSlots(data);
    }
    public int progress(){return data.get(2);}
    public int total(){return Math.max(1,data.get(3));}
    public boolean isLit(){return data.get(0)>0;}
    @Override public boolean stillValid(Player player){return link==null ||
            link.memoryCore()==container && player.distanceToSqr(link.getBlockPos().getX()+0.5,
            link.getBlockPos().getY()+0.5,link.getBlockPos().getZ()+0.5)<64.0;}
    @Override public ItemStack quickMoveStack(Player player,int slotIndex){
        if(slotIndex<0||slotIndex>=slots.size())return ItemStack.EMPTY;
        Slot slot=slots.get(slotIndex);if(!slot.hasItem())return ItemStack.EMPTY;
        ItemStack held=slot.getItem(),result=held.copy();
        if(slotIndex<3){if(!moveItemStackTo(held,3,39,true))return ItemStack.EMPTY;}
        else if(!moveItemStackTo(held,0,1,false) && !moveItemStackTo(held,1,2,false))return ItemStack.EMPTY;
        if(held.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();
        if(held.getCount()==result.getCount())return ItemStack.EMPTY;
        slot.onTake(player,held);return result;
    }
}
