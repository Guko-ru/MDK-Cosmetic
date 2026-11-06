package com.mdk.cosmetic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Persistent catalogue of arbitrary item types + 3-second, fuel-free smelting. */
public final class CollectorMemoryCoreBlockEntity extends BlockEntity implements Container {
    private static final long MAX_PER_TYPE=Long.MAX_VALUE / 8;
    public static final int SMELT_TICKS=60; // 3 seconds at 20 server ticks per second
    private static final class Stored {
        ItemStack template;
        long count;
        Stored(ItemStack template,long count) {this.template=template;this.count=count;}
    }
    private final List<Stored> stored=new ArrayList<>();
    private NonNullList<ItemStack> furnace=NonNullList.withSize(3,ItemStack.EMPTY);
    private int litTime,litDuration,progress;
    private long revision;
    private UUID identity = UUID.randomUUID();
    public UUID identity() { return identity; }
    public long revision() { return revision; }
    @Override public void setChanged() { super.setChanged(); revision++; }
    public final ContainerData furnaceData=new ContainerData() {
        @Override public int get(int i) {return switch(i) {
            case 0->litTime;case 1->litDuration;case 2->progress;case 3->SMELT_TICKS;default->0;};}
        @Override public void set(int i,int v) {switch(i) {
            case 0->litTime=v;case 1->litDuration=v;case 2->progress=v;default->{}}}
        @Override public int getCount() {return 4;}
    };
    public CollectorMemoryCoreBlockEntity(BlockPos pos, BlockState state) {
        super(MDKCosmetic.COLLECTOR_MEMORY_CORE_BE.get(),pos,state);
    }
    public int types() { return stored.size(); }
    public int pages() { return Math.max(1,(types()+26)/27); }
    public long countAt(int index) {return index>=0 && index<types()?stored.get(index).count:0;}
    public ItemStack view(int index) {
        if(index<0||index>=types())return ItemStack.EMPTY;
        Stored e=stored.get(index);
        return e.template.copyWithCount((int)Math.min(e.count,e.template.getMaxStackSize()));
    }
    private int lookup(ItemStack stack) {
        for(int i=0;i<stored.size();i++)if(ItemStack.isSameItemSameComponents(stored.get(i).template,stack))return i;
        return -1;
    }
    /** All mutations are server-only. Returns amount accepted. */
    public boolean canDeposit(ItemStack input) {
        if(input.isEmpty())return true;
        int index=lookup(input);
        long storedCount=index<0?0:stored.get(index).count;
        return storedCount <= MAX_PER_TYPE-input.getCount();
    }
    /** Validates every stack before mutation, then commits a backpack migration as one operation. */
    public boolean depositBatch(List<ItemStack> stacks) {
        if(level==null || level.isClientSide())return false;
        for(int i=0;i<stacks.size();i++) {
            ItemStack item=stacks.get(i);
            if(item.isEmpty())continue;
            long sum=0;
            for(int j=0;j<stacks.size();j++) {
                ItemStack other=stacks.get(j);
                if(!other.isEmpty() && ItemStack.isSameItemSameComponents(item,other))sum+=other.getCount();
            }
            int index=lookup(item);
            long existing=index<0?0:stored.get(index).count;
            if(sum>MAX_PER_TYPE-existing)return false;
        }
        for(ItemStack stack:stacks) {
            if(stack.isEmpty())continue;
            int index=lookup(stack);
            if(index<0)stored.add(new Stored(stack.copyWithCount(1),stack.getCount()));
            else stored.get(index).count+=stack.getCount();
        }
        setChanged();
        return true;
    }
    public int deposit(ItemStack input) {
        if(input.isEmpty() || level==null || level.isClientSide())return 0;
        int i=lookup(input);
        long current=i<0?0:stored.get(i).count;
        int accepted=(int)Math.min(input.getCount(),MAX_PER_TYPE-current);
        if(accepted<=0)return 0;
        if(i<0)stored.add(new Stored(input.copyWithCount(1),accepted));
        else stored.get(i).count+=accepted;
        setChanged();return accepted;
    }
    public ItemStack withdraw(int index,int requested) {
        if(level==null||level.isClientSide()||index<0||index>=stored.size())return ItemStack.EMPTY;
        Stored e=stored.get(index);
        int amount=(int)Math.min(e.count,Math.min(requested,e.template.getMaxStackSize()));
        ItemStack output=e.template.copyWithCount(amount);
        e.count-=amount;
        if(e.count==0)stored.remove(index);
        setChanged();return output;
    }
    public boolean isEmptyMemory() { return stored.isEmpty() && furnace.stream().allMatch(ItemStack::isEmpty); }

    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider provider) {
        super.saveAdditional(tag,provider);
        tag.put("CollectorMemory",exportMemory(provider));
        tag.store("MemoryIdentity",net.minecraft.core.UUIDUtil.CODEC,identity);
    }
    public CompoundTag exportMemory(HolderLookup.Provider provider) {
        CompoundTag out=new CompoundTag();
        ListTag list=new ListTag();
        for(Stored e:stored) {
            CompoundTag entry=new CompoundTag();
            entry.put("Item",com.mdk.cosmetic.platform.StackNbt.save(e.template,provider));
            entry.putLong("Count",e.count);
            list.add(entry);
        }
        out.put("Entries",list);
        out.store("MemoryIdentity",net.minecraft.core.UUIDUtil.CODEC,identity);
        ContainerHelper.saveAllItems(out,furnace,provider);
        out.putInt("Burn",litTime);out.putInt("BurnTotal",litDuration);out.putInt("Cook",progress);
        return out;
    }
    public void importMemory(CompoundTag tag,HolderLookup.Provider provider) {
        stored.clear();
        if(tag.read("MemoryIdentity",net.minecraft.core.UUIDUtil.CODEC).isPresent())identity=tag.read("MemoryIdentity",net.minecraft.core.UUIDUtil.CODEC).orElseThrow();
        ListTag list=tag.getListOrEmpty("Entries");
        for(int i=0;i<list.size();i++) {
            CompoundTag e=list.getCompoundOrEmpty(i);
            ItemStack stack=com.mdk.cosmetic.platform.StackNbt.parse(provider,e.getCompoundOrEmpty("Item"));
            long count=Math.clamp(e.getLongOr("Count",0L),0L,MAX_PER_TYPE);
            if(stack.isEmpty()||count<=0)continue;
            stack=stack.copyWithCount(1);
            int old=lookup(stack);
            if(old>=0)stored.get(old).count=Math.min(MAX_PER_TYPE,stored.get(old).count+count);
            else stored.add(new Stored(stack,count));
        }
        furnace=NonNullList.withSize(3,ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag,furnace,provider);
        litTime=Math.max(0,tag.getIntOr("Burn",0));litDuration=Math.max(0,tag.getIntOr("BurnTotal",0));
        progress=Math.clamp(tag.getIntOr("Cook",0),0,SMELT_TICKS);
        setChanged();
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider provider) {
        super.loadAdditional(tag,provider);
        importMemory(tag.getCompoundOrEmpty("CollectorMemory"),provider);
    }

    @Override public int getContainerSize(){return 3;}
    @Override public boolean isEmpty(){return furnace.stream().allMatch(ItemStack::isEmpty);}
    @Override public ItemStack getItem(int slot){return slot>=0&&slot<3?furnace.get(slot):ItemStack.EMPTY;}
    @Override public ItemStack removeItem(int slot,int count){
        ItemStack removed=ContainerHelper.removeItem(furnace,slot,count);
        if(!removed.isEmpty())setChanged();return removed;
    }
    @Override public ItemStack removeItemNoUpdate(int slot){return ContainerHelper.takeItem(furnace,slot);}
    @Override public void setItem(int slot,ItemStack stack){
        if(slot<0||slot>=3)return;
        furnace.set(slot,stack);
        if(stack.getCount()>getMaxStackSize())stack.setCount(getMaxStackSize());
        setChanged();
    }
    @Override public boolean canPlaceItem(int slot,ItemStack stack) {return slot!=2;}
    @Override public boolean stillValid(Player player){
        return level!=null && level.getBlockEntity(worldPosition)==this
                && player.distanceToSqr(worldPosition.getX()+0.5,worldPosition.getY()+0.5,worldPosition.getZ()+0.5)<100.0D;
    }
    @Override public void clearContent(){for(int i=0;i<3;i++)furnace.set(i,ItemStack.EMPTY);setChanged();}
    public static void serverTick(Level level,BlockPos pos,CollectorMemoryCoreBlockEntity core) {
        if(level.isClientSide())return;
        if (level instanceof net.minecraft.server.level.ServerLevel server && (level.getGameTime() & 15L) == 0)
            CollectorVoidPortalController.tickCore(server, pos, core);
        // Legacy fuel from pre-upgrade worlds is never discarded. Move it into memory.
        ItemStack oldFuel=core.furnace.get(1);
        if(!oldFuel.isEmpty()) {
            int accepted=core.deposit(oldFuel);
            if(accepted>0){oldFuel.shrink(accepted);if(oldFuel.isEmpty())core.furnace.set(1,ItemStack.EMPTY);core.setChanged();}
        }
        if(!CollectorMemoryStructure.ready(level,CollectorMemoryStructure.soulCenter(pos)))return;
        ItemStack input=core.furnace.get(0);
        var match=input.isEmpty()?java.util.Optional.<net.minecraft.world.item.crafting.RecipeHolder<net.minecraft.world.item.crafting.SmeltingRecipe>>empty():
                ((net.minecraft.server.level.ServerLevel)level).recipeAccess().getRecipeFor(RecipeType.SMELTING,new SingleRecipeInput(input),level);
        ItemStack result=match.isPresent()?match.get().value().assemble(new SingleRecipeInput(input),level.registryAccess()):ItemStack.EMPTY;
        ItemStack output=core.furnace.get(2);
        boolean canCook=!result.isEmpty() && (output.isEmpty() ||
                (ItemStack.isSameItemSameComponents(output,result) && output.getCount()+result.getCount()<=output.getMaxStackSize()));
        boolean changed=false;
        if(canCook) {
            core.progress++;
            changed=true;
            if(core.progress>=SMELT_TICKS){
                core.progress=0;
                input.shrink(1);
                if(output.isEmpty())core.furnace.set(2,result.copy()); else output.grow(result.getCount());
            }
        } else if(core.progress>0) {core.progress=0;changed=true;}
        // Burn counters remain in old NBT for safe world migration, but no fuel is consumed.
        if(core.litTime!=0 || core.litDuration!=0){core.litTime=0;core.litDuration=0;changed=true;}
        if(changed)core.setChanged();
    }
    /** Contents already travel inside the core item; the new vanilla Container removal hook must not drop them twice. */
    @Override public void preRemoveSideEffects(net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState next) {}
}
