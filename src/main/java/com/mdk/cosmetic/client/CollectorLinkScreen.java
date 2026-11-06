package com.mdk.cosmetic.client;

import com.mdk.cosmetic.CollectorLinkMenu;
import com.mdk.cosmetic.CollectorLinkBlockEntity;
import com.mdk.cosmetic.CollectorMemoryQueryPayload;
import com.mdk.cosmetic.CollectorOffers;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.mdk.cosmetic.platform.ForgePayloads;
import org.lwjgl.glfw.GLFW;

/** Compact, resolution-adaptive screen. All inventory slot positions are defined in CollectorLinkMenu. */
public final class CollectorLinkScreen extends AbstractContainerScreen<CollectorLinkMenu> {
    private static final int W = 606, H = 308;
    private static final int SHOP_COLS = 7, SHOP_ROWS = 2;
    private static final int SHOP_X = 17, SHOP_Y = 60, SHOP_DX = 33, SHOP_DY = 26;
    private static final int SHOP_SCROLL_X = 252, SHOP_SCROLL_Y = 60, SHOP_SCROLL_H = 48;
    private static final int STORE_X = 310, STORE_Y = 91;
    private static final int SCROLL_X = 546, SCROLL_Y = 91, SCROLL_H = 72;
    private static final int SEARCH_X = 305, SEARCH_Y = 53;

    private int category = CollectorOffers.EXCLUSIVE;
    private int offerRow;
    private boolean draggingMemoryScrollbar;
    private boolean draggingShopScrollbar;
    private float uiScale = 1.0F;
    private EditBox search;
    private List<SearchEntry> localDictionary;
    private record SearchEntry(String id, String name) { }

    public CollectorLinkScreen(CollectorLinkMenu menu, Inventory playerInv, Component title) {
        super(menu,playerInv,title,W,H);
        
        
        inventoryLabelX = 61;
        inventoryLabelY = 196;
    }

    @Override protected void init() {
        // Vanilla GUI scale can leave fewer than 308 logical pixels. Scale EVERYTHING,
        // including slots, tooltips and mouse hit tests, rather than clipping the menu.
        uiScale = Math.min(1.0F, Math.min((float)(width - 8) / W, (float)(height - 8) / H));
        uiScale = Math.max(0.25F, uiScale);
        super.init();
        leftPos = Math.round((width / uiScale - imageWidth) / 2.0F);
        topPos = Math.round((height / uiScale - imageHeight) / 2.0F);
        category = CollectorOffers.category(menu.getSelectedOffer());
        offerRow = 0;
        search = new EditBox(font, leftPos + SEARCH_X + 3, topPos + SEARCH_Y + 4, 230, 14,
                Component.translatable("screen.mdkcosmetic.search"));
        search.setMaxLength(64);
        search.setHint(Component.translatable("screen.mdkcosmetic.search"));
        search.setBordered(false);
        search.setTextColor(0xFFE8CEFC);
        search.setResponder(this::requestSearch);
        addRenderableWidget(search);
    }

    @Override public void containerTick() {
        super.containerTick();
        if (search != null) search.setEditable(menu.isMemoryReady());
    }

    /** Never let the inventory key (normally E) close the screen while typing in search. */
    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent mdk$key){int keyCode=mdk$key.key(),scanCode=mdk$key.scancode(),modifiers=mdk$key.modifiers();
        if (search != null && search.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                search.setFocused(false);
                return true;
            }
            search.keyPressed(mdk$key);
            return true;
        }
        return super.keyPressed(mdk$key);
    }

    @Override public boolean charTyped(net.minecraft.client.input.CharacterEvent mdk$character){char codePoint=(char)mdk$character.codepoint();int modifiers=0;
        if (search != null && search.isFocused()) {
            search.charTyped(mdk$character);
            return true;
        }
        return super.charTyped(mdk$character);
    }

    private void requestSearch(String value) {
        if (minecraft == null || minecraft.player == null || !menu.isMemoryReady()) return;
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        StringBuilder ids = new StringBuilder();
        if (!normalized.isBlank()) {
            if (localDictionary == null) {
                localDictionary = new ArrayList<>();
                for (Item item : BuiltInRegistries.ITEM) {
                    String id = BuiltInRegistries.ITEM.getKey(item).toString();
                    String name = new ItemStack(item).getHoverName().getString().toLowerCase(Locale.ROOT);
                    localDictionary.add(new SearchEntry(id, name));
                }
            }
            for (SearchEntry item : localDictionary) {
                if (item.name().contains(normalized) || item.id().contains(normalized)) {
                    if (ids.length() + item.id().length() + 1 > 15000) break;
                    if (!ids.isEmpty()) ids.append(',');
                    ids.append(item.id());
                }
            }
        }
        com.mdk.cosmetic.platform.ForgePayloads.sendToServer(new CollectorMemoryQueryPayload(menu.containerId, value, ids.toString()));
    }

    private void rect(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(leftPos + x, topPos + y, leftPos + x + w, topPos + y + h, color);
    }
    private void border(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        rect(g, x, y, w, 1, color); rect(g, x, y + h - 1, w, 1, color);
        rect(g, x, y, 1, h, color); rect(g, x + w - 1, y, 1, h, color);
    }
    private void slotBg(GuiGraphicsExtractor g, int x, int y) {
        rect(g, x - 1, y - 1, 18, 18, 0xFF8566A4);
        rect(g, x, y, 16, 16, 0xFF231829);
    }
    private void tab(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean active) {
        rect(g, x, y, w, h, active ? 0xFF71459A : 0xFF422B58);
        border(g, x, y, w, h, active ? 0xFFE4B1FF : 0xFF9266AC);
        rect(g, x + 4, y + 4, w - 8, 1, active ? 0xFFBF83DE : 0xFF674B7B);
    }
    private int offerRows() { return Math.max(1, (CollectorOffers.count(category) + SHOP_COLS - 1) / SHOP_COLS); }
    private int offerMaxRow() { return Math.max(0, offerRows() - SHOP_ROWS); }
    private void category(int next) { if (next != category) { category = next; offerRow = 0; } }

    private int hoveredOffer(int x, int y) {
        if (x < SHOP_X || y < SHOP_Y) return -1;
        int col = (x - SHOP_X) / SHOP_DX, row = (y - SHOP_Y) / SHOP_DY;
        if (col >= SHOP_COLS || row >= SHOP_ROWS) return -1;
        int xx = SHOP_X + col * SHOP_DX, yy = SHOP_Y + row * SHOP_DY;
        if (x >= xx + 29 || y >= yy + 23) return -1;
        return CollectorOffers.indexAt(category, (offerRow + row) * SHOP_COLS + col);
    }
    private void action(int id) {
        if (minecraft != null && minecraft.gameMode != null)
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }
    private int thumbHeight() {
        int totalRows = Math.max(1, (menu.getMemoryTotal() + CollectorLinkMenu.MEMORY_COLUMNS - 1) / CollectorLinkMenu.MEMORY_COLUMNS);
        return Math.clamp(SCROLL_H * CollectorLinkMenu.MEMORY_ROWS / totalRows, 12, SCROLL_H);
    }
    private int offerThumbHeight() {
        return Math.clamp(SHOP_SCROLL_H * SHOP_ROWS / offerRows(), 12, SHOP_SCROLL_H);
    }
    private void seekMemory(double logicalY) {
        int h = thumbHeight();
        double ratio = Math.clamp((logicalY - topPos - SCROLL_Y - h / 2.0D) / Math.max(1, SCROLL_H - h), 0.0D, 1.0D);
        action(3000 + (int)Math.round(ratio * 1000.0D));
    }
    private void seekShop(double logicalY) {
        int h = offerThumbHeight();
        double ratio = Math.clamp((logicalY - topPos - SHOP_SCROLL_Y - h / 2.0D) / Math.max(1, SHOP_SCROLL_H - h), 0.0D, 1.0D);
        offerRow = (int)Math.round(ratio * offerMaxRow());
    }

    @Override public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent mdk$mouse,boolean mdk$doubleClick){double mouseX=mdk$mouse.x(),mouseY=mdk$mouse.y();int button=mdk$mouse.button();
        // Entry points receive physical GUI coordinates, then convert to the virtual canvas.
        double vx = mouseX / uiScale, vy = mouseY / uiScale;
        int x = (int)vx - leftPos, y = (int)vy - topPos;
        if (menu.isMemoryReady() && (button == 0 || button == 1)
                && x >= STORE_X && x < STORE_X + CollectorLinkMenu.MEMORY_COLUMNS * 18
                && y >= STORE_Y && y < STORE_Y + CollectorLinkMenu.MEMORY_ROWS * 18) {
            int col = (x - STORE_X) / 18, row = (y - STORE_Y) / 18;
            if ((x - STORE_X) % 18 < 16 && (y - STORE_Y) % 18 < 16
                    && minecraft != null && minecraft.gameMode != null && minecraft.player != null) {
                minecraft.gameMode.handleContainerInput(menu.containerId,
                        CollectorLinkMenu.MEMORY_START + row * CollectorLinkMenu.MEMORY_COLUMNS + col,
                        button, button == 0 && net.minecraft.client.Minecraft.getInstance().hasShiftDown() ? ContainerInput.QUICK_MOVE : ContainerInput.PICKUP,
                        minecraft.player);
                return true;
            }
        }
        if (button == 0) {
            if (x >= 13 && x < 188 && y >= 30 && y < 51) { category(CollectorOffers.EXCLUSIVE); return true; }
            if (x >= 192 && x < 281 && y >= 30 && y < 51) { category(CollectorOffers.MISC); return true; }
            if (x >= SHOP_SCROLL_X - 2 && x < SHOP_SCROLL_X + 14
                    && y >= SHOP_SCROLL_Y && y < SHOP_SCROLL_Y + SHOP_SCROLL_H) {
                draggingShopScrollbar = true; seekShop(vy); return true;
            }
            int offer = hoveredOffer(x, y);
            if (offer >= 0) { action(offer); return true; }
            if (menu.isMemoryReady() && x >= SCROLL_X - 2 && x < SCROLL_X + 13
                    && y >= SCROLL_Y && y < SCROLL_Y + SCROLL_H) {
                draggingMemoryScrollbar = true; seekMemory(vy); return true;
            }
        }
        return super.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(vx,vy,mdk$mouse.buttonInfo()),mdk$doubleClick);
    }
    @Override public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent mdk$mouse,double dx,double dy){double mouseX=mdk$mouse.x(),mouseY=mdk$mouse.y();int button=mdk$mouse.button();
        if (draggingShopScrollbar && button == 0) { seekShop(mouseY / uiScale); return true; }
        if (draggingMemoryScrollbar && button == 0) { seekMemory(mouseY / uiScale); return true; }
        return super.mouseDragged(new net.minecraft.client.input.MouseButtonEvent(mouseX / uiScale,mouseY / uiScale,mdk$mouse.buttonInfo()),dx / uiScale,dy / uiScale);
    }
    @Override public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent mdk$mouse){double mouseX=mdk$mouse.x(),mouseY=mdk$mouse.y();int button=mdk$mouse.button();
        draggingShopScrollbar = false;
        draggingMemoryScrollbar = false;
        return super.mouseReleased(new net.minecraft.client.input.MouseButtonEvent(mouseX / uiScale,mouseY / uiScale,mdk$mouse.buttonInfo()));
    }
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double x = mouseX / uiScale - leftPos, y = mouseY / uiScale - topPos;
        if (menu.isMemoryReady() && x >= 302 && x < 565 && y >= 82 && y < 169) {
            int n = Math.max(1, Math.min(4, (int)Math.ceil(Math.abs(scrollY))));
            for (int i = 0; i < n; i++) action(scrollY > 0 ? 2000 : 2001);
            return true;
        }
        if (x >= 13 && x < 281 && y >= SHOP_Y - 3 && y < SHOP_Y + SHOP_ROWS * SHOP_DY + 2) {
            offerRow = Math.clamp(offerRow - (int)Math.signum(scrollY), 0, offerMaxRow());
            return true;
        }
        return super.mouseScrolled(mouseX / uiScale, mouseY / uiScale, scrollX, scrollY);
    }

    private void renderMachineBackground(GuiGraphicsExtractor g, float partial, int mouseX, int mouseY) {
        rect(g, 0, 0, W, H, 0xFF15101D);
        border(g, 0, 0, W, H, 0xFF784E9B);
        rect(g, 5, 5, W - 10, H - 10, 0xFF261C32);
        rect(g, 9, 22, 277, 164, 0xFF342640);
        rect(g, 9, 191, 277, 108, 0xFF201729);
        rect(g, 293, 22, 304, 277, 0xFF342640);
        rect(g, 289, 22, 2, 277, 0xFF9959C5);
        tab(g, 13, 30, 175, 21, category == CollectorOffers.EXCLUSIVE);
        tab(g, 192, 30, 89, 21, category == CollectorOffers.MISC);
        for (int row = 0; row < SHOP_ROWS; row++) for (int col = 0; col < SHOP_COLS; col++) {
            int offer = CollectorOffers.indexAt(category, (offerRow + row) * SHOP_COLS + col);
            if (offer < 0) continue;
            int x = SHOP_X + col * SHOP_DX, y = SHOP_Y + row * SHOP_DY;
            rect(g, x, y, 29, 23, offer == menu.getSelectedOffer() ? 0xFF68408E : 0xFF23182D);
            border(g, x, y, 29, 23, offer == menu.getSelectedOffer() ? 0xFFF3C2FF : 0xFF8F5BAF);
        }
        // The store now uses a browser-style scrollbar, not arrow buttons.
        rect(g, SHOP_SCROLL_X, SHOP_SCROLL_Y, 9, SHOP_SCROLL_H, 0xFF190E25);
        int shopThumb = offerThumbHeight();
        int shopOffset = offerMaxRow() == 0 ? 0 : (SHOP_SCROLL_H - shopThumb) * offerRow / offerMaxRow();
        rect(g, SHOP_SCROLL_X + 1, SHOP_SCROLL_Y + shopOffset, 7, shopThumb, 0xFF9D65C6);
        border(g, SHOP_SCROLL_X + 1, SHOP_SCROLL_Y + shopOffset, 7, shopThumb, 0xFFE0B2FB);
        slotBg(g, 42, 140); slotBg(g, 222, 140); slotBg(g, 571, 29);
        rect(g, 75, 145, 132, 6, 0xFF160F22);
        int progress = menu.getProgress();
        if (progress > 0) rect(g, 76, 146,
                Math.min(130, progress * 130 / CollectorLinkBlockEntity.TRADE_TICKS), 4, 0xFFC187EB);
        for (int i = 0; i < 36; i++) {
            int x = 61 + (i % 9) * 18;
            int y = i < 27 ? 211 + (i / 9) * 18 : 273;
            slotBg(g, x, y);
        }
        rect(g, 305, 40, 248, 7, 0xFF190F24);
        int stress = menu.getStress();
        rect(g, 306, 41, stress * 246 / 100, 5,
                stress >= 80 ? 0xFFE04472 : stress >= 50 ? 0xFFDAA04E : 0xFF9D69D4);
        rect(g, SEARCH_X, SEARCH_Y, 241, 22, 0xFF23182C);
        border(g, SEARCH_X, SEARCH_Y, 241, 22, 0xFF9567B1);
        rect(g, 304, 84, 255, 91, 0xFF241A30);
        rect(g, 304, 183, 255, 111, 0xFF271B35);
        // Split the bottom row: crafting on the left, smelting beside it.
        rect(g, 433, 192, 1, 91, 0xFF82589E);
        if (menu.isMemoryReady()) {
            for (int i = 0; i < CollectorLinkMenu.MEMORY_SIZE; i++)
                slotBg(g, STORE_X + (i % CollectorLinkMenu.MEMORY_COLUMNS) * 18,
                        STORE_Y + (i / CollectorLinkMenu.MEMORY_COLUMNS) * 18);
            rect(g, SCROLL_X, SCROLL_Y, 10, SCROLL_H, 0xFF171022);
            int th = thumbHeight();
            int maxRow = Math.max(1, menu.getMemoryMaxRow());
            int offset = (SCROLL_H - th) * menu.getMemoryRow() / maxRow;
            rect(g, SCROLL_X + 1, SCROLL_Y + offset, 8, th, 0xFF9F65C6);
            border(g, SCROLL_X + 1, SCROLL_Y + offset, 8, th, 0xFFE0B2FB);
            for (int i = 0; i < 9; i++) slotBg(g, 319 + i % 3 * 18, 207 + i / 3 * 18);
            slotBg(g, 393, 225);
            slotBg(g, 452, 222); slotBg(g, 532, 222);
            rect(g, 475, 226, 48, 7, 0xFF180E21);
            rect(g, 476, 227, Math.min(46, menu.getSmeltProgress() * 46 / menu.getSmeltTime()), 5, 0xFFCF7EF4);
        }
    }

    @Override protected void extractLabels(GuiGraphicsExtractor g, int mx, int my) {
        g.centeredText(font, title, imageWidth / 2, 7, 0xFFE9C8FB);
        g.centeredText(font, Component.translatable("screen.mdkcosmetic.exclusive"), 100, 36, 0xFFF2D6FF);
        g.centeredText(font, Component.translatable("screen.mdkcosmetic.misc"), 236, 36, 0xFFF2D6FF);
        g.text(font, Component.translatable("screen.mdkcosmetic.stress"), 306, 27, 0xFFE6CBF6, false);
        g.text(font, menu.getStress() + "%", 530, 27, 0xFFFAEEFF, false);
        g.text(font, Component.translatable("container.inventory"), 61, 196, 0xFFD6BBEA, false);
        if (menu.isMemoryReady()) {
            g.text(font, Component.translatable("screen.mdkcosmetic.memory"), 310, 78, 0xFFE8CCFC, false);
            g.text(font, Integer.toString(menu.getMemoryTotal()), 524, 78, 0xFFC19AD7, false);
            g.text(font, Component.translatable("screen.mdkcosmetic.crafting"), 310, 188, 0xFFE8CCFC, false);
            g.text(font, Component.translatable("screen.mdkcosmetic.smelting_no_fuel"), 439, 188, 0xFFE8CCFC, false);
        } else {
            g.centeredText(font, Component.translatable("screen.mdkcosmetic.memory_locked"), 446, 128, 0xFFCAA9D9);
        }
        if (!menu.isActive())
            g.centeredText(font, Component.translatable("screen.mdkcosmetic.structure_required"), 147, 168, 0xFFFF9A9A);
    }

    private static String compactCount(long count) {
        if (count >= 1_000_000_000_000L) return (count / 1_000_000_000_000L) + "T";
        if (count >= 1_000_000_000L) return (count / 1_000_000_000L) + "G";
        if (count >= 1_000_000L) return (count / 1_000_000L) + "M";
        if (count >= 1000L) return (count / 1000L) + "k";
        return Long.toString(count);
    }
    /** Shop items are drawn on top of the slots and strictly clipped to each icon.
     *  Some detailed item models are physically larger than a vanilla GUI sprite.
     *  Clipping prevents geometry from spilling over nearby buttons and inventory. */
    private void renderShopIcons(GuiGraphicsExtractor g) {
        for (int row = 0; row < SHOP_ROWS; row++) for (int col = 0; col < SHOP_COLS; col++) {
            int offer = CollectorOffers.indexAt(category, (offerRow + row) * SHOP_COLS + col);
            if (offer < 0) continue;
            int x = SHOP_X + col * SHOP_DX + 6;
            int y = SHOP_Y + row * SHOP_DY + 3;
            clipItemStart(g, x, y);
            g.item(CollectorOffers.preview(offer), leftPos + x, topPos + y);
            g.disableScissor();
        }
    }

    /** Since 1.21.4, GuiGraphicsExtractor transforms the scissor by the current GUI pose. */
    private void clipItemStart(GuiGraphicsExtractor g,int localX,int localY){
        g.enableScissor(leftPos+localX,topPos+localY,leftPos+localX+16,topPos+localY+16);
    }

    @Override protected void extractSlot(GuiGraphicsExtractor g, net.minecraft.world.inventory.Slot slot,int mouseX,int mouseY) {
        // Only the virtual 12x4 memory grid needs clipping. Its slot preview can
        // contain oversized animated 3D items (e.g. the Collector sword).
        boolean memoryCell = menu.isMemoryReady()
                && slot.x >= STORE_X && slot.x < STORE_X + CollectorLinkMenu.MEMORY_COLUMNS * 18
                && slot.y >= STORE_Y && slot.y < STORE_Y + CollectorLinkMenu.MEMORY_ROWS * 18;
        if (memoryCell) g.enableScissor(slot.x,slot.y,slot.x+16,slot.y+16);
        super.extractSlot(g, slot,mouseX,mouseY);
        if (memoryCell) g.disableScissor();
    }

    private void renderMemoryCounts(GuiGraphicsExtractor g) {
        if (!menu.isMemoryReady()) return;
        // Render AFTER vanilla slots. Otherwise the item sprites cover count labels.
        g.pose().pushMatrix();
        g.nextStratum();
        for (int i = 0; i < CollectorLinkMenu.MEMORY_SIZE; i++) {
            long count = menu.getMemoryQuantity(i);
            if (count <= 1) continue;
            String label = compactCount(count);
            int x = leftPos + STORE_X + (i % CollectorLinkMenu.MEMORY_COLUMNS) * 18;
            int y = topPos + STORE_Y + (i / CollectorLinkMenu.MEMORY_COLUMNS) * 18;
            g.text(font, label, x + 17 - font.width(label), y + 10, 0xFFFFE8FF, true);
        }
        g.pose().popMatrix();
    }

    // mdk: unified scaled canvas; native background callbacks must not draw it a second time.
    @Override public void extractBackground(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partial) {}

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float partial) {
        // Keep the vignette behind everything at the real GUI resolution.
        extractTransparentBackground(g);
        g.pose().pushMatrix();
        g.pose().scale(uiScale,uiScale);
        int logicalX = (int)(mx / uiScale), logicalY = (int)(my / uiScale);
        renderMachineBackground(g,partial,logicalX,logicalY);
        super.extractRenderState(g, logicalX, logicalY, partial);
        renderShopIcons(g);
        renderMemoryCounts(g);
        int offer = hoveredOffer(logicalX - leftPos, logicalY - topPos);
        if (offer >= 0) {
            ItemStack product = CollectorOffers.preview(offer);
            g.setTooltipForNextFrame(font, Component.translatable("screen.mdkcosmetic.offer_price",
                    product.getHoverName().copy(), product.getCount(), CollectorOffers.cost(offer)), logicalX, logicalY);
        } else extractTooltip(g, logicalX, logicalY);
        g.pose().popMatrix();
    }
}
