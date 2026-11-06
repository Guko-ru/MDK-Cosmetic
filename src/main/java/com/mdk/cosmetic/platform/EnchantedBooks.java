package com.mdk.cosmetic.platform;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
public final class EnchantedBooks {
 private EnchantedBooks() {}
 public static ItemStack createForEnchantment(EnchantmentInstance enchantment){var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(enchantment.enchantment(),enchantment.level());return book;}
}
