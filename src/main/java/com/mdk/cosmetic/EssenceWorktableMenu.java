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

/** Dedicated inventory interface: one input, one output, synced 0..63 progress. */
public final class EssenceWorktableMenu extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerData data;

    public EssenceWorktableMenu(int id, Inventory playerInv) {
        this(id, playerInv, new SimpleContainer(2), new SimpleContainerData(1));
    }
    public EssenceWorktableMenu(int id, Inventory playerInv, Container inventory, ContainerData data) {
        super(MDKCosmetic.ESSENCE_WORKTABLE_MENU.get(), id);
        checkContainerSize(inventory, 2);
        checkContainerDataCount(data, 1);
        this.inventory = inventory;
        this.data = data;
        this.addSlot(new Slot(inventory, 0, 27, 39) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.is(MDKCosmetic.POWER_ESSENCE.get());
            }
        });
        this.addSlot(new Slot(inventory, 1, 132, 39) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        addDataSlots(data);
    }
    public int getProgress() { return Math.clamp(data.get(0), 0, EssenceWorktableBlockEntity.COST); }
    @Override public boolean stillValid(Player player) { return inventory.stillValid(player); }
    @Override public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) return result;
        ItemStack stack = slot.getItem();
        result = stack.copy();
        if (slotIndex < 2) {
            if (!moveItemStackTo(stack, 2, 38, true)) return ItemStack.EMPTY;
        } else if (stack.is(MDKCosmetic.POWER_ESSENCE.get())) {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else if (slotIndex < 29) {
            if (!moveItemStackTo(stack, 29, 38, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 2, 29, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return result;
    }
}
