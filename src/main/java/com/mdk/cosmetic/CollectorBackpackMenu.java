package com.mdk.cosmetic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.server.level.ServerLevel;


/** Portable backpack, or compact remote terminal backed by the existing persistent memory core. */
public final class CollectorBackpackMenu extends AbstractContainerMenu {
    private static final int CAPACITY = 45;
    private static final int UPGRADE_START = 45;
    private static final int UPGRADE_COUNT = 5;
    private static final int FILTER_START = UPGRADE_START + UPGRADE_COUNT;
    private static final int CRAFT_START = FILTER_START + 9;
    private static final int CRAFT_RESULT = CRAFT_START + 9;
    private static final int FURNACE_START = CRAFT_RESULT + 1;
    private static final int PLAYER_START = FURNACE_START + 3;

    private final Inventory playerInventory;
    private final boolean equipped;
    private final ItemStack backpack;
    private final boolean remote;
    private final CollectorMemoryCoreBlockEntity memory;
    private final SimpleContainer display = new SimpleContainer(CAPACITY);
    private final SimpleContainer upgradeSlots;
    private final SimpleContainer ghostFilter = new SimpleContainer(9);
    private final SimpleContainer contents;
    private final ContainerData synced;
    private final List<Integer> filtered = new ArrayList<>();
    private String query = "";
    private Set<String> matches = new HashSet<>();
    private int page;
    private boolean remoteSorted;
    private long lastRevision = -1L;
    private boolean loading = true;
    private final TransientCraftingContainer crafting = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer craftingResult = new ResultContainer();
    private final SimpleContainer portableFurnace;
    private final ContainerData furnaceData;

    public SimpleContainer furnaceStorage() { return portableFurnace; }
    public boolean ownsBackpack(ItemStack item) {
        return backpack == item || equipped && ItemStack.isSameItemSameComponents(backpack,item);
    }
    public boolean isEquippedBackpack() { return equipped; }
    public ItemStack backingItem() { return backpack; }
    public boolean furnaceInstalled() {
        return CollectorBackpackData.hasUpgradeItem(backpack, MDKCosmetic.COLLECTOR_BACKPACK_FURNACE.get(), playerInventory.player.registryAccess());
    }
    private boolean hasSelectedModule(net.minecraft.world.item.Item item) {
        return activeModule >= 0 && activeModule < UPGRADE_COUNT && upgradeSlots.getItem(activeModule).is(item);
    }
    public boolean craftingVisible() { return activeModule() >= 0 && moduleAt(activeModule()).is(MDKCosmetic.COLLECTOR_BACKPACK_CRAFTING.get()); }
    public boolean furnaceVisible() { return activeModule() >= 0 && moduleAt(activeModule()).is(MDKCosmetic.COLLECTOR_BACKPACK_FURNACE.get()); }
    public int furnaceValue(int index) { return index >= 0 && index < 4 ? furnaceData.get(index) : 0; }

    private int activeModule = -1;
    private int filterPage = 0;


    public CollectorBackpackMenu(int id, Inventory inv) { this(id, inv, false); }
    public CollectorBackpackMenu(int id, Inventory inv, boolean equipped) {
        super(equipped ? MDKCosmetic.COLLECTOR_EQUIPPED_BACKPACK_MENU.get()
                : MDKCosmetic.COLLECTOR_BACKPACK_MENU.get(), id);
        this.equipped = equipped;
        this.playerInventory = inv;
        backpack = equipped ? CollectorEquipmentData.backpack(inv.player) : inv.getItem(inv.getSelectedSlot());
        remote = CollectorBackpackData.linked(backpack);
        memory = remote && com.mdk.cosmetic.platform.ServerAccess.get(inv.player) != null ? CollectorBackpackData.remoteCore(backpack, com.mdk.cosmetic.platform.ServerAccess.get(inv.player)) : null;
        contents = new SimpleContainer(CAPACITY) {
            @Override public void setChanged() {
                super.setChanged();
                if (!loading && !remote && !backpack.isEmpty()) saveAllBackpackData();
            }
        };
        upgradeSlots = new SimpleContainer(UPGRADE_COUNT) {
            @Override public void setChanged() {
                super.setChanged();
                if (!loading && !backpack.isEmpty()) saveAllBackpackData();
            }
        };
        if (backpack.getItem() instanceof CollectorBackpackItem) {
            if (!remote) {
                List<ItemStack> loaded = CollectorBackpackData.items(backpack, inv.player.registryAccess());
                for (int i = 0; i < Math.min(loaded.size(), CAPACITY); i++) contents.setItem(i, loaded.get(i));
            }
            List<ItemStack> upgrades = CollectorBackpackData.upgradeItems(backpack, inv.player.registryAccess());
            for (int i = 0; i < Math.min(upgrades.size(), UPGRADE_COUNT); i++) upgradeSlots.setItem(i, upgrades.get(i));
            refreshFilterSlots();
        }
        portableFurnace = CollectorBackpackFurnace.trackedInventory(backpack, inv.player.registryAccess());
        furnaceData = inv.player.level().isClientSide()
                ? new SimpleContainerData(4)
                : CollectorBackpackFurnace.data(backpack);
        loading = false;

        for (int i = 0; i < CAPACITY; i++) {
            final int index = i;
            addSlot(new Slot(remote ? display : contents, i, 8 + (i % 9) * 18, 34 + (i / 9) * 18) {
                @Override public boolean mayPlace(ItemStack stack) {
                    if (remote || index >= CollectorBackpackData.capacity(backpack)) return false;
                    if (stack.getItem() instanceof CollectorBackpackItem) return false;
                    if (isModuleItem(stack)) return false;
                    return !(stack.getItem() instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock);
                }
                @Override public boolean mayPickup(Player player) {
                    return remote ? index < 27 && !display.getItem(index).isEmpty() : index < CollectorBackpackData.capacity(backpack);
                }
            });
        }

        for (int i = 0; i < UPGRADE_COUNT; i++) {
            final int index = i;
            addSlot(new Slot(upgradeSlots, i, 190, 34 + i * 22) {
                @Override public boolean mayPlace(ItemStack stack) {
                    return isModuleItem(stack) && !moduleAlreadyInstalled(stack, index);
                }
                @Override public boolean mayPickup(Player player) {
                    return !getItem().is(MDKCosmetic.COLLECTOR_BACKPACK_FURNACE.get())
                        || CollectorBackpackFurnace.isEmpty(backpack, player.registryAccess());
                }
            });
        }
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(ghostFilter, i, 305 + (i % 3) * 18, 56 + (i / 3) * 18) {
                @Override public boolean isActive() {
                    int active = activeModule();
                    return active >= 0 && active < UPGRADE_COUNT
                            && upgradeSlots.getItem(active).is(MDKCosmetic.COLLECTOR_BACKPACK_MAGNET.get());
                }
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player player) { return false; }
            });
        }

        // Embedded crafting and furnace slots belong to this menu, not a second window.
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(crafting, i, 290 + (i % 3) * 18, 62 + (i / 3) * 18) {
                @Override public boolean isActive() { return craftingVisible(); }
                @Override public boolean mayPlace(ItemStack stack) { return craftingVisible() && !(stack.getItem() instanceof CollectorBackpackItem); }
                @Override public boolean mayPickup(Player player) { return craftingVisible(); }
            });
        }
        addSlot(new ResultSlot(inv.player, crafting, craftingResult, 0, 394, 81) {
            @Override public boolean isActive() { return craftingVisible(); }
            @Override public boolean mayPickup(Player player) { return craftingVisible() && super.mayPickup(player); }
        });
        addSlot(new Slot(portableFurnace, 0, 294, 80) {
            @Override public boolean isActive() { return furnaceVisible(); }
            @Override public boolean mayPlace(ItemStack stack) { return furnaceVisible() && !(stack.getItem() instanceof CollectorBackpackItem); }
            @Override public boolean mayPickup(Player player) { return furnaceVisible(); }
        });
        addSlot(new Slot(portableFurnace, 1, 294, 119) {
            @Override public boolean isActive() { return furnaceVisible(); }
            @Override public boolean mayPlace(ItemStack stack) { return furnaceVisible() && com.mdk.cosmetic.platform.FurnaceFuel.burnTime(playerInventory.player.level(),stack) > 0; }
            @Override public boolean mayPickup(Player player) { return furnaceVisible(); }
        });
        addSlot(new Slot(portableFurnace, 2, 391, 80) {
            @Override public boolean isActive() { return furnaceVisible(); }
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) { return furnaceVisible(); }
        });

        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) {
            int slot = 9 + row * 9 + col;
            addSlot(new Slot(inv, slot, 8 + col * 18, 150 + row * 18));
        }
        for (int col = 0; col < 9; col++) {
            final int index = col;
            addSlot(new Slot(inv, col, 8 + col * 18, 208) {
                @Override public boolean mayPickup(Player player) { return equipped || index != player.getInventory().getSelectedSlot(); }
                @Override public boolean mayPlace(ItemStack stack) { return equipped || index != playerInventory.getSelectedSlot(); }
            });
        }

        synced = inv.player.level().isClientSide() ? new SimpleContainerData(14 + 27 * 4) : new ContainerData() {
            @Override public int get(int index) {
                if (index == 0) return remote ? 1 : 0;
                if (index == 1) return page;
                if (index == 2) return Math.max(1, (filtered.size() + 26) / 27);
                if (index == 3) return CollectorBackpackData.whitelist(backpack) ? 1 : 0;
                if (index == 4) return CollectorBackpackData.hasFilter(backpack, playerInventory.player.registryAccess()) ? 1 : 0;
                if (index == 5) return CollectorBackpackData.hasMagnet(backpack, playerInventory.player.registryAccess()) ? 1 : 0;
                if (index == 6) return CollectorBackpackData.capacity(backpack);
                if (index == 7) return CollectorBackpackData.read(backpack).getBooleanOr("Exact",false) ? 1 : 0;
                if (index == 8) return CollectorBackpackData.magnetEnabled(backpack) ? 1 : 0;
                if (index == 9) return activeModule;
                if (index == 10) return filterPage;
                if (index == 11) return filterPages();
                if (index == 12) return CollectorBackpackData.totemEnabled(backpack) ? 1 : 0;
                if (index == 13) return CollectorBackpackData.deathVaultEnabled(backpack) ? 1 : 0;
                int cell = (index - 14) / 4;
                if (cell < 0 || cell >= 27 || memory == null) return 0;
                int at = page * 27 + cell;
                long count = at < filtered.size() ? memory.countAt(filtered.get(at)) : 0L;
                return (int) ((count >>> (((index - 14) % 4) * 16)) & 0xFFFFL);
            }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return 14 + 27 * 4; }
        };
        if (inv.player.level().isClientSide()) {
            synced.set(0, remote ? 1 : 0);
            synced.set(6, CollectorBackpackData.capacity(backpack));
        }
        addDataSlots(synced);
        addDataSlots(furnaceData);
        if (memory != null) rebuild();
    }

    private void saveAllBackpackData() {
        if (!remote) {
            List<ItemStack> stored = new ArrayList<>();
            for (int i = 0; i < CAPACITY; i++) stored.add(contents.getItem(i).copy());
            CollectorBackpackData.saveItems(backpack, stored, playerInventory.player.registryAccess());
        }
        List<ItemStack> upgrades = new ArrayList<>();
        for (int i = 0; i < UPGRADE_COUNT; i++) upgrades.add(upgradeSlots.getItem(i).copy());
        CollectorBackpackData.saveUpgradeItems(backpack, upgrades, playerInventory.player.registryAccess());
        persistEquippedBackpack();
    }

    private void persistEquippedBackpack() {
        if (equipped && !playerInventory.player.level().isClientSide())
            CollectorEquipmentData.setBackpack(playerInventory.player, backpack);
    }

    public boolean isRemote() { return synced.get(0) != 0; }
    public int getPage() { return synced.get(1); }
    public int getPages() { return Math.max(1, synced.get(2)); }
    public boolean whitelist() { return synced.get(3) != 0; }
    public boolean hasFilter() { return synced.get(4) != 0; }
    public boolean hasMagnet() { return synced.get(5) != 0; }
    public int capacity() { return synced.get(6); }
    public boolean exact() { return synced.get(7) != 0; }
    public boolean magnetEnabled() { return synced.get(8) != 0; }
    public boolean canUpgrade() { return !remote && backpack.is(MDKCosmetic.COLLECTOR_BACKPACK_BASIC.get()); }
    public int activeModule() { return synced.get(9); }
    public int filterPage() { return synced.get(10); }
    public boolean totemEnabled() { return synced.get(12) != 0; }
    public boolean deathVaultEnabled() { return synced.get(13) != 0; }
    public int filterPagesShown() { return Math.max(1, synced.get(11)); }
    public ItemStack moduleAt(int index) { return index >= 0 && index < UPGRADE_COUNT ? upgradeSlots.getItem(index) : ItemStack.EMPTY; }
    private boolean moduleAlreadyInstalled(ItemStack item, int exclude) {
        for (int i = 0; i < UPGRADE_COUNT; i++) {
            if (i != exclude && !upgradeSlots.getItem(i).isEmpty() && upgradeSlots.getItem(i).is(item.getItem())) return true;
        }
        return false;
    }
    public static boolean isModuleItem(ItemStack item) {
        return item.is(MDKCosmetic.COLLECTOR_BACKPACK_MAGNET.get())
            || item.is(MDKCosmetic.COLLECTOR_BACKPACK_CRAFTING.get())
            || item.is(MDKCosmetic.COLLECTOR_BACKPACK_FURNACE.get())
            || item.is(MDKCosmetic.COLLECTOR_BACKPACK_TOTEM.get())
            || item.is(MDKCosmetic.COLLECTOR_BACKPACK_DEATH_VAULT.get());
    }
    private void refreshFilterSlots() {
        List<ItemStack> items = CollectorBackpackData.filterItems(backpack, playerInventory.player.registryAccess());
        for (int i = 0; i < 9; i++) ghostFilter.setItem(i, items.get(filterPage * 9 + i));
    }
    private int filterPages() {
        List<ItemStack> list = CollectorBackpackData.filterItems(backpack, playerInventory.player.registryAccess());
        int highest = -1;
        for (int i = 0; i < list.size(); i++) if (!list.get(i).isEmpty()) highest = i;
        return Math.clamp((highest + 9) / 9 + (highest % 9 == 8 ? 1 : 0), 1, 6);
    }


    public long quantity(int cell) {
        if (cell < 0 || cell >= 27) return 0L;
        long n = 0L;
        for (int i = 0; i < 4; i++) n |= ((long) synced.get(14 + cell * 4 + i) & 65535L) << (i * 16);
        return n;
    }

    private boolean matches(ItemStack stack) {
        if (query.isBlank()) return true;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return id.contains(query) || stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(query) || matches.contains(id);
    }

    private void rebuild() {
        if (memory == null) return;
        filtered.clear();
        for (int i = 0; i < memory.types(); i++) if (matches(memory.view(i))) filtered.add(i);
        if (remoteSorted) filtered.sort(Comparator.comparing(i -> BuiltInRegistries.ITEM.getKey(memory.view(i).getItem()).toString()));
        page = Math.clamp(page, 0, Math.max(0, (filtered.size() - 1) / 27));
        for (int i = 0; i < 27; i++) {
            int at = page * 27 + i;
            display.setItem(i, at < filtered.size() ? memory.view(filtered.get(at)).copyWithCount(1) : ItemStack.EMPTY);
        }
        lastRevision = memory.revision();
    }

    public void search(String value, String ids, Player player) {
        if (!remote || memory == null || !stillValid(player)) return;
        String lower = value.toLowerCase(Locale.ROOT).trim();
        query = lower.substring(0, Math.min(64, lower.length()));
        matches.clear();
        for (String id : ids.split(",", 512)) if (id.length() < 130) matches.add(id);
        page = 0;
        rebuild();
        broadcastChanges();
    }

    @Override public void slotsChanged(net.minecraft.world.Container container) {
        if (container == crafting && playerInventory != null && playerInventory.player.level() instanceof ServerLevel level) {
            CraftingInput input = crafting.asCraftInput();
            ItemStack output = ((net.minecraft.server.level.ServerLevel)level).recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, level)
                    .map(r -> r.value().assemble(input)).orElse(ItemStack.EMPTY);
            craftingResult.setItem(0, output);
        }
        super.slotsChanged(container);
    }

    @Override public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            for (int i = 0; i < 9; i++) {
                ItemStack item = crafting.removeItemNoUpdate(i);
                if (!item.isEmpty() && !player.getInventory().add(item)) player.drop(item, false, net.minecraft.util.Prediction.SERVER_ONLY);
            }
        }
    }

    @Override public void broadcastChanges() {
        persistEquippedBackpack();
        if (memory != null && memory.revision() != lastRevision) rebuild();
        super.broadcastChanges();
    }

    @Override public boolean clickMenuButton(Player player, int button) {
        if (!stillValid(player)) return false;
        if (button >= 3000 && button < 3000 + UPGRADE_COUNT) {
            int index = button - 3000;
            if (upgradeSlots.getItem(index).isEmpty()) return false;
            activeModule = activeModule == index ? -1 : index;
            filterPage = 0;
            refreshFilterSlots();
            broadcastChanges();
            return true;
        }
        if (button == 5003 && activeModule >= 0 && upgradeSlots.getItem(activeModule).is(MDKCosmetic.COLLECTOR_BACKPACK_DEATH_VAULT.get())) {
            CollectorBackpackData.flag(backpack, "DeathVaultDisabled", CollectorBackpackData.deathVaultEnabled(backpack));
            broadcastChanges();
            return true;
        }
        if (button == 5002 && activeModule >= 0 && upgradeSlots.getItem(activeModule).is(MDKCosmetic.COLLECTOR_BACKPACK_TOTEM.get())) {
            CollectorBackpackData.flag(backpack, "TotemDisabled", CollectorBackpackData.totemEnabled(backpack));
            broadcastChanges();
            return true;
        }
        if (button >= 4000 && button < 4006) {
            if (activeModule < 0 || !upgradeSlots.getItem(activeModule).is(MDKCosmetic.COLLECTOR_BACKPACK_MAGNET.get())) return false;
            filterPage = Math.clamp(button - 4000, 0, filterPages() - 1);
            refreshFilterSlots();
            broadcastChanges();
            return true;
        }
        if (button >= 2 && button <= 4 && hasMagnet()) {
            if (button == 2) CollectorBackpackData.flag(backpack, "Whitelist", !CollectorBackpackData.whitelist(backpack));
            if (button == 3) CollectorBackpackData.flag(backpack, "MagnetEnabled", !CollectorBackpackData.magnetEnabled(backpack));
            if (button == 4) CollectorBackpackData.flag(backpack, "Exact", !CollectorBackpackData.read(backpack).getBooleanOr("Exact",false));
            broadcastChanges();
            return true;
        }
        if (remote) {
            if (button == 5) {
                remoteSorted = !remoteSorted;
                rebuild();
                broadcastChanges();
                return true;
            }
            if (button >= 1000 && button < 2000) {
                page = Math.clamp(button - 1000, 0, Math.max(0, (filtered.size() - 1) / 27));
            } else {
                if (button != 0 && button != 1) return false;
                page = Math.clamp(page + (button == 0 ? -1 : 1), 0, Math.max(0, (filtered.size() - 1) / 27));
            }
            rebuild();
            broadcastChanges();
            return true;
        }
        if (button == 6 && canUpgrade()) {
            return performUpgrade(player);
        }
        if (button == 5) {
            List<ItemStack> stacks = new ArrayList<>();
            for (int i = 0; i < CollectorBackpackData.capacity(backpack); i++) if (!contents.getItem(i).isEmpty()) stacks.add(contents.getItem(i).copy());
            stacks.sort(Comparator.comparing(v -> BuiltInRegistries.ITEM.getKey(v.getItem()).toString()));
            for (int i = 0; i < CollectorBackpackData.capacity(backpack); i++) contents.setItem(i, i < stacks.size() ? stacks.get(i) : ItemStack.EMPTY);
            contents.setChanged();
            broadcastChanges();
            return true;
        }
        return false;
    }

    private boolean performUpgrade(Player player) {
        if (!canUpgrade()) return false;
        int shardCount = totalCount(MDKCosmetic.POWER_ESSENCE.get());
        int coinCount = totalCount(MDKCosmetic.CHARGED_POWER_ESSENCE.get());
        if (shardCount < 32 || coinCount < 1) return false;
        consume(MDKCosmetic.POWER_ESSENCE.get(), 32);
        consume(MDKCosmetic.CHARGED_POWER_ESSENCE.get(), 1);
        ItemStack upgraded = new ItemStack(MDKCosmetic.COLLECTOR_BACKPACK_REINFORCED.get());
        if (backpack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            upgraded.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, backpack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA));
        }
        List<ItemStack> stored = new ArrayList<>();
        for (int i = 0; i < CAPACITY; i++) stored.add(contents.getItem(i).copy());
        CollectorBackpackData.saveItems(upgraded, stored, player.registryAccess());
        List<ItemStack> upgrades = new ArrayList<>();
        for (int i = 0; i < UPGRADE_COUNT; i++) upgrades.add(upgradeSlots.getItem(i).copy());
        CollectorBackpackData.saveUpgradeItems(upgraded, upgrades, player.registryAccess());
        // Filter configuration already lives in CUSTOM_DATA and is carried across the upgrade.
        if (equipped) CollectorEquipmentData.setBackpack(player, upgraded);
        else player.getInventory().setItem(player.getInventory().getSelectedSlot(), upgraded);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.closeContainer();
            serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, p) -> new CollectorBackpackMenu(id, inventory, equipped),
                    Component.translatable("menu.mdkcosmetic.collector_backpack")));
            CollectorAdvancementEvents.onBackpackUpgrade(serverPlayer);
            serverPlayer.sendSystemMessage(Component.translatable("message.mdkcosmetic.backpack_upgraded"));
        }
        return true;
    }

    private int totalCount(Item item) {
        int total = 0;
        for (int i = 0; i < CollectorBackpackData.capacity(backpack); i++) {
            ItemStack stack = contents.getItem(i);
            if (stack.is(item)) total += stack.getCount();
        }
        for (int i = 0; i < 36; i++) {
            ItemStack stack = playerInventory.getItem(i);
            if (stack.is(item)) total += stack.getCount();
        }
        return total;
    }

    private void consume(Item item, int amount) {
        for (int i = 0; i < CollectorBackpackData.capacity(backpack) && amount > 0; i++) {
            ItemStack stack = contents.getItem(i);
            if (!stack.is(item)) continue;
            int used = Math.min(stack.getCount(), amount);
            stack.shrink(used);
            amount -= used;
            if (stack.isEmpty()) contents.setItem(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < 36 && amount > 0; i++) {
            ItemStack stack = playerInventory.getItem(i);
            if (!stack.is(item)) continue;
            int used = Math.min(stack.getCount(), amount);
            stack.shrink(used);
            amount -= used;
            if (stack.isEmpty()) playerInventory.setItem(i, ItemStack.EMPTY);
        }
        contents.setChanged();
    }

    private void sendToPlayer(Player player, int cell, int amount) {
        int at = page * 27 + cell;
        if (memory == null || at < 0 || at >= filtered.size()) return;
        ItemStack sample = memory.view(filtered.get(at));
        int room = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack target = player.getInventory().getItem(i);
            if (target.isEmpty()) room += sample.getMaxStackSize();
            else if (ItemStack.isSameItemSameComponents(target, sample)) room += Math.max(0, sample.getMaxStackSize() - target.getCount());
        }
        if (room == 0) return;
        ItemStack taken = memory.withdraw(filtered.get(at), Math.min(amount, room));
        if (taken.isEmpty()) return;
        player.getInventory().add(taken);
        if (!taken.isEmpty()) memory.deposit(taken);
        rebuild();
    }

    @Override public void clicked(int slot, int button, ContainerInput click, Player player) {
        if (slot >= 0 && slot < 27 && remote) {
            if (memory == null || !stillValid(player)) return;
            if (click == ContainerInput.QUICK_MOVE) { sendToPlayer(player, slot, 64); return; }
            if (click == ContainerInput.PICKUP) {
                ItemStack carried = getCarried();
                if (carried.isEmpty()) {
                    int at = page * 27 + slot;
                    if (at < filtered.size()) setCarried(memory.withdraw(filtered.get(at), button == 1 ? 1 : 64));
                } else {
                    if (carried.getItem() instanceof CollectorBackpackItem) return;
                    int accepted = memory.deposit(carried);
                    if (accepted > 0) {
                        carried.shrink(accepted);
                        setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
                    }
                }
                rebuild();
            }
            return;
        }
        if (slot >= 27 && slot < 45 && remote) return;
        if (slot >= FILTER_START && slot < FILTER_START + 9) {
            if (activeModule < 0 || !upgradeSlots.getItem(activeModule).is(MDKCosmetic.COLLECTOR_BACKPACK_MAGNET.get())) return;
            ItemStack carried = getCarried();
            int index = filterPage * 9 + slot - FILTER_START;
            CollectorBackpackData.setFilterItem(backpack, index,
                    button == 1 ? ItemStack.EMPTY : carried, player.registryAccess());
            refreshFilterSlots();
            persistEquippedBackpack();
            broadcastChanges();
            return;
        }
        super.clicked(slot, button, click, player);
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !stillValid(player)) return ItemStack.EMPTY;
        if (remote) {
            if (index < 27) { sendToPlayer(player, index, 64); return ItemStack.EMPTY; }
            if (index < UPGRADE_START) return ItemStack.EMPTY;
            if (index >= FILTER_START && index < PLAYER_START) {
            Slot special = slots.get(index);
            if (!special.isActive() || !special.mayPickup(player) || !special.hasItem()) return ItemStack.EMPTY;
            if (index == CRAFT_RESULT) {
                ItemStack crafted = special.getItem().copy();
                if (!moveItemStackTo(crafted, PLAYER_START, slots.size(), true)) return ItemStack.EMPTY;
                special.onTake(player, crafted);
                return crafted;
            }
            ItemStack stack = special.getItem();
            ItemStack copy = stack.copy();
            if (!moveItemStackTo(stack, PLAYER_START, slots.size(), true)) return ItemStack.EMPTY;
            if (stack.isEmpty()) special.set(ItemStack.EMPTY); else special.setChanged();
            return copy;
        }
            Slot slot = slots.get(index);
            if (!slot.hasItem()) return ItemStack.EMPTY;
            ItemStack stack = slot.getItem(), copy = stack.copy();
            if (index >= UPGRADE_START && index < FILTER_START) {
                if (!slot.mayPickup(player) || !moveItemStackTo(stack, PLAYER_START, slots.size(), true)) return ItemStack.EMPTY;
                if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
                upgradeSlots.setChanged();
                return copy;
            }
            if (isModuleItem(stack)) {
                if (moduleAlreadyInstalled(stack, -1) || !moveItemStackTo(stack, UPGRADE_START, FILTER_START, false)) return ItemStack.EMPTY;
                if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
                upgradeSlots.setChanged();
                return copy;
            }
            if (memory == null || stack.getItem() instanceof CollectorBackpackItem) return ItemStack.EMPTY;
            int accepted = memory.deposit(stack);
            if (accepted == 0) return ItemStack.EMPTY;
            stack.shrink(accepted);
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
            rebuild();
            return copy;
        }
        if (index >= FILTER_START && index < PLAYER_START) {
            Slot special = slots.get(index);
            if (!special.isActive() || !special.mayPickup(player) || !special.hasItem()) return ItemStack.EMPTY;
            if (index == CRAFT_RESULT) {
                ItemStack crafted = special.getItem().copy();
                if (!moveItemStackTo(crafted, PLAYER_START, slots.size(), true)) return ItemStack.EMPTY;
                special.onTake(player, crafted);
                return crafted;
            }
            ItemStack stack = special.getItem();
            ItemStack copy = stack.copy();
            if (!moveItemStackTo(stack, PLAYER_START, slots.size(), true)) return ItemStack.EMPTY;
            if (stack.isEmpty()) special.set(ItemStack.EMPTY); else special.setChanged();
            return copy;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack moving = slot.getItem(), original = moving.copy();
        if (index < CAPACITY || (index >= UPGRADE_START && index < FILTER_START)) {
            if (!slot.mayPickup(player) || !moveItemStackTo(moving, PLAYER_START, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (moving.getItem() instanceof CollectorBackpackItem) return ItemStack.EMPTY;
            if (isModuleItem(moving)) {
                if (moduleAlreadyInstalled(moving, -1) || !moveItemStackTo(moving, UPGRADE_START, FILTER_START, false)) return ItemStack.EMPTY;
            } else {
                if (!moveItemStackTo(moving, 0, CollectorBackpackData.capacity(backpack), false)) return ItemStack.EMPTY;
            }
        }
        if (moving.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        contents.setChanged();
        upgradeSlots.setChanged();
        return original;
    }

    @Override public boolean stillValid(Player player) {
        // The client only has a cosmetic copy of the equipped stack; the server validates ownership.
        if (player.level().isClientSide()) return true;
        if (equipped) {
            if (!player.level().isClientSide() && !ItemStack.isSameItemSameComponents(
                    CollectorEquipmentData.backpack(player), backpack)) return false;
        } else if (player.getInventory().getItem(player.getInventory().getSelectedSlot()) != backpack) return false;
        if (!remote) return true;
        return com.mdk.cosmetic.platform.ServerAccess.get(player) != null && memory != null && CollectorBackpackData.remoteCore(backpack, com.mdk.cosmetic.platform.ServerAccess.get(player)) == memory;
    }
}
