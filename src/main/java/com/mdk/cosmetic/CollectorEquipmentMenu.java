package com.mdk.cosmetic;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;

/** One equipment screen: independent back slot, fuel, and armor controls, no external addon. */
public final class CollectorEquipmentMenu extends AbstractContainerMenu {
    public static final int BAG_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int PLAYER_START = 2;
    private final Inventory inventory;
    private final SimpleContainer special;
    private final ContainerData settings;
    private boolean loading = true;

    public CollectorEquipmentMenu(int id, Inventory inv) {
        super(MDKCosmetic.COLLECTOR_EQUIPMENT_MENU.get(),id);
        inventory = inv;
        special = new SimpleContainer(2) {
            @Override public void setChanged() {
                super.setChanged();
                if (!loading && !inventory.player.level().isClientSide()) {
                    CollectorEquipmentData.setBackpack(inventory.player,getItem(BAG_SLOT));
                    CollectorEquipmentData.setFuel(inventory.player,getItem(FUEL_SLOT));
                }
            }
        };
        if (!inv.player.level().isClientSide()) {
            special.setItem(0, CollectorEquipmentData.backpack(inv.player));
            special.setItem(1, CollectorEquipmentData.fuel(inv.player));
        }
        loading = false;
        addSlot(new Slot(special,0,23,42) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof CollectorBackpackItem; }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(special,1,23,88) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(MDKCosmetic.POWER_ESSENCE.get()); }
        });
        for (int row=0;row<3;row++) for(int col=0;col<9;col++)
            addSlot(new Slot(inv,9+row*9+col,20+col*18,159+row*18));
        for(int col=0;col<9;col++)
            addSlot(new Slot(inv,col,20+col*18,217));
        settings= inv.player.level().isClientSide() ? new SimpleContainerData(6) : new ContainerData(){
            @Override public int get(int index) {
                return switch(index) {
                    case 0 -> CollectorEquipmentData.fearMode(inv.player);
                    case 1 -> CollectorEquipmentData.flag(inv.player,"NightVision")?1:0;
                    case 2 -> CollectorEquipmentData.flag(inv.player,"Flight")?1:0;
                    case 3 -> CollectorEquipmentData.get(inv.player,"Speed");
                    case 4 -> CollectorEquipmentData.flag(inv.player,"NoFall")?1:0;
                    case 5 -> CollectorEquipmentData.get(inv.player,"FlightRemaining");
                    default -> 0;
                };
            }
            @Override public void set(int index,int value){}
            @Override public int getCount(){return 6;}
        };
        addDataSlots(settings);
    }

    public int fearMode() { return settings.get(0); }
    public boolean nightVision(){return settings.get(1)!=0;}
    public boolean flight(){return settings.get(2)!=0;}
    public int speed(){return settings.get(3);}
    public boolean noFall(){return settings.get(4)!=0;}
    public int flightRemaining(){return settings.get(5);}
    public boolean fullArmor(){return CollectorArmorFear.fullSet(inventory.player);}
    public boolean helmet(){return inventory.player.getItemBySlot(EquipmentSlot.HEAD).is(MDKCosmetic.COLLECTOR_HELMET.get());}
    public boolean chest(){return inventory.player.getItemBySlot(EquipmentSlot.CHEST).is(MDKCosmetic.COLLECTOR_CHESTPLATE.get());}
    public boolean legs(){return inventory.player.getItemBySlot(EquipmentSlot.LEGS).is(MDKCosmetic.COLLECTOR_LEGGINGS.get());}
    public boolean boots(){return inventory.player.getItemBySlot(EquipmentSlot.FEET).is(MDKCosmetic.COLLECTOR_BOOTS.get());}
    public ItemStack equipped(){return special.getItem(0);}
    public ItemStack fuelStack(){return special.getItem(1);}
    public boolean consumeFuel(){
        ItemStack fuel = special.getItem(FUEL_SLOT);
        if (!fuel.is(MDKCosmetic.POWER_ESSENCE.get()) || fuel.isEmpty()) return false;
        fuel.shrink(1);
        if (fuel.isEmpty()) special.setItem(FUEL_SLOT, ItemStack.EMPTY);
        else special.setChanged();
        return true;
    }

    @Override public boolean clickMenuButton(Player player, int button) {
        if (!stillValid(player)) return false;
        if (button >= 100 && button <= 110) {
            if (!player.getItemBySlot(EquipmentSlot.LEGS).is(MDKCosmetic.COLLECTOR_LEGGINGS.get())) return false;
            CollectorEquipmentData.set(player,"Speed",button-100);
            broadcastChanges();
            return true;
        }
        switch(button) {
            case 0 -> { if (!CollectorArmorFear.fullSet(player)) return false;
                CollectorEquipmentData.set(player,"FearMode",(CollectorEquipmentData.fearMode(player)+1)%3);
            }
            case 1 -> { if (!player.getItemBySlot(EquipmentSlot.HEAD).is(MDKCosmetic.COLLECTOR_HELMET.get())) return false;
                CollectorEquipmentData.flag(player,"NightVision",!CollectorEquipmentData.flag(player,"NightVision"));
            }
            case 2 -> { if (!player.getItemBySlot(EquipmentSlot.CHEST).is(MDKCosmetic.COLLECTOR_CHESTPLATE.get())) return false;
                CollectorEquipmentData.flag(player,"Flight",!CollectorEquipmentData.flag(player,"Flight"));
            }
            case 3,4 -> { if (!player.getItemBySlot(EquipmentSlot.LEGS).is(MDKCosmetic.COLLECTOR_LEGGINGS.get())) return false;
                int next=Math.clamp(CollectorEquipmentData.get(player,"Speed")+(button==3?1:-1),0,10);
                CollectorEquipmentData.set(player,"Speed",next);
            }
            case 5 -> { if (!player.getItemBySlot(EquipmentSlot.FEET).is(MDKCosmetic.COLLECTOR_BOOTS.get())) return false;
                CollectorEquipmentData.flag(player,"NoFall",!CollectorEquipmentData.flag(player,"NoFall"));
            }
            default -> { return false; }
        }
        broadcastChanges();return true;
    }

    @Override public ItemStack quickMoveStack(Player player,int index) {
        if(index<0 || index>=slots.size()) return ItemStack.EMPTY;
        Slot slot=slots.get(index);
        if(!slot.hasItem())return ItemStack.EMPTY;
        ItemStack moving=slot.getItem();
        ItemStack original=moving.copy();
        if(index<PLAYER_START) {
            if(!moveItemStackTo(moving,PLAYER_START,slots.size(),true))return ItemStack.EMPTY;
        } else if(moving.getItem() instanceof CollectorBackpackItem && special.getItem(BAG_SLOT).isEmpty()) {
            if(!moveItemStackTo(moving,BAG_SLOT,BAG_SLOT+1,false))return ItemStack.EMPTY;
        } else if(moving.is(MDKCosmetic.POWER_ESSENCE.get()) && special.getItem(FUEL_SLOT).getCount()<64) {
            if(!moveItemStackTo(moving,FUEL_SLOT,FUEL_SLOT+1,false))return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if(moving.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();
        special.setChanged();
        return original;
    }

    @Override public boolean stillValid(Player player) {return player.isAlive();}
}
