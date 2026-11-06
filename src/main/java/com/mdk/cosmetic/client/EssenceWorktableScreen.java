package com.mdk.cosmetic.client;

import com.mdk.cosmetic.EssenceWorktableBlockEntity;
import com.mdk.cosmetic.EssenceWorktableMenu;
import com.mdk.cosmetic.MDKCosmetic;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Pixel-art IC2-like reservoir: 64 essence units become one charged power essence. */
public final class EssenceWorktableScreen extends AbstractContainerScreen<EssenceWorktableMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            MDKCosmetic.MOD_ID, "textures/gui/essence_worktable.png");

    public EssenceWorktableScreen(EssenceWorktableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int width = Math.round((menu.getProgress() / (float) EssenceWorktableBlockEntity.COST) * 64);
        if (width > 0) graphics.blit(TEXTURE, leftPos + 58, topPos + 41, 0, 176, width, 10, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, title, imageWidth / 2, 8, 0xFFE4CE);
        graphics.drawCenteredString(font,
                Component.literal(menu.getProgress() + " / " + EssenceWorktableBlockEntity.COST),
                imageWidth / 2, 62, 0x35203C);
        graphics.drawString(font, Component.translatable("gui.mdkcosmetic.essence.input"), 12, 26, 0x452B53, false);
        graphics.drawString(font, Component.translatable("gui.mdkcosmetic.essence.output"), 128, 26, 0x452B53, false);
        // The compact 176x166 layout intentionally omits the normal inventory label above slot row.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
