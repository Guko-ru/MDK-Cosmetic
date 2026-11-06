package com.mdk.cosmetic.client;

import com.mdk.cosmetic.EssenceWorktableBlockEntity;
import com.mdk.cosmetic.EssenceWorktableMenu;
import com.mdk.cosmetic.MDKCosmetic;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Pixel-art IC2-like reservoir: 64 essence units become one charged power essence. */
public final class EssenceWorktableScreen extends AbstractContainerScreen<EssenceWorktableMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            MDKCosmetic.MOD_ID, "textures/gui/essence_worktable.png");

    public EssenceWorktableScreen(EssenceWorktableMenu menu, Inventory playerInventory, Component title) {
        super(menu,playerInventory,title,176,166);
        
        
        inventoryLabelY = 72;
    }

    private void renderMachineBackground(GuiGraphicsExtractor graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int width = Math.round((menu.getProgress() / (float) EssenceWorktableBlockEntity.COST) * 64);
        if (width > 0) graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,TEXTURE, leftPos + 58, topPos + 41, 0, 176, width, 10, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.centeredText(font, title, imageWidth / 2, 8, 0xFFE4CE);
        graphics.centeredText(font,
                Component.literal(menu.getProgress() + " / " + EssenceWorktableBlockEntity.COST),
                imageWidth / 2, 62, 0x35203C);
        graphics.text(font, Component.translatable("gui.mdkcosmetic.essence.input"), 12, 26, 0x452B53, false);
        graphics.text(font, Component.translatable("gui.mdkcosmetic.essence.output"), 128, 26, 0x452B53, false);
        // The compact 176x166 layout intentionally omits the normal inventory label above slot row.
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partial) {}
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractTransparentBackground(graphics);renderMachineBackground(graphics,partialTick,mouseX,mouseY);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        extractTooltip(graphics, mouseX, mouseY);
    }
}
