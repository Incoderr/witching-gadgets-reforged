package witchinggadgets.client.research;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.jspecify.annotations.Nullable;
import thaumcraft.client.research.ResearchBrowserScreen;
import thaumcraft.client.research.ResearchPageScreen;
import thaumcraft.crafting.ArcaneRecipe;
import thaumcraft.crafting.CraftingRecipeRef;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.ResearchPage;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.client.WgClientConfig;
import witchinggadgets.client.platform.WgGuiBlit;

@EventBusSubscriber(modid = WitchingGadgets.MODID, value = Dist.CLIENT)
public final class ThaumonomiconSearch {
    private static final Identifier PARCHMENT = Identifier.fromNamespaceAndPath("thaumcraft", "textures/misc/parchment3.png");
    private static final Identifier FRAME = Identifier.fromNamespaceAndPath("thaumcraft", "textures/gui/guiresearchtable2.png");
    private static final int LINES = 20;

    private record Result(String research, @Nullable String display) {
        String label() {
            ResearchItem item = ResearchCategories.getResearch(research);
            return display != null ? display : item == null ? research : I18n.get(item.nameKey());
        }
    }

    private static @Nullable EditBox field;
    private static List<Result> results = new ArrayList<>();
    private static int offset;
    private static String query = "";

    private ThaumonomiconSearch() {
    }

    private static final class SearchBox extends EditBox {
        SearchBox(Font font, int x, int y) {
            super(font, x, y, 103, 13, Component.translatable("wg.gui.search"));
        }

        @Override
        public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            WgGuiBlit.blit(graphics, FRAME, getX() - 2, getY() - 4, 94, 8, getWidth() + 8, getHeight());
            WgGuiBlit.blit(graphics, FRAME, getX() - 2, getY() + getHeight() - 4, 138, 158, getWidth() + 8, 2);
            WgGuiBlit.blit(graphics, FRAME, getX() + getWidth() + 6, getY() - 4, 244, 136, 2, getHeight() + 2);
            if (results.isEmpty() && !isFocused() && getValue().isEmpty()) {
                graphics.text(Minecraft.getInstance().font, Component.translatable("wg.gui.search"), getX(), getY(), 0xFF777777, false);
            }
            super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);
        }
    }

    private static int pane(ResearchBrowserScreen screen, String method, int fallback) {
        try {
            Method getter = ResearchBrowserScreen.class.getDeclaredMethod(method);
            getter.setAccessible(true);
            return (int) getter.invoke(screen);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return fallback;
        }
    }

    private static int left(ResearchBrowserScreen screen) {
        return (screen.width - pane(screen, "paneWidth", 256)) / 2;
    }

    private static int top(ResearchBrowserScreen screen) {
        return (screen.height - pane(screen, "paneHeight", 230)) / 2;
    }

    private static int listX(ResearchBrowserScreen screen) {
        int paneWidth = pane(screen, "paneWidth", 256);
        int x = left(screen) + paneWidth + (ResearchCategories.all().size() > 9 ? 24 : 0);
        return screen.width - x >= 120 ? x : left(screen) + paneWidth - Math.min(224, paneWidth / 2) - 8;
    }

    private static int listTop(ResearchBrowserScreen screen) {
        return top(screen);
    }

    private static void place(ResearchBrowserScreen screen) {
        if (field != null) {
            field.setX(left(screen) + pane(screen, "paneWidth", 256) / 2);
            field.setY(top(screen) + 5);
        }
    }

    @SubscribeEvent
    static void onInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof ResearchBrowserScreen screen)) {
            field = null;
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        EditBox box = new SearchBox(minecraft.font, 0, 0);
        box.setBordered(false);
        box.setMaxLength(40);
        box.setTextColor(0xFFFFFFFF);
        box.setValue(query);
        results = search(query, WgClientConfig.limitBookSearch() ? activeCategory() : null);
        offset = Math.max(0, Math.min(offset, results.size() - LINES));
        box.setResponder(value -> {
            query = value;
            offset = 0;
            results = search(value, WgClientConfig.limitBookSearch() ? activeCategory() : null);
        });
        field = box;
        place(screen);
        event.addListener(box);
    }

    @SubscribeEvent
    static void onRender(ScreenEvent.Render.Post event) {
        if (field == null || !(event.getScreen() instanceof ResearchBrowserScreen screen)) {
            return;
        }
        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        place(screen);
        field.extractRenderState(graphics, event.getMouseX(), event.getMouseY(), event.getPartialTick());
        if (results.isEmpty()) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        int x = listX(screen);
        int y = listTop(screen);
        int maxWidth = Math.min(screen.width - x, 224);
        WgGuiBlit.region(graphics, PARCHMENT, x, y, maxWidth, 230, 0, 0, 150, 150, 256, 256);
        for (int line = 0; line < LINES && line + offset < results.size(); line++) {
            Result result = results.get(line + offset);
            String label = font.plainSubstrByWidth(result.label(), maxWidth - 10);
            Component text = result.display() != null ? Component.literal(label).withStyle(ChatFormatting.DARK_GRAY) : Component.literal(label);
            graphics.text(font, text, x + 6, y + 6 + line * 11, 0xFFFFFFFF, false);
        }
    }

    @SubscribeEvent
    static void onMouse(ScreenEvent.MouseButtonPressed.Pre event) {
        if (field == null || !(event.getScreen() instanceof ResearchBrowserScreen screen)) {
            return;
        }
        MouseButtonEvent mouse = event.getMouseButtonEvent();
        place(screen);
        if (field.isMouseOver(mouse.x(), mouse.y())) {
            if (mouse.button() == 1) {
                field.setValue("");
            }
            screen.setFocused(field);
            field.setFocused(true);
            field.mouseClicked(mouse, false);
            event.setCanceled(true);
            return;
        }
        if (field.isFocused()) {
            field.setFocused(false);
            screen.setFocused(null);
        }
        if (mouse.x() > listX(screen) && mouse.y() > listTop(screen) && mouse.y() < listTop(screen) + 230 && !results.isEmpty()) {
            int selected = (int) ((mouse.y() - (listTop(screen) + 6)) / 11) + offset;
            if (selected >= 0 && selected < results.size()) {
                ResearchItem item = ResearchCategories.getResearch(results.get(selected).research());
                if (item != null) {
                    ResearchPageScreen.open(item);
                }
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onScroll(ScreenEvent.MouseScrolled.Pre event) {
        if (field == null || !(event.getScreen() instanceof ResearchBrowserScreen screen) || event.getMouseX() <= listX(screen) || results.isEmpty()) {
            return;
        }
        offset += event.getScrollDeltaY() < 0 ? 1 : -1;
        offset = Math.max(0, Math.min(offset, results.size() - LINES));
        event.setCanceled(true);
    }

    @SubscribeEvent
    static void onKey(ScreenEvent.KeyPressed.Pre event) {
        if (field != null && field.isFocused() && event.getScreen() instanceof ResearchBrowserScreen && !event.getKeyEvent().isEscape()) {
            field.keyPressed(event.getKeyEvent());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onChar(ScreenEvent.CharacterTyped.Pre event) {
        if (field != null && field.isFocused() && event.getScreen() instanceof ResearchBrowserScreen) {
            field.charTyped(event.getCharacterEvent());
            event.setCanceled(true);
        }
    }

    private static @Nullable String activeCategory() {
        try {
            Field selected = ResearchBrowserScreen.class.getDeclaredField("selectedCategory");
            selected.setAccessible(true);
            return (String) selected.get(null);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    private static List<Result> search(String query, @Nullable String category) {
        List<Result> valid = new ArrayList<>();
        Minecraft minecraft = Minecraft.getInstance();
        if (query == null || query.isEmpty() || minecraft.player == null) {
            return valid;
        }
        String lower = query.toLowerCase();
        Collection<ResearchItem> items = new ArrayList<>();
        for (ResearchItem item : ResearchCategories.allResearch()) {
            if (category == null || item.category().equals(category)) {
                items.add(item);
            }
        }
        Set<String> used = new HashSet<>();
        for (ResearchItem item : items) {
            String key = item.key();
            if (!ResearchManager.isComplete(minecraft.player, key) || !net.minecraft.locale.Language.getInstance().has(item.nameKey())) {
                continue;
            }
            String name = I18n.get(item.nameKey());
            List<Result> recipeBased = new ArrayList<>();
            for (ResearchPage page : item.pages()) {
                for (String recipe : recipes(page)) {
                    ItemStack output = output(TcRecipes.get(recipe));
                    if (output.isEmpty()) {
                        continue;
                    }
                    String display = output.getHoverName().getString();
                    if (display.toLowerCase().contains(lower) && used.add(display)) {
                        recipeBased.add(new Result(key, "Item: " + display));
                    }
                }
            }
            boolean added = false;
            if (recipeBased.size() <= 1 && !used.contains(name) && (key.toLowerCase().contains(lower) || name.toLowerCase().contains(lower))) {
                valid.add(new Result(key, null));
                used.add(name);
                added = true;
            }
            if (!added) {
                valid.addAll(recipeBased);
            }
        }
        valid.sort((first, second) -> first.label().compareToIgnoreCase(second.label()));
        return valid;
    }

    private static List<String> recipes(ResearchPage page) {
        return switch (page) {
            case ResearchPage.Crafting crafting -> crafting.recipes();
            case ResearchPage.Arcane arcane -> arcane.recipes();
            case ResearchPage.Crucible crucible -> crucible.recipes();
            case ResearchPage.Infusion infusion -> infusion.recipes();
            default -> List.of();
        };
    }

    private static ItemStack output(@Nullable TcRecipe recipe) {
        TcStack result = switch (recipe) {
            case ArcaneRecipe arcane -> arcane.result();
            case CrucibleRecipe crucible -> crucible.result();
            case InfusionRecipe infusion -> infusion.result();
            case CraftingRecipeRef crafting -> crafting.result();
            case null, default -> null;
        };
        return result == null ? ItemStack.EMPTY : result.create();
    }
}
