package com.mdk.cosmetic.client;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.*;
public final class ForgeMenuScreens {
 public <M extends AbstractContainerMenu,S extends Screen & MenuAccess<M>> void register(MenuType<M> type,MenuScreens.ScreenConstructor<M,S> factory){MenuScreens.register(type,factory);}
}
