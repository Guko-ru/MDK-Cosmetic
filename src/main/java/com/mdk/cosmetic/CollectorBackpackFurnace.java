package com.mdk.cosmetic;

import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import com.mdk.cosmetic.platform.Events.PlayerTickEvent;

/** Server-side portable furnace. Everything, including work in progress, lives on the backpack item. */
public final class CollectorBackpackFurnace {
    private CollectorBackpackFurnace() {}
    public static final String ITEM_KEY = "FurnaceItems";
    private static final String[] DATA_KEYS = {"FurnaceBurn", "FurnaceBurnTotal", "FurnaceProgress", "FurnaceCookTime"};

    public static List<ItemStack> read(ItemStack backpack, HolderLookup.Provider provider) {
        List<ItemStack> values = new java.util.ArrayList<>();
        for (int i = 0; i < 3; i++) values.add(ItemStack.EMPTY);
        ListTag saved = CollectorBackpackData.read(backpack).getList(ITEM_KEY, 10);
        for (int i = 0; i < saved.size(); i++) {
            CompoundTag entry = saved.getCompound(i);
            int slot = entry.getInt("Slot");
            if (slot >= 0 && slot < 3) values.set(slot, ItemStack.parseOptional(provider, entry.getCompound("Stack")));
        }
        return values;
    }

    public static void save(ItemStack backpack, SimpleContainer inventory, HolderLookup.Provider provider) {
        CompoundTag data = CollectorBackpackData.read(backpack);
        ListTag result = new ListTag();
        for (int i = 0; i < 3; i++) {
            ItemStack item = inventory.getItem(i);
            if (item.isEmpty()) continue;
            CompoundTag slot = new CompoundTag();
            slot.putInt("Slot", i);
            slot.put("Stack", item.saveOptional(provider));
            result.add(slot);
        }
        data.put(ITEM_KEY, result);
        CollectorBackpackData.write(backpack, data);
    }

    public static boolean isEmpty(ItemStack backpack, HolderLookup.Provider provider) {
        for (ItemStack item : read(backpack, provider)) if (!item.isEmpty()) return false;
        return true;
    }

    static SimpleContainer trackedInventory(ItemStack backpack, HolderLookup.Provider provider) {
        final boolean[] loading = {true};
        SimpleContainer inventory = new SimpleContainer(3) {
            @Override public void setChanged() {
                super.setChanged();
                if (!loading[0]) save(backpack, this, provider);
            }
        };
        List<ItemStack> saved = read(backpack, provider);
        for (int i = 0; i < 3; i++) inventory.setItem(i, saved.get(i));
        loading[0] = false;
        return inventory;
    }

    static ContainerData data(ItemStack backpack) {
        return new ContainerData() {
            @Override public int get(int index) {
                return index >= 0 && index < DATA_KEYS.length
                        ? CollectorBackpackData.read(backpack).getInt(DATA_KEYS[index]) : 0;
            }
            @Override public void set(int index, int value) {
                if (index < 0 || index >= DATA_KEYS.length) return;
                CompoundTag data = CollectorBackpackData.read(backpack);
                data.putInt(DATA_KEYS[index], value);
                CollectorBackpackData.write(backpack, data);
            }
            @Override public int getCount() { return DATA_KEYS.length; }
        };
    }

    public static FurnaceMenu open(int id, Inventory playerInventory, ItemStack backpack) {
        return new PortableMenu(id, playerInventory, backpack,
                trackedInventory(backpack, playerInventory.player.registryAccess()), data(backpack));
    }

    public static final class PortableMenu extends FurnaceMenu {
        public final ItemStack backpack;
        public final SimpleContainer storage;
        private PortableMenu(int id, Inventory inv, ItemStack backpack, SimpleContainer storage, ContainerData data) {
            super(id, inv, storage, data);
            this.backpack = backpack;
            this.storage = storage;
        }
        @Override public boolean stillValid(net.minecraft.world.entity.player.Player player) {
            return player.getInventory().getItem(player.getInventory().selected) == backpack;
        }
    }

    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        for (int slot = 0; slot <= 36; slot++) {
            boolean equipped = slot == 36;
            ItemStack backpack = equipped ? CollectorEquipmentData.backpack(player)
                    : player.getInventory().getItem(slot);
            if (!(backpack.getItem() instanceof CollectorBackpackItem)) continue;
            if (!CollectorBackpackData.hasUpgradeItem(backpack, MDKCosmetic.COLLECTOR_BACKPACK_FURNACE.get(), player.registryAccess())) continue;
            if (equipped && player.containerMenu instanceof CollectorBackpackMenu open
                    && open.isEquippedBackpack()) backpack = open.backingItem();
            SimpleContainer storage;
            if (player.containerMenu instanceof PortableMenu menu && menu.backpack == backpack) {
                storage = menu.storage;
            } else if (player.containerMenu instanceof CollectorBackpackMenu menu && menu.ownsBackpack(backpack)) {
                storage = menu.furnaceStorage();
            } else {
                storage = trackedInventory(backpack, player.registryAccess());
            }
            cook(player, backpack, storage);
            if (equipped) CollectorEquipmentData.setBackpack(player, backpack);
        }
    }

    private static void cook(ServerPlayer player, ItemStack backpack, SimpleContainer storage) {
        ItemStack input = storage.getItem(0);
        ItemStack fuel = storage.getItem(1);
        ItemStack output = storage.getItem(2);
        CompoundTag state = CollectorBackpackData.read(backpack);
        int burn = Math.max(0, state.getInt(DATA_KEYS[0]));
        int burnTotal = Math.max(0, state.getInt(DATA_KEYS[1]));
        int progress = Math.max(0, state.getInt(DATA_KEYS[2]));
        var match = input.isEmpty() ? java.util.Optional.<net.minecraft.world.item.crafting.RecipeHolder<net.minecraft.world.item.crafting.SmeltingRecipe>>empty()
                : player.serverLevel().recipeAccess().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), player.level());
        ItemStack result = match.isPresent() ? match.get().value().assemble(new SingleRecipeInput(input),player.registryAccess()) : ItemStack.EMPTY;
        boolean possible = !result.isEmpty() && (output.isEmpty() ||
                ItemStack.isSameItemSameComponents(output, result) && output.getCount() + result.getCount() <= output.getMaxStackSize());
        if (burn > 0) burn--;
        if (possible && burn == 0 && !fuel.isEmpty()) {
            int burnTime = com.mdk.cosmetic.platform.FurnaceFuel.burnTime(player.level(),fuel);
            if (burnTime > 0) {
                burn = burnTime;
                burnTotal = burnTime;
                ItemStack remaining = fuel.getRecipeRemainder();
                fuel.shrink(1);
                if (fuel.isEmpty()) storage.setItem(1, remaining);
            }
        }
        if (possible && burn > 0) {
            progress++;
            if (progress >= 200) {
                progress = 0;
                input.shrink(1);
                if (input.isEmpty()) storage.setItem(0, ItemStack.EMPTY);
                if (output.isEmpty()) storage.setItem(2, result.copy());
                else output.grow(result.getCount());
                storage.setChanged();
            }
        } else if (!possible) progress = 0;
        state = CollectorBackpackData.read(backpack);
        state.putInt(DATA_KEYS[0], burn);
        state.putInt(DATA_KEYS[1], burnTotal);
        state.putInt(DATA_KEYS[2], progress);
        state.putInt(DATA_KEYS[3], 200);
        CollectorBackpackData.write(backpack, state);
    }
}
