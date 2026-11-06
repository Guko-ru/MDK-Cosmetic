package com.mdk.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** One-block 64-unit essence reservoir with persistent input/output inventory. */
public final class EssenceWorktableBlockEntity extends BaseContainerBlockEntity {
    public static final int COST = 64;
    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private int progress;
    private int tickCounter;

    public final ContainerData data = new ContainerData() {
        @Override public int get(int index) { return index == 0 ? progress : 0; }
        @Override public void set(int index, int value) { if (index == 0) progress = Math.clamp(value, 0, COST - 1); }
        @Override public int getCount() { return 1; }
    };

    public EssenceWorktableBlockEntity(BlockPos pos, BlockState state) {
        super(MDKCosmetic.ESSENCE_WORKTABLE_BE.get(), pos, state);
    }

    @Override public int getContainerSize() { return 2; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> newItems) { items = newItems; }
    @Override protected Component getDefaultName() {
        return Component.translatable("menu.mdkcosmetic.essence_worktable");
    }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new EssenceWorktableMenu(id, inventory, this, data);
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && stack.is(MDKCosmetic.POWER_ESSENCE.get());
    }

    @Override protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput mdk$output) {
        CompoundTag tag=new CompoundTag();
        var registries=(getLevel()==null?net.minecraft.core.RegistryAccess.EMPTY:getLevel().registryAccess());

        super.saveAdditional(mdk$output);
        com.mdk.cosmetic.platform.InventoryNbt.saveAllItems(tag, items, registries);
        tag.putInt("EssenceProgress", progress);
    
        com.mdk.cosmetic.platform.LegacyValues.write(mdk$output,tag);
    }
    @Override protected void loadAdditional(net.minecraft.world.level.storage.ValueInput mdk$input) {
        CompoundTag tag=com.mdk.cosmetic.platform.LegacyValues.read(mdk$input);
        var registries=mdk$input.lookup();

        super.loadAdditional(mdk$input);
        items = NonNullList.withSize(2, ItemStack.EMPTY);
        com.mdk.cosmetic.platform.InventoryNbt.loadAllItems(tag, items, registries);
        progress = Math.clamp(tag.getIntOr("EssenceProgress",0), 0, COST - 1);
        }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EssenceWorktableBlockEntity table) {
        if (level.isClientSide() || (++table.tickCounter % 3) != 0) return;
        ItemStack input = table.getItem(0);
        ItemStack output = table.getItem(1);
        if (input.isEmpty() || !input.is(MDKCosmetic.POWER_ESSENCE.get())) return;
        if (output.isEmpty() || (output.is(MDKCosmetic.CHARGED_POWER_ESSENCE.get()) && output.getCount() < output.getMaxStackSize())) {
            input.shrink(1);
            table.progress++;
            if (table.progress >= COST) {
                table.progress = 0;
                if (output.isEmpty()) table.setItem(1, new ItemStack(MDKCosmetic.CHARGED_POWER_ESSENCE.get()));
                else output.grow(1);
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.65F, 1.1F);
            }
            table.setChanged();
            // Menu synchronizes data via ContainerData; dirty flag persists fractional progress to NBT.
        }
    }
    @Override public void preRemoveSideEffects(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState next){
        if(level!=null&&!level.isClientSide()){level.updateNeighbourForOutputSignal(pos,getBlockState().getBlock());}
        super.preRemoveSideEffects(pos,next);
    }
}
