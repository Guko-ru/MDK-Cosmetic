package com.mdk.cosmetic;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

/** Prevents loss of the bottomless inventory when players mine and relocate its core. */
public final class CollectorMemoryCoreItem extends BlockItem {
    public CollectorMemoryCoreItem(Block block,Item.Properties props){super(block,props);}
    public static void writeMemory(ItemStack stack,CompoundTag memory) {
        CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        tag.put("MDKMemory",memory.copy());
        stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
    }
    @Override public InteractionResult place(BlockPlaceContext ctx) {
        ItemStack held=ctx.getItemInHand();
        CustomData data=held.get(DataComponents.CUSTOM_DATA);
        CompoundTag stored=data==null?new CompoundTag():data.copyTag().getCompound("MDKMemory").copy();
        var pos=ctx.getClickedPos();
        InteractionResult result=super.place(ctx);
        if(result.consumesAction() && !ctx.getLevel().isClientSide()
                && ctx.getLevel().getBlockEntity(pos) instanceof CollectorMemoryCoreBlockEntity core)
            core.importMemory(stored,ctx.getLevel().registryAccess());
        return result;
    }
    @Override public void appendHoverText(ItemStack stack,Item.TooltipContext context,List<Component> tooltip,TooltipFlag flag) {
        super.appendHoverText(stack,context,tooltip,flag);
        CustomData data=stack.get(DataComponents.CUSTOM_DATA);
        if(data!=null) {
            var tag=data.copyTag().getCompound("MDKMemory");
            var entries=tag.getList("Entries",10);
            if(!entries.isEmpty())tooltip.add(Component.translatable("tooltip.mdkcosmetic.memory_saved",entries.size())
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}
