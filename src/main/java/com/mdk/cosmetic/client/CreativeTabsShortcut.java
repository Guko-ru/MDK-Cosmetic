package com.mdk.cosmetic.client;
import com.mdk.cosmetic.MDKCosmetic;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.client.itemgroup.v1.FabricCreativeInventoryScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
/** A visible route to Fabric's separate page containing the two MDK tabs. */
public final class CreativeTabsShortcut {
 public static void initialize(){ScreenEvents.AFTER_INIT.register((client,screen,width,height)->{
  if(screen instanceof CreativeModeInventoryScreen creative){
   Screens.getButtons(screen).add(Button.builder(Component.literal("MDK"),button->{
    ((FabricCreativeInventoryScreen)creative).setSelectedItemGroup(MDKCosmetic.COSMETIC_TAB.get());
   }).bounds(width-42,4,38,18).tooltip(Tooltip.create(Component.translatable("itemGroup.mdkcosmetic.mdk_cosmetic"))).build());
  }
 });}
}
