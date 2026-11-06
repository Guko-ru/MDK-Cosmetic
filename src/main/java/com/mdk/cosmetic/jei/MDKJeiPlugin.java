package com.mdk.cosmetic.jei;

import com.mdk.cosmetic.MDKCosmetic;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Util;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Ships inside MDK Cosmetic; discovered only when JEI is present, no companion addon. */
@JeiPlugin
public final class MDKJeiPlugin implements IModPlugin {
    private static String tr(String key) { return Component.translatable("jei.mdkcosmetic." + key).getString(); }
    public static final RecipeType<JeiData.BackpackUpgrade> BACKPACK_UPGRADE = RecipeType.create(MDKCosmetic.MOD_ID,"backpack_upgrade",JeiData.BackpackUpgrade.class);
    public static final RecipeType<JeiData.Sewing> SEWING = RecipeType.create(MDKCosmetic.MOD_ID, "tailoring", JeiData.Sewing.class);
    public static final RecipeType<JeiData.Processing> PROCESS = RecipeType.create(MDKCosmetic.MOD_ID, "collector_processing", JeiData.Processing.class);
    public static final RecipeType<JeiData.Trade> TRADE = RecipeType.create(MDKCosmetic.MOD_ID, "collector_trades", JeiData.Trade.class);
    public static final RecipeType<JeiData.Structure> SMALL = RecipeType.create(MDKCosmetic.MOD_ID, "collector_3x3", JeiData.Structure.class);
    public static final RecipeType<JeiData.Structure> LARGE = RecipeType.create(MDKCosmetic.MOD_ID, "collector_5x5_upgrade", JeiData.Structure.class);
    public static final RecipeType<JeiData.Structure> FINAL = RecipeType.create(MDKCosmetic.MOD_ID, "collector_void_final", JeiData.Structure.class);

    @Override public Identifier getPluginUid() { return Identifier.fromNamespaceAndPath(MDKCosmetic.MOD_ID, "integrated_jei"); }
    @Override public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new WorkCategory<>(BACKPACK_UPGRADE,"backpack_upgrade",new ItemStack(MDKCosmetic.COLLECTOR_BACKPACK_REINFORCED.get()),gui,3),
                new WorkCategory<>(SEWING, "tailoring", new ItemStack(MDKCosmetic.TAILORING_TABLE_ITEM.get()), gui, 0),
                new WorkCategory<>(PROCESS, "processing", new ItemStack(MDKCosmetic.ESSENCE_WORKTABLE_ITEM.get()), gui, 1),
                new WorkCategory<>(TRADE, "trades", new ItemStack(MDKCosmetic.COLLECTOR_LINK_ITEM.get()), gui, 2),
                new StructureCategory(SMALL, "structure_small", new ItemStack(MDKCosmetic.COLLECTOR_LINK_ITEM.get()), gui),
                new StructureCategory(LARGE, "structure_large", new ItemStack(MDKCosmetic.COLLECTOR_MEMORY_CORE_ITEM.get()), gui),
                new StructureCategory(FINAL, "structure_final", new ItemStack(MDKCosmetic.COLLECTOR_METEOR_BLOCK_ITEM.get()), gui));
    }
    @Override public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(BACKPACK_UPGRADE, JeiData.backpackUpgrades());
        registration.addRecipes(SEWING, JeiData.sewing());
        registration.addRecipes(PROCESS, JeiData.processing());
        registration.addRecipes(TRADE, JeiData.trades());
        registration.addRecipes(SMALL, List.of(JeiData.structure(false)));
        registration.addRecipes(LARGE, List.of(JeiData.structure(true)));
        registration.addRecipes(FINAL, List.of(JeiData.structure(2)));
    }
    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(MDKCosmetic.COLLECTOR_BACKPACK_BASIC.get()), BACKPACK_UPGRADE);
        registration.addRecipeCatalyst(new ItemStack(MDKCosmetic.TAILORING_TABLE_ITEM.get()), SEWING);
        registration.addRecipeCatalyst(new ItemStack(MDKCosmetic.ESSENCE_WORKTABLE_ITEM.get()), PROCESS);
        registration.addRecipeCatalyst(new ItemStack(MDKCosmetic.COLLECTOR_LINK_ITEM.get()), SMALL, TRADE);
        registration.addRecipeCatalyst(new ItemStack(MDKCosmetic.COLLECTOR_SOUL_STORAGE_ITEM.get()), SMALL, LARGE, TRADE);
        registration.addRecipeCatalyst(new ItemStack(MDKCosmetic.COLLECTOR_MEMORY_CORE_ITEM.get()), LARGE, FINAL);
        registration.addRecipeCatalyst(new ItemStack(MDKCosmetic.COLLECTOR_METEOR_BLOCK_ITEM.get()), FINAL);
    }

    private static final class WorkCategory<T> implements IRecipeCategory<T> {
        private final RecipeType<T> type;
        private final Component name;
        private final IDrawable icon, background;
        private final int kind;
        WorkCategory(RecipeType<T> type, String name, ItemStack display, IGuiHelper gui, int kind) {
            this.type=type; this.name=Component.translatable("jei.mdkcosmetic."+name); this.kind=kind;
            this.icon=gui.createDrawableIngredient(VanillaTypes.ITEM_STACK,display);
            this.background=gui.createBlankDrawable(166, kind == 2 ? 108 : 94);
        }
        @Override public RecipeType<T> getRecipeType() { return type; }
        @Override public Component getTitle() { return name; }
        @Override public int getWidth() { return background.getWidth(); }
        @Override public int getHeight() { return background.getHeight(); }
        @Override public IDrawable getIcon() { return icon; }
        private static void slot(IRecipeLayoutBuilder b, RecipeIngredientRole role, int x,int y,ItemStack stack) {
            b.addSlot(role,x,y).addItemStack(stack);
        }
        @Override public void setRecipe(IRecipeLayoutBuilder builder,T recipe,IFocusGroup focuses) {
            if (recipe instanceof JeiData.BackpackUpgrade r) {
                slot(builder,RecipeIngredientRole.INPUT,8,30,r.backpack());
                slot(builder,RecipeIngredientRole.INPUT,34,30,r.shards());
                slot(builder,RecipeIngredientRole.INPUT,61,30,r.coin());
                slot(builder,RecipeIngredientRole.OUTPUT,130,30,r.result());
            } else if (recipe instanceof JeiData.Sewing r) {
                slot(builder,RecipeIngredientRole.INPUT,13,31,r.fur());
                slot(builder,RecipeIngredientRole.INPUT,45,31,r.thread());
                slot(builder,RecipeIngredientRole.OUTPUT,127,31,r.result());
            } else if (recipe instanceof JeiData.Processing r) {
                slot(builder,RecipeIngredientRole.INPUT,19,31,r.input());
                slot(builder,RecipeIngredientRole.OUTPUT,127,31,r.result());
            } else if (recipe instanceof JeiData.Trade r) {
                slot(builder,RecipeIngredientRole.INPUT,19,39,r.coins());
                slot(builder,RecipeIngredientRole.OUTPUT,127,39,r.product());
            }
        }
        private void drawTradeCard(JeiData.Trade r, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
            var font = Minecraft.getInstance().font;
            long now = Util.getMillis();
            int base = 0xFFCCCCCC;
            graphics.fill(6, 8, 160, 99, 0xFF15111D);
            graphics.outline(6, 8, 154, 91, 0xFF5F4D82);
            graphics.text(font, tr("trade_head"), 12, 13, 0xFFE8DBFF, false);
            graphics.text(font, tr("trade_info"), 12, 24, 0xFFB6B0C7, false);

            graphics.fill(10, 35, 52, 77, 0xFF241D33);
            graphics.outline(10, 35, 42, 42, 0xFF6B5A8D);
            graphics.fill(104, 35, 146, 77, 0xFF241D33);
            graphics.outline(104, 35, 42, 42, 0xFF8B72C4);
            graphics.fill(63, 38, 98, 73, 0xFF1C1725);
            graphics.outline(63, 38, 35, 35, 0xFF4E4460);

            int arrowX = 68 + (int)((now / 90L) % 20L);
            graphics.fill(69, 54, 94, 56, 0xFF5E5870);
            graphics.fill(arrowX, 53, Math.min(95, arrowX + 8), 57, 0xFFE0B6FF);
            graphics.fill(90, 50, 96, 60, 0xFFD9C1FF);
            graphics.fill(95, 48, 98, 62, 0xFFD9C1FF);

            for (int i = 0; i < 4; i++) {
                double phase = (now / 220.0D) + i * 1.7D;
                int px = 114 + (int)Math.round(Math.cos(phase) * 10.0D);
                int py = 46 + (int)Math.round(Math.sin(phase * 1.3D) * 8.0D);
                graphics.fill(px, py, px + 2, py + 2, 0xFFDAB7FF);
            }

            int stress = Math.max(0, Math.min(100, r.stress()));
            graphics.text(font, tr("stress"), 11, 82, base, false);
            graphics.fill(11, 92, 147, 97, 0xFF30283E);
            graphics.fill(11, 92, 11 + Math.round(136f * stress / 100f), 97, 0xFFB07DFF);
            graphics.outline(11, 92, 136, 5, 0xFF54486A);
            graphics.text(font, stress + tr("stress_unit"), 120, 82, 0xFFEBD5FF, false);
            String label = font.plainSubstrByWidth(r.product().getHoverName().getString(), 100);
            graphics.text(font, label, 58, 67, 0xFFC8BCDD, false);
        }
        @Override public void draw(T recipe,IRecipeSlotsView view,GuiGraphicsExtractor graphics,double mouseX,double mouseY) {
            var font=Minecraft.getInstance().font;
            int c=0xFFCCCCCC;
            if (recipe instanceof JeiData.BackpackUpgrade) {
                graphics.text(font,tr("upgrade_info"),7,12,c,false);
                graphics.text(font,tr("upgrade_cost"),9,68,c,false);
                graphics.text(font,"→",103,35,c,false);
            } else if (recipe instanceof JeiData.Sewing) {
                graphics.text(font,tr("sewing_inputs"),11,14,c,false);
                graphics.text(font,"→",100,35,c,false);
                graphics.text(font,tr("sewing_info"),10,69,c,false);
            } else if (recipe instanceof JeiData.Processing) {
                graphics.text(font,tr("processing_inputs"),9,12,c,false);
                graphics.text(font,"→",94,35,c,false);
                graphics.text(font,tr("processing_info"),9,69,c,false);
            } else if (recipe instanceof JeiData.Trade r) {
                drawTradeCard(r, graphics, mouseX, mouseY);
            }
        }
    }

    /** Interactive, real block-model renderer in the JEI recipe view. */
    private static final class StructureCategory implements IRecipeCategory<JeiData.Structure> {
        private final RecipeType<JeiData.Structure> type;
        private final Component title;
        private final IDrawable icon, background;

        private float angle = 33f;
        private float elevation = 25f;
        private float zoom = 1.0f;
        private boolean autoSpin = true;
        private boolean onlyNew = false;
        private int visibleLayers = 0; // 0 = show all
        private boolean previousMouseDown = false;
        private boolean dragging = false;
        private boolean iconsFallback = false;
        private boolean previousRightDown = false;
        private double previousX, previousY;
        private double pressX, pressY;
        private double draggedDistance = 0.0D;
        private long lastRenderMillis = 0L;
        private ItemStack selectedStack = ItemStack.EMPTY;
        private JeiData.Cell selectedCell = null;

        private record ScreenCell(JeiData.Cell cell, double x, double y, double depth) {}

        StructureCategory(RecipeType<JeiData.Structure> type, String label, ItemStack item, IGuiHelper gui) {
            this.type = type;
            this.title = Component.translatable("jei.mdkcosmetic."+label);
            this.icon = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, item);
            this.background = gui.createBlankDrawable(166, 214);
        }
        @Override public RecipeType<JeiData.Structure> getRecipeType() { return type; }
        @Override public Component getTitle() { return title; }
        @Override public IDrawable getIcon() { return icon; }
        @Override public int getWidth() { return background.getWidth(); }
        @Override public int getHeight() { return background.getHeight(); }

        @Override public void setRecipe(IRecipeLayoutBuilder builder, JeiData.Structure recipe, IFocusGroup focuses) {
            Map<net.minecraft.world.item.Item, Integer> counts = new LinkedHashMap<>();
            for (JeiData.Cell cell : recipe.cells()) {
                if (!cell.item().isEmpty()) counts.merge(cell.item().getItem(), 1, Integer::sum);
            }
            int i = 0;
            for (var entry : counts.entrySet()) {
                ItemStack stack = new ItemStack(entry.getKey(), Math.min(99, entry.getValue()));
                builder.addSlot(RecipeIngredientRole.INPUT, 8 + (i % 7) * 22, 180 + (i / 7) * 19)
                        .addItemStack(stack);
                i++;
            }
        }

        private boolean hit(double mx, double my, int x1, int y1, int x2, int y2) {
            return mx >= x1 && mx < x2 && my >= y1 && my < y2;
        }
        private void drawControl(GuiGraphicsExtractor graphics, String label, int x, int y, int w, boolean selected) {
            graphics.fill(x,y,x+w,y+16, selected ? 0xB0504273 : 0xA02A303C);
            graphics.outline(x,y,w,16,selected ? 0xFFB996FF : 0xFF667080);
            graphics.centeredText(Minecraft.getInstance().font,label,x+w/2,y+4,0xFFECE5FF);
        }

        private List<ScreenCell> projectCells(JeiData.Structure recipe, int size) {
            final double yaw = Math.toRadians(angle);
            final double tilt = Math.toRadians(elevation);
            final double sinYaw = Math.sin(yaw), cosYaw = Math.cos(yaw);
            final double sinTilt = Math.sin(tilt), cosTilt = Math.cos(tilt);
            final float pitch = (size == 3 ? 20f : 14f) * zoom;
            List<ScreenCell> projected = new ArrayList<>();
            for (JeiData.Cell cell : recipe.cells()) {
                if (visibleLayers > 0 && cell.y() + size / 2 + 1 > visibleLayers) continue;
                if (onlyNew && recipe.upgrade() && cell.original()) continue;
                if (cell.item().isEmpty()) continue;
                double right = cell.x() * cosYaw - cell.z() * sinYaw;
                double back = cell.x() * sinYaw + cell.z() * cosYaw;
                double raised = cell.y() * cosTilt + back * sinTilt;
                double depth = back * cosTilt - cell.y() * sinTilt;
                projected.add(new ScreenCell(cell, 83 + right * pitch, 76 - raised * pitch, depth));
            }
            projected.sort(Comparator.comparingDouble(ScreenCell::depth).reversed());
            return projected;
        }

        private void selectNearest(JeiData.Structure recipe, double x, double y, int size) {
            double maxDistSq = size == 3 ? 13.0D * 13.0D : 10.0D * 10.0D;
            ScreenCell best = null;
            double bestDistSq = maxDistSq;
            for (ScreenCell cell : projectCells(recipe, size)) {
                double dx = x - cell.x();
                double dy = y - cell.y();
                double distSq = dx * dx + dy * dy;
                if (distSq <= bestDistSq) {
                    bestDistSq = distSq;
                    best = cell;
                }
            }
            if (best != null) {
                selectedCell = best.cell();
                selectedStack = best.cell().item().copy();
            }
        }

        private void handleMouse(JeiData.Structure recipe, double x, double y) {
            boolean down = Minecraft.getInstance().mouseHandler.isLeftPressed();
            boolean newlyPressed = down && !previousMouseDown;
            boolean rightDown = Minecraft.getInstance().mouseHandler.isRightPressed();
            if (rightDown && !previousRightDown && hit(x, y, 5, 21, 161, 133)) {
                iconsFallback = !iconsFallback;
            }
            previousRightDown = rightDown;
            int size = recipe.size();
            if (newlyPressed) {
                if (hit(x,y,7,157,35,173)) zoom=Math.max(0.55f,zoom-0.15f);
                else if (hit(x,y,38,157,66,173)) zoom=Math.min(1.8f,zoom+0.15f);
                else if (hit(x,y,69,157,116,173)) autoSpin=!autoSpin;
                else if (hit(x,y,119,157,159,173)) visibleLayers=(visibleLayers+1)%(size+1);
                else if (recipe.upgrade() && hit(x,y,7,136,152,153)) onlyNew=!onlyNew;
                else if (hit(x,y,5,21,161,133)) {
                    dragging=true;
                    previousX=x;
                    previousY=y;
                    pressX=x;
                    pressY=y;
                    draggedDistance=0.0D;
                }
            }
            if (down && dragging) {
                double dx = x - previousX;
                double dy = y - previousY;
                draggedDistance += Math.abs(dx) + Math.abs(dy);
                if (draggedDistance > 2.0D) {
                    autoSpin=false;
                    angle += (float)(dx * 1.2f);
                    elevation = Math.max(-80f,Math.min(80f,elevation+(float)(dy*0.9f)));
                }
                previousX=x;
                previousY=y;
            }
            if (!down && dragging) {
                if (draggedDistance <= 3.0D && hit(pressX, pressY, 5, 21, 161, 133)) {
                    selectNearest(recipe, pressX, pressY, size);
                }
                dragging=false;
            }
            previousMouseDown=down;
        }

        private void renderRigidModel(JeiData.Structure recipe, GuiGraphicsExtractor graphics, int size) {
            var cells=new java.util.ArrayList<com.mdk.cosmetic.client.MultiblockPreview.Cell>();
            for(var cell:recipe.cells()){
                if(visibleLayers>0&&cell.y()+size/2+1>visibleLayers)continue;
                if(onlyNew&&recipe.upgrade()&&cell.original())continue;
                if(!cell.item().isEmpty())cells.add(new com.mdk.cosmetic.client.MultiblockPreview.Cell(cell.x(),cell.y(),cell.z(),cell.item().copy()));
            }
            com.mdk.cosmetic.client.MultiblockPreview.submit(graphics,cells,angle,elevation,(size==3?20F:12F)*zoom);
        }

        private void renderLegacyIcons(JeiData.Structure recipe, GuiGraphicsExtractor graphics, int size) {
            final float iconSize = (size == 3 ? 21f : 15f) * zoom;
            graphics.nextStratum();
            for (ScreenCell projected : projectCells(recipe, size)) {
                float left = (float)(projected.x() - iconSize * 0.5f);
                float top = (float)(projected.y() - iconSize * 0.5f);
                if (left < 5 || top < 20 || left + iconSize > 161 || top + iconSize > 134) continue;
                var pose = graphics.pose();
                pose.pushMatrix();
                try {
                    pose.translate(left, top);
                    pose.scale(iconSize / 16f, iconSize / 16f);
                    graphics.item(projected.cell().item(), 0, 0);
                } finally { pose.popMatrix(); }
            }
            graphics.nextStratum();
        }

        private void drawSelectionPanel(GuiGraphicsExtractor graphics, double mx, double my, JeiData.Structure recipe) {
            var font = Minecraft.getInstance().font;
            graphics.fill(8, 23, 78, 43, 0xC0181322);
            graphics.outline(8, 23, 70, 20, 0xFF6E5B92);
            if (selectedStack.isEmpty()) {
                graphics.text(font, tr("pick_block"), 12, 27, 0xFFE8DFFF, false);
                graphics.text(font, tr("pick_info"), 12, 35, 0xFFBAACC9, false);
                return;
            }
            graphics.item(selectedStack, 11, 25);
            graphics.itemDecorations(font, selectedStack, 11, 25);
            String label = font.plainSubstrByWidth(selectedStack.getHoverName().getString(), 42);
            graphics.text(font, label, 31, 31, 0xFFF0E6FF, false);
            if (hit(mx, my, 11, 25, 27, 41)) {
                graphics.setTooltipForNextFrame(font, selectedStack, (int)mx, (int)my);
            }

            if (selectedCell != null) {
                for (ScreenCell cell : projectCells(recipe, recipe.size())) {
                    if (cell.cell().equals(selectedCell)) {
                        int left = (int)Math.round(cell.x() - 8);
                        int top = (int)Math.round(cell.y() - 8);
                        graphics.outline(left, top, 16, 16, 0xFFCCA7FF);
                        break;
                    }
                }
            }
        }

        @Override public void draw(JeiData.Structure recipe, IRecipeSlotsView view, GuiGraphicsExtractor graphics, double mx, double my) {
            int size=recipe.size();
            handleMouse(recipe,mx,my);
            long now=Util.getMillis();
            if (autoSpin && lastRenderMillis>0) angle+=(Math.min(100,now-lastRenderMillis)*0.015f);
            lastRenderMillis=now;
            graphics.text(Minecraft.getInstance().font,
                    recipe.finalTier() ? tr("final_title") : (recipe.upgrade() ? tr("upgrade_title") : tr("base_title")),
                    6,6,0xFFE7DAFF,false);
            graphics.fill(4,19,162,135,0xA0101623);
            graphics.outline(4,19,158,116,0xFF4B5567);

            int visibleCount = 0;
            for (JeiData.Cell cell : recipe.cells()) {
                if (visibleLayers > 0 && cell.y() + size / 2 + 1 > visibleLayers) continue;
                if (onlyNew && recipe.upgrade() && cell.original()) continue;
                visibleCount++;
            }
            if (iconsFallback) {
                renderLegacyIcons(recipe, graphics, size);
            } else {
                renderRigidModel(recipe, graphics, size);
            }
            drawSelectionPanel(graphics, mx, my, recipe);
            graphics.text(Minecraft.getInstance().font,
                    (iconsFallback ? tr("icons") : "3D") + ": " + visibleCount + tr("blocks_hint"),
                    9, 122, 0xFFD1D8E7, false);
            if (recipe.upgrade()) drawControl(graphics,onlyNew ? tr("new_only") : tr("show_upgrade"),7,137,145,onlyNew);
            if (!recipe.upgrade()) graphics.text(Minecraft.getInstance().font,tr("interaction"),7,143,0xFFC3C8D5,false);
            drawControl(graphics,"−",7,157,28,false);
            drawControl(graphics,"+",38,157,28,false);
            drawControl(graphics,autoSpin?tr("auto_on"):tr("auto_off"),69,157,47,autoSpin);
            drawControl(graphics,visibleLayers==0?tr("all"):visibleLayers+tr("layers_unit"),119,157,40,visibleLayers!=0);
            graphics.text(Minecraft.getInstance().font,tr("blocks_title"),7,174,0xFFD8D0E6,false);
            graphics.text(Minecraft.getInstance().font,
                    recipe.finalTier() ? tr("final_portal_tip") : (visibleLayers==0?tr("all_layers"):tr("lower_layers")+visibleLayers),
                    7,204,0xFF9BA7BA,false);
        }
    }

}
