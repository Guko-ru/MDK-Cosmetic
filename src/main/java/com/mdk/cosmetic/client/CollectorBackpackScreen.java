package com.mdk.cosmetic.client;

import com.mdk.cosmetic.CollectorBackpackMenu;
import com.mdk.cosmetic.CollectorBackpackQueryPayload;
import com.mdk.cosmetic.MDKCosmetic;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.mdk.cosmetic.platform.Networking;
import org.lwjgl.glfw.GLFW;

/** Collector backpack with five universal vertical modules and an expandable contextual panel. */
public final class CollectorBackpackScreen extends AbstractContainerScreen<CollectorBackpackMenu> {
    private static final int W = 442, H = 234;
    private static final int MEMORY_SCROLL_X = 168, MEMORY_SCROLL_Y = 34, MEMORY_SCROLL_H = 53;
    private static final int FILTER_SCROLL_X = 373, FILTER_SCROLL_Y = 55, FILTER_SCROLL_H = 54;
    private EditBox search;
    private List<SearchEntry> dictionary;
    private boolean draggingMemory;
    private boolean draggingFilter;
    private float scale = 1.0F;
    private record SearchEntry(String id, String localized) {}

    public CollectorBackpackScreen(CollectorBackpackMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = W;
        imageHeight = H;
        inventoryLabelX = 8;
        inventoryLabelY = 138;
    }

    private static Component txt(String value) { return Component.translatable("screen.mdkcosmetic." + value); }

    @Override protected void init() {
        scale = Math.min(1.0F, Math.min((width - 8.0F) / W, (height - 8.0F) / H));
        scale = Math.max(0.25F, scale);
        super.init();
        leftPos = Math.round((width / scale - W) / 2.0F);
        topPos = Math.round((height / scale - H) / 2.0F);
        if (menu.isRemote()) {
            search = new EditBox(font, leftPos + 8, topPos + 14, 154, 14, txt("backpack_search"));
            search.setMaxLength(64);
            search.setHint(txt("backpack_search_hint"));
            search.setBordered(false);
            search.setTextColor(0xFFF4DCFF);
            search.setResponder(this::sendSearch);
            addRenderableWidget(search);
        } else search = null;
    }

    private void sendSearch(String query) {
        if (minecraft == null || minecraft.player == null) return;
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        StringBuilder ids = new StringBuilder();
        if (!normalized.isEmpty()) {
            if (dictionary == null) {
                dictionary = new ArrayList<>();
                for (Item item : BuiltInRegistries.ITEM) {
                    ItemStack stack = new ItemStack(item);
                    dictionary.add(new SearchEntry(BuiltInRegistries.ITEM.getKey(item).toString(),
                            stack.getHoverName().getString().toLowerCase(Locale.ROOT)));
                }
            }
            for (SearchEntry entry : dictionary) {
                if (entry.id().contains(normalized) || entry.localized().contains(normalized)) {
                    if (ids.length() + entry.id().length() + 1 >= 15000) break;
                    if (ids.length() > 0) ids.append(',');
                    ids.append(entry.id());
                }
            }
        }
        Networking.sendToServer(new CollectorBackpackQueryPayload(menu.containerId, normalized, ids.toString()));
    }

    private void fill(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(leftPos + x, topPos + y, leftPos + x + w, topPos + y + h, color);
    }
    private void outline(GuiGraphics g, int x, int y, int w, int h, int color) {
        fill(g, x, y, w, 1, color);
        fill(g, x, y + h - 1, w, 1, color);
        fill(g, x, y, 1, h, color);
        fill(g, x + w - 1, y, 1, h, color);
    }
    private void slotBg(GuiGraphics g, int x, int y) {
        fill(g, x - 1, y - 1, 18, 18, 0xFF715782);
        fill(g, x, y, 16, 16, 0xFF191326);
    }
    private void drawText(GuiGraphics g, Component name, int x, int y, int maxWidth, int color) {
        String visible = font.plainSubstrByWidth(name.getString(), maxWidth);
        g.drawString(font, visible, x, y, color, false);
    }
    private void buttonBg(GuiGraphics g, int x, int y, int w, int h, boolean active) {
        fill(g, x, y, w, h, active ? 0xFF62427A : 0xFF3B2A4E);
        outline(g, x, y, w, h, active ? 0xFFD7B1FF : 0xFF775A91);
    }
    private void button(GuiGraphics g, Component text, int x, int y, int w, int h, boolean active) {
        buttonBg(g, x, y, w, h, active);
        drawText(g, text, x + 4, y + (h - 8) / 2, w - 8, 0xFFF0DEFF);
    }
    private int moduleKind(ItemStack item) {
        if (item.is(MDKCosmetic.COLLECTOR_BACKPACK_MAGNET.get())) return 1;
        if (item.is(MDKCosmetic.COLLECTOR_BACKPACK_CRAFTING.get())) return 2;
        if (item.is(MDKCosmetic.COLLECTOR_BACKPACK_FURNACE.get())) return 3;
        if (item.is(MDKCosmetic.COLLECTOR_BACKPACK_TOTEM.get())) return 4;
        if (item.is(MDKCosmetic.COLLECTOR_BACKPACK_DEATH_VAULT.get())) return 5;
        return 0;
    }
    private int activeKind() { return moduleKind(menu.moduleAt(menu.activeModule())); }
    private int memoryThumb() { return Math.clamp(MEMORY_SCROLL_H / menu.getPages(), 12, MEMORY_SCROLL_H); }
    private int filterThumb() { return Math.clamp(FILTER_SCROLL_H / menu.filterPagesShown(), 12, FILTER_SCROLL_H); }
    private void scrollMemory(double logicalY) {
        int thumb = memoryThumb();
        double ratio = Math.clamp((logicalY - topPos - MEMORY_SCROLL_Y - thumb / 2.0D) / Math.max(1, MEMORY_SCROLL_H - thumb), 0.0D, 1.0D);
        int target = (int) Math.round(ratio * Math.max(0, menu.getPages() - 1));
        if (target != menu.getPage()) clickButton(1000 + target);
    }
    private void scrollFilter(double logicalY) {
        int thumb = filterThumb();
        double ratio = Math.clamp((logicalY - topPos - FILTER_SCROLL_Y - thumb / 2.0D) / Math.max(1, FILTER_SCROLL_H - thumb), 0.0D, 1.0D);
        int target = (int) Math.round(ratio * Math.max(0, menu.filterPagesShown() - 1));
        if (target != menu.filterPage()) clickButton(4000 + target);
    }

    @Override protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        fill(g, 0, 0, 270, H, 0xFF15111F);
        outline(g, 0, 0, 270, H, 0xFFB784EA);
        fill(g, 5, 10, 260, 124, 0xFF261D35);
        fill(g, 5, 144, 166, 85, 0xFF292036);

        for (int i = 0; i < (menu.isRemote() ? 27 : menu.capacity()); i++)
            slotBg(g, 8 + (i % 9) * 18, 34 + (i / 9) * 18);
        for (int i = 0; i < 36; i++)
            slotBg(g, 8 + (i % 9) * 18, i < 27 ? 150 + (i / 9) * 18 : 208);

        if (menu.isRemote()) {
            fill(g, 7, 12, 158, 17, 0xFF37274C);
            outline(g, 7, 12, 158, 17, 0xFF9573BC);
            fill(g, MEMORY_SCROLL_X, MEMORY_SCROLL_Y, 9, MEMORY_SCROLL_H, 0xFF190E25);
            int thumb = memoryThumb();
            int offset = menu.getPages() <= 1 ? 0 : (MEMORY_SCROLL_H - thumb) * menu.getPage() / (menu.getPages() - 1);
            fill(g, MEMORY_SCROLL_X + 1, MEMORY_SCROLL_Y + offset, 7, thumb, 0xFFAB6CD4);
        }

        // Five universal, player-configurable module slots in both backpack modes.
        fill(g, 182, 24, 82, 134, 0xFF21172D);
        outline(g, 182, 24, 82, 134, 0xFF765B92);
        for (int i = 0; i < 5; i++) {
            slotBg(g, 190, 34 + i * 22);
            int arrowY = 34 + i * 22;
            buttonBg(g, 214, arrowY, 18, 16, menu.activeModule() == i);
        }
        // Sorting is a single small action in the bottom-right, never mixed with magnet settings.
        buttonBg(g, 243, 208, 19, 18, false);
        if (menu.canUpgrade()) buttonBg(g, 186, 164, 76, 18, true);

        if (menu.activeModule() >= 0) {
            fill(g, 272, 13, 168, 216, 0xFF171121);
            outline(g, 272, 13, 168, 216, 0xFFAF82DD);
            fill(g, 276, 35, 160, 183, 0xFF261C34);
            int kind = activeKind();
            if (kind == 1) {
                for (int i = 0; i < 9; i++) slotBg(g, 305 + (i % 3) * 18, 56 + (i / 3) * 18);
                fill(g, FILTER_SCROLL_X, FILTER_SCROLL_Y, 9, FILTER_SCROLL_H, 0xFF190E25);
                int thumb = filterThumb();
                int offset = menu.filterPagesShown() <= 1 ? 0 : (FILTER_SCROLL_H - thumb) * menu.filterPage() / (menu.filterPagesShown() - 1);
                fill(g, FILTER_SCROLL_X + 1, FILTER_SCROLL_Y + offset, 7, thumb, 0xFFAB6CD4);
                buttonBg(g, 286, 137, 140, 17, menu.magnetEnabled());
                buttonBg(g, 286, 159, 140, 17, menu.whitelist());
                buttonBg(g, 286, 181, 140, 17, menu.exact());
            } else if (kind == 2) {
                for (int i = 0; i < 9; i++) slotBg(g, 290 + (i % 3) * 18, 62 + (i / 3) * 18);
                slotBg(g, 394, 81);
                fill(g, 358, 87, 24, 3, 0xFFC59AEF);
                fill(g, 376, 82, 7, 13, 0xFFC59AEF);
            } else if (kind == 3) {
                slotBg(g, 294, 80);
                slotBg(g, 294, 119);
                slotBg(g, 391, 80);
                fill(g, 326, 86, 43, 4, 0xFF3A294B);
                int progress = menu.furnaceValue(2);
                int cookTime = Math.max(1, menu.furnaceValue(3));
                fill(g, 326, 86, Math.clamp(progress * 43 / cookTime, 0, 43), 4, 0xFFBF81E5);
                fill(g, 301, 108, 3, 4 + Math.clamp(menu.furnaceValue(0) / 35, 0, 10), 0xFFFFB96A);
            } else if (kind == 4 || kind == 5) {
                buttonBg(g, 287, 94, 138, 22, kind == 4 ? menu.totemEnabled() : menu.deathVaultEnabled());
            }
        }
    }

    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {
        drawText(g, txt(menu.isRemote() ? "collector_memory_terminal" : "collector_backpack"), 9, 3, 248, 0xFFE4CCFC);
        g.drawString(font, Component.translatable("container.inventory"), 8, 138, 0xFFDECAEE, false);
        drawText(g, txt("module_modules"), 186, 13, 74, 0xFFE4CCFC);
        for (int i = 0; i < 5; i++) {
            drawText(g, Component.literal(">"), 220, 38 + i * 22, 10, 0xFFECCFFF);
        }
        if (menu.canUpgrade()) drawText(g, txt("backpack_upgrade_button"), 192, 169, 64, 0xFFEECFFF);
        drawText(g, Component.literal("⇅"), 246, 213, 14, 0xFFEECFFF);
        if (menu.activeModule() >= 0) {
            int kind = activeKind();
            String title = switch (kind) {
                case 1 -> "module_magnet";
                case 2 -> "module_crafting";
                case 3 -> "module_furnace";
                case 4 -> "module_totem";
                case 5 -> "module_death_vault";
                default -> "module_install";
            };
            drawText(g, txt(title), 280, 21, 150, 0xFFE8CCFF);
            if (kind == 1) {
                drawText(g, txt("module_filter"), 282, 39, 142, 0xFFC6B0E0);
                drawText(g, Component.translatable("screen.mdkcosmetic.module_filter_page", menu.filterPage() + 1, menu.filterPagesShown()), 286, 116, 142, 0xFFB9A7D0);
                drawText(g, txt(menu.magnetEnabled() ? "module_deactivate" : "module_activate"), 294, 141, 125, 0xFFECCFFF);
                drawText(g, txt(menu.whitelist() ? "module_accept" : "module_block"), 294, 163, 125, 0xFFECCFFF);
                drawText(g, txt(menu.exact() ? "module_exact" : "module_by_type"), 294, 185, 125, 0xFFECCFFF);
                drawText(g, txt("module_filter_hint1"), 281, 204, 150, 0xFFB69ACC);
                drawText(g, txt("module_filter_hint2"), 281, 216, 150, 0xFFB69ACC);
            } else if (kind == 2) {
                drawText(g, txt("module_crafting"), 292, 43, 132, 0xFFBBA5D2);
                drawText(g, txt("module_crafting_desc"), 282, 141, 152, 0xFFBBA5D2);
            } else if (kind == 3) {
                drawText(g, txt("module_furnace"), 292, 43, 132, 0xFFBBA5D2);
                drawText(g, txt("module_furnace_desc"), 282, 152, 150, 0xFFBBA5D2);
            } else if (kind == 4 || kind == 5) {
                drawText(g, txt(kind == 4 ? "module_totem_desc" : "module_death_vault_desc"), 282, 56, 144, 0xFFBBA5D2);
                drawText(g, txt((kind == 4 ? menu.totemEnabled() : menu.deathVaultEnabled()) ? "module_deactivate" : "module_activate"), 324, 102, 90, 0xFFECCFFF);
            }
        }
    }

    private void renderRemoteCounts(GuiGraphics g) {
        if (!menu.isRemote()) return;
        g.flush();
        g.pose().pushPose();
        g.pose().translate(0.0F, 0.0F, 350.0F);
        for (int i = 0; i < 27; i++) {
            long n = menu.quantity(i);
            if (n <= 1) continue;
            String text = n >= 1000000 ? n / 1000000 + "m" : n >= 1000 ? n / 1000 + "k" : Long.toString(n);
            g.drawString(font, text, leftPos + 24 + (i % 9) * 18 - font.width(text),
                    topPos + 44 + (i / 9) * 18, 0xFFFFFFFF, true);
        }
        g.flush();
        g.pose().popPose();
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        double vx = mx / scale;
        double vy = my / scale;
        int x = (int) Math.floor(vx) - leftPos;
        int y = (int) Math.floor(vy) - topPos;
        if (button == 0) {
            if (menu.isRemote() && x >= MEMORY_SCROLL_X && x < MEMORY_SCROLL_X + 10
                    && y >= MEMORY_SCROLL_Y && y < MEMORY_SCROLL_Y + MEMORY_SCROLL_H) {
                draggingMemory = true;
                scrollMemory(vy);
                return true;
            }
            if (x >= 214 && x < 232) {
                for (int i = 0; i < 5; i++) if (y >= 34 + i * 22 && y < 50 + i * 22) {
                    if (!menu.moduleAt(i).isEmpty()) clickButton(3000 + i);
                    return true;
                }
            }
            if (x >= 243 && x < 262 && y >= 208 && y < 226) {
                clickButton(5);
                return true;
            }
            if (menu.canUpgrade() && x >= 186 && x < 262 && y >= 164 && y < 182) {
                clickButton(6);
                return true;
            }
            int kind = activeKind();
            if (kind == 1) {
                if (x >= FILTER_SCROLL_X && x < FILTER_SCROLL_X + 10 && y >= FILTER_SCROLL_Y && y < FILTER_SCROLL_Y + FILTER_SCROLL_H) {
                    draggingFilter = true;
                    scrollFilter(vy);
                    return true;
                }
                if (x >= 286 && x < 426) {
                    if (y >= 137 && y < 154) { clickButton(3); return true; }
                    if (y >= 159 && y < 176) { clickButton(2); return true; }
                    if (y >= 181 && y < 198) { clickButton(4); return true; }
                }
            }
            if (x >= 287 && x < 425 && y >= 94 && y < 116) {
                if (kind == 4) { clickButton(5002); return true; }
                if (kind == 5) { clickButton(5003); return true; }
            }
        }
        return super.mouseClicked(vx, vy, button);
    }

    @Override public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (button == 0 && draggingMemory) { scrollMemory(my / scale); return true; }
        if (button == 0 && draggingFilter) { scrollFilter(my / scale); return true; }
        return super.mouseDragged(mx / scale, my / scale, button, dx / scale, dy / scale);
    }
    @Override public boolean mouseReleased(double mx, double my, int button) {
        draggingMemory = false;
        draggingFilter = false;
        return super.mouseReleased(mx / scale, my / scale, button);
    }
    @Override public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        double x = mx / scale - leftPos;
        double y = my / scale - topPos;
        if (menu.isRemote() && x >= 7 && x < 179 && y >= 32 && y < 92) {
            int target = Math.clamp(menu.getPage() - (int) Math.signum(scrollY), 0, menu.getPages() - 1);
            if (target != menu.getPage()) clickButton(1000 + target);
            return true;
        }
        if (activeKind() == 1 && x >= 300 && x < 386 && y >= 53 && y < 111) {
            int target = Math.clamp(menu.filterPage() - (int) Math.signum(scrollY), 0, menu.filterPagesShown() - 1);
            if (target != menu.filterPage()) clickButton(4000 + target);
            return true;
        }
        return super.mouseScrolled(mx / scale, my / scale, scrollX, scrollY);
    }

    private void clickButton(int id) {
        if (minecraft != null && minecraft.gameMode != null)
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }
    @Override public boolean keyPressed(int key, int scan, int mods) {
        if (search != null && search.isFocused()) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { search.setFocused(false); return true; }
            search.keyPressed(key, scan, mods);
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }
    @Override public boolean charTyped(char code, int mods) {
        if (search != null && search.isFocused()) return search.charTyped(code, mods);
        return super.charTyped(code, mods);
    }
    // mdk: unified scaled canvas; native background callbacks must not draw it a second time.
    @Override public void renderBackground(GuiGraphics graphics,int mouseX,int mouseY,float partial) {}

    @Override public void render(GuiGraphics g, int mx, int my, float partial) {
        int mouseX = (int) Math.floor(mx / scale), mouseY = (int) Math.floor(my / scale);
        renderTransparentBackground(g);
        g.pose().pushPose();
        g.pose().scale(scale, scale, 1.0F);
        renderBg(g,partial,mouseX,mouseY);
        super.render(g, mouseX, mouseY, partial);
        renderRemoteCounts(g);
        renderTooltip(g, mouseX, mouseY);
        g.pose().popPose();
    }
}
