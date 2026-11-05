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

/** Each storage ItemStack carries its own stress, even when moved to a different base. */
public final class CollectorStorageItem extends BlockItem {
    private static final String KEY="MDKCollectorStress";
    private static final String FIRST="MDKCollectorActivated";
    public CollectorStorageItem(Block block, Item.Properties props) { super(block,props); }
    public static int stressOf(ItemStack stack) {
        CustomData data=stack.get(DataComponents.CUSTOM_DATA);
        return data==null?0:Math.clamp(data.copyTag().getInt(KEY),0,100);
    }
    public static boolean activated(ItemStack stack) {
        CustomData data=stack.get(DataComponents.CUSTOM_DATA);
        return data!=null && data.copyTag().getBoolean(FIRST);
    }
    public static void setStress(ItemStack stack,int stress,boolean activated) {
        CompoundTag tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        tag.putInt(KEY,Math.clamp(stress,0,100));
        tag.putBoolean(FIRST,activated);
        stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
    }
    @Override public InteractionResult place(BlockPlaceContext context) {
        ItemStack inHand=context.getItemInHand();
        int stress=stressOf(inHand);
        boolean activated=activated(inHand);
        var pos=context.getClickedPos();
        InteractionResult result=super.place(context);
        if (result.consumesAction() && !context.getLevel().isClientSide()
                && context.getLevel().getBlockEntity(pos) instanceof CollectorSoulStorageBlockEntity storage) {
            storage.setFromItem(stress,activated);
        }
        return result;
    }
    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context,
            List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack,context,tooltip,flag);
        int stress=stressOf(stack);
        tooltip.add(Component.translatable("tooltip.mdkcosmetic.storage_stress", stress)
                .withStyle(stress>=80?ChatFormatting.RED:stress>=40?ChatFormatting.GOLD:ChatFormatting.LIGHT_PURPLE));
    }
}
