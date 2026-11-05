package com.mdk.cosmetic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Two-part merchant-style trading and soul stress recovery interface. */
public final class CollectorLinkBlockEntity extends BaseContainerBlockEntity {
    // coin input, result preview, recovery input
    private NonNullList<ItemStack> items=NonNullList.withSize(3,ItemStack.EMPTY);
    private boolean active;
    /** 2 seconds of server-side preparation before a preview stack becomes available. */
    public static final int TRADE_TICKS = 40;
    private int tradeTicks;
    private int selectedOffer;
    public final ContainerData data=new ContainerData() {
        @Override public int get(int index) {
            if (index==0) return currentStress();
            if (index==1) return active?1:0;
            if (index==2) return tradeTicks;
            if (index==3) return selectedOffer;
            if (index==4) return memoryCore()!=null?1:0;
            return 0;
        }
        @Override public void set(int index,int value) { /* server authoritative */ }
        @Override public int getCount() { return 5; }
    };
    public CollectorLinkBlockEntity(BlockPos pos, BlockState state) {
        super(MDKCosmetic.COLLECTOR_LINK_BE.get(),pos,state);
    }
    @Override public int getContainerSize() { return 3; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> values) { items=values; }
    @Override protected Component getDefaultName() {
        return Component.translatable("menu.mdkcosmetic.collector_link");
    }
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inv) {
        return new CollectorLinkMenu(id,inv,this,data,this);
    }
    @Override public boolean canPlaceItem(int slot,ItemStack item) {
        if (slot==0) return item.is(MDKCosmetic.CHARGED_POWER_ESSENCE.get());
        if (slot==2) return item.is(MDKCosmetic.CHARGED_POWER_ESSENCE.get())
                || item.is(MDKCosmetic.POWER_ESSENCE.get());
        return false;
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries) {
        super.saveAdditional(tag,registries);
        tag.putInt("CollectorTradeOffer",selectedOffer);
        tag.putInt("CollectorOfferFormat",3);
        tag.putString("CollectorTradeOfferId",CollectorOffers.id(selectedOffer));
        // Ghost output is deliberately never serialized or dropped as free loot.
        NonNullList<ItemStack> safe=NonNullList.withSize(3,ItemStack.EMPTY);
        safe.set(0,items.get(0));safe.set(2,items.get(2));
        ContainerHelper.saveAllItems(tag,safe,registries);
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries) {
        super.loadAdditional(tag,registries);
        items=NonNullList.withSize(3,ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag,items,registries);
        items.set(1,ItemStack.EMPTY);
        int persisted=tag.getInt("CollectorTradeOffer");
        int format=tag.getInt("CollectorOfferFormat");
        if (format>=3 && tag.contains("CollectorTradeOfferId")) {
            // Stable item identifiers: adding/removing offers cannot select a wrong item.
            int found=CollectorOffers.indexOfId(tag.getString("CollectorTradeOfferId"));
            persisted=found>=0 ? found : 0;
        } else if (format==2) {
            // The v1.18.30 catalogue had 5 exclusives, then 17 hats, then misc.
            // Removed hats redirect to the first exclusive; all misc ids shift by 17.
            if (persisted>=5 && persisted<22) persisted=0;
            else if (persisted>=22) persisted-=17;
        } else {
            // Legacy 5-button market: white block, helmet, chest, legs, boots.
            persisted = switch (persisted) {
                case 0 -> CollectorOffers.indexAt(CollectorOffers.MISC,0);
                case 1 -> 0; // helmet
                case 2 -> 1; // chestplate
                case 3 -> 2; // leggings
                case 4 -> 3; // boots
                default -> 0;
            };
        }
        selectedOffer=Math.clamp(persisted,0,CollectorOffers.COUNT-1);
        tradeTicks=0; // no free output / preparation after unloading or reloading
    }
    public CollectorSoulStorageBlockEntity core() {
        if(level==null) return null;
        BlockState state=level.getBlockState(worldPosition);
        if (!state.is(MDKCosmetic.COLLECTOR_LINK.get())) return null;
        Direction outward=state.getValue(CollectorLinkBlock.FACING);
        BlockPos corePos=worldPosition.relative(outward.getOpposite());
        return level.getBlockEntity(corePos) instanceof CollectorSoulStorageBlockEntity core ? core:null;
    }
    /** The permanent memory upgrade is resolved from the existing soul center. */
    public CollectorMemoryCoreBlockEntity memoryCore() {
        CollectorSoulStorageBlockEntity soul=core();
        if(!active || soul==null || level==null)return null;
        return CollectorMemoryStructure.findCore(level,soul.getBlockPos());
    }
    public int currentStress() {
        CollectorSoulStorageBlockEntity core=core();
        return core==null?0:core.getStress();
    }
    public boolean isActive() { return active; }
    public void selectOffer(int id) {
        if (level==null || level.isClientSide() || id<0 || id>=CollectorOffers.COUNT) return;
        if (selectedOffer==id) return;
        selectedOffer=id;
        tradeTicks=0;
        items.set(1,ItemStack.EMPTY);
        setChanged();
    }
    /** Server validates offer, price, output count and active structure. */
    public boolean canCompleteTrade() {
        if (level==null || level.isClientSide() || !active || tradeTicks<TRADE_TICKS) return false;
        CollectorSoulStorageBlockEntity core=core();
        ItemStack expected=CollectorOffers.preview(selectedOffer);
        return core!=null && core.isAssembled() && core.getStress()<100
                && items.get(0).is(MDKCosmetic.CHARGED_POWER_ESSENCE.get())
                && items.get(0).getCount()>=CollectorOffers.cost(selectedOffer)
                && ItemStack.isSameItemSameComponents(items.get(1),expected)
                && items.get(1).getCount()==expected.getCount();
    }
    public void completeTrade(Player player) {
        // onTake runs after vanilla takes output. Recheck inputs and structure.
        if(level==null || level.isClientSide() || !active || tradeTicks<TRADE_TICKS) return;
        CollectorSoulStorageBlockEntity core=core();
        if(core==null || !core.isAssembled() || core.getStress()>=100) return;
        ItemStack coins=items.get(0);
        if(!coins.is(MDKCosmetic.CHARGED_POWER_ESSENCE.get())
                || coins.getCount()<CollectorOffers.cost(selectedOffer)) return;
        coins.shrink(CollectorOffers.cost(selectedOffer));
        items.set(1,ItemStack.EMPTY);
        tradeTicks=0;
        core.addStress(CollectorOffers.stress(selectedOffer));
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
            CollectorAdvancementEvents.onCollectorTrade(serverPlayer, CollectorOffers.product(selectedOffer));
        level.playSound(null,worldPosition,SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.BLOCKS,0.9F,1.15F);
        setChanged();
    }
    public void dropInputs() {
        if(level==null || level.isClientSide())return;
        for(int i:new int[]{0,2}) {
            ItemStack stack=items.get(i);
            if(!stack.isEmpty()) Block.popResource(level,worldPosition,stack.copy());
            items.set(i,ItemStack.EMPTY);
        }
        items.set(1,ItemStack.EMPTY);
        setChanged();
    }
    public static void serverTick(Level level,BlockPos pos,BlockState state,CollectorLinkBlockEntity link) {
        if(level.isClientSide()) return;
        if ((level.getGameTime()+(pos.asLong()&31))%10==0) {
            boolean formed=CollectorStructure.inspect(level,pos,state.getValue(CollectorLinkBlock.FACING)).formed();
            link.active=formed;
            if (state.getValue(CollectorLinkBlock.ACTIVE)!=formed)
                level.setBlock(pos,state.setValue(CollectorLinkBlock.ACTIVE,formed),3);
        }
        CollectorSoulStorageBlockEntity core=link.core();
        if(link.active && core!=null && core.isAssembled() && core.getStress()>0) {
            ItemStack offering=link.items.get(2);
            if(offering.is(MDKCosmetic.CHARGED_POWER_ESSENCE.get()) || offering.is(MDKCosmetic.POWER_ESSENCE.get())) {
                int amount=offering.is(MDKCosmetic.CHARGED_POWER_ESSENCE.get())?100:1;
                offering.shrink(1);
                core.reduceStress(amount);
                link.setChanged();
                level.playSound(null,pos,SoundEvents.EXPERIENCE_ORB_PICKUP,SoundSource.BLOCKS,0.45F,1.35F);
            }
        }
        // The output is a protected, unpersisted preview. No coins: no animation.
        // The client sees the server-synchronized progress via ContainerData index 2.
        boolean offer=link.active && core!=null && core.isAssembled() && core.getStress()<100
                && link.items.get(0).is(MDKCosmetic.CHARGED_POWER_ESSENCE.get())
                && link.items.get(0).getCount()>=CollectorOffers.cost(link.selectedOffer);
        if (!offer) {
            if (link.tradeTicks!=0 || !link.items.get(1).isEmpty()) {
                link.tradeTicks=0;
                link.items.set(1,ItemStack.EMPTY);
                link.setChanged();
            }
        } else if (link.items.get(1).isEmpty()) {
            if (link.tradeTicks<TRADE_TICKS) link.tradeTicks++;
            if (link.tradeTicks>=TRADE_TICKS) {
                // Product and price are determined exclusively by the selected server offer.
                link.items.set(1,CollectorOffers.preview(link.selectedOffer));
                link.setChanged();
                level.playSound(null,pos,SoundEvents.AMETHYST_BLOCK_CHIME,
                        SoundSource.BLOCKS,0.55F,1.30F);
            }
        } else {
            link.tradeTicks=TRADE_TICKS; // static filled bar until output is collected
        }
    }
}
