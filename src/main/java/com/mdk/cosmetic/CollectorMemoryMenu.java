package com.mdk.cosmetic;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Paged virtual storage; the slots show item TYPES, not individual physical stacks. */
public final class CollectorMemoryMenu extends AbstractContainerMenu {
    private final SimpleContainer display;
    private final ContainerData data;
    private final CollectorMemoryCoreBlockEntity memory;
    private final CollectorLinkBlockEntity link;
    private int page;
    public CollectorMemoryMenu(int id,Inventory inv) {
        this(id,inv,null,null);
    }
    public CollectorMemoryMenu(int id,Inventory playerInv,CollectorMemoryCoreBlockEntity core,CollectorLinkBlockEntity link) {
        super(MDKCosmetic.COLLECTOR_MEMORY_MENU.get(),id);
        memory=core;this.link=link;
        display=new SimpleContainer(27);
        data=core==null?new SimpleContainerData(56):new ContainerData() {
            @Override public int get(int i) {
                if(i==0)return page;
                if(i==1)return memory.pages();
                int index=page*27+(i-2)/2;
                long n=memory.countAt(index);
                return (int)((i%2==0?n:n>>>16)&65535L);
            }
            @Override public void set(int i,int v) {}
            @Override public int getCount(){return 56;}
        };
        for(int row=0;row<3;row++)for(int col=0;col<9;col++) {
            int index=row*9+col;
            addSlot(new Slot(display,index,8+col*18,22+row*18) {
                @Override public boolean mayPlace(ItemStack stack){return false;}
                @Override public boolean mayPickup(Player player){return false;}
            });
        }
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)
            addSlot(new Slot(playerInv,9+row*9+col,8+col*18,123+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(playerInv,col,8+col*18,181));
        addDataSlots(data);
        if(core!=null)refresh();
    }
    private void refresh(){
        if(memory==null)return;
        for(int i=0;i<27;i++)display.setItem(i,memory.view(page*27+i));
        broadcastChanges();
    }
    @Override public boolean clickMenuButton(Player player,int id) {
        if(id!=0 && id!=1)return false;
        if(memory==null)return true;
        page=Math.clamp(page+(id==0?-1:1),0,memory.pages()-1);
        refresh();return true;
    }
    public int getPage(){return Math.max(0,data.get(0));}
    public int getPages(){return Math.max(1,data.get(1));}
    public long getQuantity(int i) {
        int low=data.get(i*2+2)&65535;
        int high=data.get(i*2+3)&65535;
        return ((long)high<<16)|low;
    }
    @Override public void clicked(int slot,int button,ClickType click,Player player){
        if(slot>=0 && slot<27) {
            // Never allow vanilla Slot mechanics to move the ghost display stack.
            if(memory==null || !stillValid(player))return;
            if(click!=ClickType.PICKUP)return;
            ItemStack held=getCarried();
            if(!held.isEmpty()) {
                int taken=memory.deposit(held);
                if(taken>0){held.shrink(taken);setCarried(held.isEmpty()?ItemStack.EMPTY:held);}
            } else {
                ItemStack output=memory.withdraw(page*27+slot,button==1?1:64);
                if(!output.isEmpty())setCarried(output);
            }
            page=Math.clamp(page,0,memory.pages()-1);
            refresh();return;
        }
        super.clicked(slot,button,click,player);
    }
    @Override public ItemStack quickMoveStack(Player player,int slotIndex) {
        if(slotIndex<27 || slotIndex>=slots.size()||memory==null||!stillValid(player))return ItemStack.EMPTY;
        Slot slot=slots.get(slotIndex);
        if(!slot.hasItem())return ItemStack.EMPTY;
        ItemStack stack=slot.getItem();ItemStack copy=stack.copy();
        int taken=memory.deposit(stack);
        if(taken<=0)return ItemStack.EMPTY;
        stack.shrink(taken);
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();
        refresh();return copy;
    }
    @Override public boolean stillValid(Player player) {
        return memory==null || link!=null && link.getLevel()!=null
                && link.getLevel().getBlockEntity(link.getBlockPos())==link
                && link.memoryCore()==memory &&
                player.distanceToSqr(link.getBlockPos().getX()+0.5,
                link.getBlockPos().getY()+0.5,link.getBlockPos().getZ()+0.5)<64.0D;
    }
}
