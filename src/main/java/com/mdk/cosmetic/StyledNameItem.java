package com.mdk.cosmetic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Simple item with a fixed colored display name, while keeping translations in lang files. */
public class StyledNameItem extends Item {
    private final ChatFormatting color;

    public StyledNameItem(Properties properties, ChatFormatting color) {
        super(properties);
        this.color = color;
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(color);
    }
}
