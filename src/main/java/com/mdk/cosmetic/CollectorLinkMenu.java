package com.mdk.cosmetic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

/** All-in-one Collector interface. Shop at left; searchable memory, 3x3 crafting and fuel-free smelting at right. */
public final class CollectorLinkMenu extends AbstractContainerMenu {
    public static final int MEMORY_COLUMNS = 12;
    public static final int MEMORY_ROWS = 4;
    public static final int MEMORY_SIZE = MEMORY_COLUMNS * MEMORY_ROWS;
    public static final int MEMORY_START = 3;
    public static final int CRAFT_START = MEMORY_START + MEMORY_SIZE;
    public static final int CRAFT_RESULT = CRAFT_START + 9;
    public static final int SMELT_INPUT = CRAFT_RESULT + 1;
    public static final int SMELT_OUTPUT = SMELT_INPUT + 1;
    public static final int PLAYER_START = SMELT_OUTPUT + 1;
    public static final int PLAYER_END = PLAYER_START + 36;
    // Two metadata values plus four 16-bit words for each long item quantity, plus smelting progress.
    private static final int MEMORY_DATA_SIZE = 2 + MEMORY_SIZE * 4 + 2;

    private final Container inventory;
    private final ContainerData data;
    private final ContainerData memoryData;
    private final CollectorLinkBlockEntity controller;
    private final CollectorMemoryCoreBlockEntity memory;
    private final SimpleContainer visible = new SimpleContainer(MEMORY_SIZE);
    private final Container furnace;
    private final TransientCraftingContainer craft = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer craftResult = new ResultContainer();
    private final Player owner;
    private final List<Integer> filtered = new ArrayList<>();
    private Set<String> itemMatches = Set.of();
    private String query = "";
    private int rowOffset;
    private long seenRevision = -1;

    public CollectorLinkMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(3), new SimpleContainerData(5), null);
    }

    public CollectorLinkMenu(int id, Inventory playerInv, Container inventory, ContainerData data,
            CollectorLinkBlockEntity controller) {
        super(MDKCosmetic.COLLECTOR_LINK_MENU.get(), id);
        checkContainerSize(inventory, 3);
        checkContainerDataCount(data, 5);
        this.inventory = inventory;
        this.data = data;
        this.controller = controller;
        this.owner = playerInv.player;
        this.memory = controller == null ? null : controller.memoryCore();
        this.furnace = memory == null ? new SimpleContainer(3) : memory;
        this.memoryData = memory == null ? new SimpleContainerData(MEMORY_DATA_SIZE) : new ContainerData() {
            @Override public int get(int index) {
                if (index == 0) return rowOffset;
                if (index == 1) return filtered.size();
                if (index >= 2 && index < 2 + MEMORY_SIZE * 4) {
                    int cell = (index - 2) / 4;
                    int word = (index - 2) % 4;
                    int indexInMemory = cellIndex(cell);
                    long count = indexInMemory < 0 ? 0 : memory.countAt(indexInMemory);
                    return (int)((count >>> (word * 16)) & 0xFFFF);
                }
                if (index == 2 + MEMORY_SIZE * 4) return memory.furnaceData.get(2);
                if (index == 3 + MEMORY_SIZE * 4) return CollectorMemoryCoreBlockEntity.SMELT_TICKS;
                return 0;
            }
            @Override public void set(int index, int value) { }
            @Override public int getCount() { return MEMORY_DATA_SIZE; }
        };

        addSlot(new Slot(inventory, 0, 42, 140) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(MDKCosmetic.CHARGED_POWER_ESSENCE.get()); }
        });
        addSlot(new Slot(inventory, 1, 222, 140) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) {
                if (!isActive() || getProgress() < CollectorLinkBlockEntity.TRADE_TICKS
                        || !inventory.getItem(0).is(MDKCosmetic.CHARGED_POWER_ESSENCE.get())
                        || inventory.getItem(0).getCount() < CollectorOffers.cost(getSelectedOffer())
                        || !ItemStack.isSameItemSameComponents(inventory.getItem(1), CollectorOffers.preview(getSelectedOffer()))
                        || inventory.getItem(1).getCount() != CollectorOffers.preview(getSelectedOffer()).getCount()) return false;
                return controller == null || controller.canCompleteTrade();
            }
            @Override public void onTake(Player player, ItemStack stack) {
                if (controller != null) controller.completeTrade(player);
                super.onTake(player, stack);
            }
        });
        addSlot(new Slot(inventory, 2, 571, 29) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.is(MDKCosmetic.POWER_ESSENCE.get()) || stack.is(MDKCosmetic.CHARGED_POWER_ESSENCE.get());
            }
        });

        for (int i = 0; i < MEMORY_SIZE; i++) {
            addSlot(new Slot(visible, i, 310 + (i % MEMORY_COLUMNS) * 18, 91 + (i / MEMORY_COLUMNS) * 18) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player player) { return false; }
            });
        }
        for (int i = 0; i < 9; i++)
            addSlot(new Slot(craft, i, 319 + i % 3 * 18, 207 + i / 3 * 18) {
                @Override public boolean mayPlace(ItemStack stack) { return controller == null || memoryReadyNow(); }
            });
        addSlot(new ResultSlot(playerInv.player, craft, craftResult, 0, 393, 225) {
            @Override public boolean mayPickup(Player player) { return (controller == null || memoryReadyNow()) && super.mayPickup(player); }
        });
        addSlot(new Slot(furnace, 0, 452, 222) {
            @Override public boolean mayPlace(ItemStack stack) { return controller == null || memoryReadyNow(); }
        });
        addSlot(new Slot(furnace, 2, 532, 222) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) { return controller == null || memoryReadyNow(); }
        });
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++)
            addSlot(new Slot(playerInv, 9 + r * 9 + c, 61 + 18 * c, 211 + 18 * r));
        for (int c = 0; c < 9; c++) addSlot(new Slot(playerInv, c, 61 + 18 * c, 273));
        addDataSlots(data);
        addDataSlots(memoryData);
        if (memory != null) rebuild();
    }

    private boolean memoryReadyNow() { return controller != null && controller.memoryCore() == memory && memory != null; }
    public boolean isMemoryReady() { return data.get(4) != 0; }
    public int getStress() { return Math.clamp(data.get(0), 0, 100); }
    public boolean isActive() { return data.get(1) != 0; }
    public int getProgress() { return Math.clamp(data.get(2), 0, CollectorLinkBlockEntity.TRADE_TICKS); }
    public int getSelectedOffer() { return Math.clamp(data.get(3), 0, CollectorOffers.COUNT - 1); }
    public int getMemoryRow() { return Math.max(0, memoryData.get(0)); }
    public int getMemoryTotal() { return Math.max(0, memoryData.get(1)); }
    public int getMemoryMaxRow() { return Math.max(0, (getMemoryTotal() + MEMORY_COLUMNS - 1) / MEMORY_COLUMNS - MEMORY_ROWS); }
    public int getSmeltProgress() { return Math.max(0, memoryData.get(2 + MEMORY_SIZE * 4)); }
    public int getSmeltTime() { return Math.max(1, memoryData.get(3 + MEMORY_SIZE * 4)); }
    public long getMemoryQuantity(int cell) {
        if (cell < 0 || cell >= MEMORY_SIZE) return 0;
        long count = 0L;
        for (int i = 0; i < 4; i++) count |= (long)(memoryData.get(2 + cell * 4 + i) & 65535) << (i * 16);
        return count;
    }
    private int cellIndex(int cell) {
        int index = rowOffset * MEMORY_COLUMNS + cell;
        return index >= 0 && index < filtered.size() ? filtered.get(index) : -1;
    }
    private void rebuild() {
        if (memory == null) return;
        filtered.clear();
        for (int i = 0; i < memory.types(); i++) {
            ItemStack stack = memory.view(i);
            if (query.isBlank() || matches(stack)) filtered.add(i);
        }
        rowOffset = Math.clamp(rowOffset, 0, Math.max(0, (filtered.size() + MEMORY_COLUMNS - 1) / MEMORY_COLUMNS - MEMORY_ROWS));
        refreshSlots();
        seenRevision = memory.revision();
    }
    private boolean matches(ItemStack stack) {
        String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return name.contains(query) || id.contains(query) || itemMatches.contains(id);
    }
    private void refreshSlots() {
        if (memory == null) return;
        for (int i = 0; i < MEMORY_SIZE; i++) {
            int index = cellIndex(i);
            visible.setItem(i, index < 0 ? ItemStack.EMPTY : memory.view(index).copyWithCount(1));
        }
    }
    public void search(String term, String matchedIds, Player player) {
        if (memory == null || !stillValid(player)) return;
        String cleaned = term == null ? "" : term.toLowerCase(Locale.ROOT).trim();
        query = cleaned.substring(0, Math.min(64, cleaned.length()));
        itemMatches = new HashSet<>();
        if (matchedIds != null) {
            for (String id : matchedIds.split(",", 512)) if (id.length() <= 128) itemMatches.add(id);
        }
        rowOffset = 0;
        rebuild();
        broadcastChanges();
    }
    @Override public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < CollectorOffers.COUNT) {
            if (controller != null) controller.selectOffer(id);
            return true;
        }
        if (id == 2000 || id == 2001 || (id >= 3000 && id <= 4000)) {
            if (memory == null || !stillValid(player)) return false;
            if (id == 2000) rowOffset--;
            else if (id == 2001) rowOffset++;
            else rowOffset = (int)Math.round(Math.max(0, (filtered.size() + MEMORY_COLUMNS - 1) / MEMORY_COLUMNS - MEMORY_ROWS) * ((id - 3000) / 1000.0D));
            rowOffset = Math.clamp(rowOffset, 0, Math.max(0, (filtered.size() + MEMORY_COLUMNS - 1) / MEMORY_COLUMNS - MEMORY_ROWS));
            refreshSlots(); broadcastChanges();
            return true;
        }
        return false;
    }
    @Override public void broadcastChanges() {
        if (memory != null && memory.revision() != seenRevision) rebuild();
        super.broadcastChanges();
    }
    @Override public void slotsChanged(Container container) {
        if (container == craft && memoryReadyNow() && owner instanceof ServerPlayer player) {
            var input = craft.asCraftInput();
            var recipe = player.level().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, player.level());
            ItemStack result = ItemStack.EMPTY;
            if (recipe.isPresent()) {
                RecipeHolder<CraftingRecipe> found = recipe.get();
                result = found.value().assemble(input, player.level().registryAccess());
                craftResult.setRecipeUsed(found);
            } else craftResult.setRecipeUsed(null);
            craftResult.setItem(0, result);
        }
        super.slotsChanged(container);
    }
    /** Amount of a stored item that can actually fit in the player's inventory.
     *  This is computed before removing anything from the persistent memory. */
    private int playerInventoryRoom(ItemStack sample) {
        if (sample.isEmpty()) return 0;
        long room = 0L;
        for (int i = PLAYER_START; i < PLAYER_END; i++) {
            Slot target = slots.get(i);
            if (!target.mayPlace(sample)) continue;
            ItemStack other = target.getItem();
            int limit = Math.min(target.getMaxStackSize(sample), sample.getMaxStackSize());
            if (other.isEmpty()) room += limit;
            else if (ItemStack.isSameItemSameComponents(other, sample))
                room += Math.max(0, limit - other.getCount());
        }
        return (int)Math.min(Integer.MAX_VALUE, room);
    }

    /** Shift-click from a virtual memory cell. Never lose items when the player is full. */
    private ItemStack withdrawMemoryToPlayer(Player player, int slotIndex) {
        if (player.level().isClientSide() || memory == null || !memoryReadyNow() || !stillValid(player))
            return ItemStack.EMPTY;
        int memoryIndex = cellIndex(slotIndex - MEMORY_START);
        if (memoryIndex < 0) return ItemStack.EMPTY;
        ItemStack preview = memory.view(memoryIndex);
        int room = playerInventoryRoom(preview);
        if (room <= 0) return ItemStack.EMPTY;
        ItemStack taken = memory.withdraw(memoryIndex, Math.min(room, preview.getMaxStackSize()));
        if (taken.isEmpty()) return ItemStack.EMPTY;
        ItemStack original = taken.copy();
        // Only normal inventory/hotbar slots are recipients. Never fill craft/furnace/shop.
        moveItemStackTo(taken, PLAYER_START, PLAYER_END, true);
        if (!taken.isEmpty()) memory.deposit(taken); // safety net if a slot changed
        rebuild();
        broadcastChanges();
        return original;
    }

    @Override public void clicked(int slotId, int mouseButton, ClickType clickType, Player player) {
        if (slotId == 1) {
            if (clickType != ClickType.PICKUP || !getCarried().isEmpty()) return;
            mouseButton = 0;
        }
        if (slotId >= MEMORY_START && slotId < CRAFT_START) {
            if (clickType == ClickType.QUICK_MOVE && mouseButton == 0) {
                withdrawMemoryToPlayer(player, slotId);
                return;
            }
            if (memory == null || !memoryReadyNow() || !stillValid(player) || clickType != ClickType.PICKUP) return;
            int index = cellIndex(slotId - MEMORY_START);
            ItemStack held = getCarried();
            if (!held.isEmpty()) {
                int deposited = memory.deposit(held);
                if (deposited > 0) {
                    held.shrink(deposited);
                    setCarried(held.isEmpty() ? ItemStack.EMPTY : held);
                }
            } else if (index >= 0) {
                ItemStack output = memory.withdraw(index, mouseButton == 1 ? 1 : 64);
                if (!output.isEmpty()) setCarried(output);
            }
            rebuild(); broadcastChanges(); return;
        }
        if (slotId >= CRAFT_START && slotId <= SMELT_OUTPUT && controller != null && !memoryReadyNow()) return;
        super.clicked(slotId, mouseButton, clickType, player);
    }
    @Override public boolean stillValid(Player player) { return inventory.stillValid(player); }
    @Override public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) return ItemStack.EMPTY;
        if (slotIndex >= MEMORY_START && slotIndex < CRAFT_START)
            return withdrawMemoryToPlayer(player, slotIndex);
        if (slotIndex == 1) return ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack current = slot.getItem();
        ItemStack original = current.copy();
        if (slotIndex >= PLAYER_START && controller == null && isMemoryReady()) return ItemStack.EMPTY;
        if (slotIndex >= PLAYER_START && memoryReadyNow()) {
            // Keep pre-existing shift-click trading semantics for coins and stress fragments.
            boolean depositedIntoTrade = current.is(MDKCosmetic.CHARGED_POWER_ESSENCE.get())
                    && moveItemStackTo(current, 0, 1, false);
            boolean depositedIntoRecovery = !depositedIntoTrade
                    && (current.is(MDKCosmetic.POWER_ESSENCE.get())
                    || current.is(MDKCosmetic.CHARGED_POWER_ESSENCE.get()))
                    && moveItemStackTo(current, 2, 3, false);
            if (depositedIntoTrade || depositedIntoRecovery) {
                if (current.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
                return original;
            }
            int accepted = memory.deposit(current);
            if (accepted > 0) {
                current.shrink(accepted);
                if (current.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
                rebuild(); return original;
            }
        }
        if (slotIndex == 0 || slotIndex == 2 || (slotIndex >= CRAFT_START && slotIndex <= SMELT_OUTPUT)) {
            if (slotIndex == CRAFT_RESULT && !memoryReadyNow()) return ItemStack.EMPTY;
            if (slotIndex == SMELT_OUTPUT && !memoryReadyNow()) return ItemStack.EMPTY;
            if (!moveItemStackTo(current, PLAYER_START, PLAYER_END, true)) return ItemStack.EMPTY;
            if (slotIndex == CRAFT_RESULT) slot.onQuickCraft(current, original);
        } else if (slotIndex >= PLAYER_START) {
            if (current.is(MDKCosmetic.CHARGED_POWER_ESSENCE.get())) {
                if (!moveItemStackTo(current, 0, 1, false) && !moveItemStackTo(current, 2, 3, false)) return ItemStack.EMPTY;
            } else if (current.is(MDKCosmetic.POWER_ESSENCE.get())) {
                if (!moveItemStackTo(current, 2, 3, false)) return ItemStack.EMPTY;
            } else return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (current.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (original.getCount() == current.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, current);
        return original;
    }
    @Override public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            for (int i = 0; i < craft.getContainerSize(); i++) {
                ItemStack remaining = craft.removeItemNoUpdate(i);
                if (!remaining.isEmpty()) player.getInventory().placeItemBackInInventory(remaining);
            }
        }
    }
}
