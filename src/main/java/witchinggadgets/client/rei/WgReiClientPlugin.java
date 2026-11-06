package witchinggadgets.client.rei;

import java.util.ArrayList;
import java.util.List;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import witchinggadgets.compat.rei.BlastFurnaceDisplay;
import witchinggadgets.compat.rei.SpinningDisplay;
import witchinggadgets.compat.rei.WgRei;
import witchinggadgets.recipeview.WgRecipeCatalog;
import witchinggadgets.spinning.SpinningFeature;

@REIPluginClient
public final class WgReiClientPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new SpinningCategory(), new BlastFurnaceCategory());
        registry.addWorkstations(WgRei.SPINNING, EntryStacks.of(SpinningFeature.SPINNING_WHEEL.get()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        WgRecipeCatalog.spinning().forEach(recipe -> registry.add(new SpinningDisplay(recipe)));
        WgRecipeCatalog.blastFurnace().forEach(view -> registry.add(new BlastFurnaceDisplay(view)));
    }

    @Override
    public void registerBasicEntryFiltering(BasicFilteringRule<?> rule) {
        rule.hide(() -> WgRecipeCatalog.hiddenStacks().stream().<EntryStack<?>>map(EntryStacks::of).toList());
    }

    private static final class SpinningCategory extends Category<SpinningDisplay> {
        SpinningCategory() {
            super(WgRei.SPINNING, "block.witchinggadgets.spinning_wheel", SpinningFeature.SPINNING_WHEEL.get(), WgRecipeCatalog.SPINNING_TEXTURE, WgRecipeCatalog.SPINNING_HEIGHT);
        }

        @Override
        void slots(SpinningDisplay display, Point origin, List<Widget> widgets) {
            List<EntryIngredient> inputs = display.getInputEntries();
            for (int index = 0; index < inputs.size(); index++) {
                slot(widgets, origin, WgRecipeCatalog.SPINNING_INPUT_X, WgRecipeCatalog.SPINNING_INPUT_Y + 18 * index, inputs.get(index), true);
            }
            slot(widgets, origin, WgRecipeCatalog.SPINNING_OUTPUT_X, WgRecipeCatalog.SPINNING_OUTPUT_Y, display.getOutputEntries().getFirst(), false);
        }
    }

    private static final class BlastFurnaceCategory extends Category<BlastFurnaceDisplay> {
        BlastFurnaceCategory() {
            super(WgRei.BLAST_FURNACE, "block.witchinggadgets.infernal_blast_furnace", Blocks.NETHER_BRICKS, WgRecipeCatalog.BLAST_FURNACE_TEXTURE, WgRecipeCatalog.BLAST_FURNACE_HEIGHT);
        }

        @Override
        void slots(BlastFurnaceDisplay display, Point origin, List<Widget> widgets) {
            List<EntryIngredient> outputs = display.getOutputEntries();
            slot(widgets, origin, WgRecipeCatalog.BLAST_INPUT_X, WgRecipeCatalog.BLAST_INPUT_Y, display.getInputEntries().getFirst(), true);
            slot(widgets, origin, WgRecipeCatalog.BLAST_OUTPUT_X, WgRecipeCatalog.BLAST_OUTPUT_Y, outputs.getFirst(), false);
            if (outputs.size() > 1) {
                slot(widgets, origin, WgRecipeCatalog.BLAST_BONUS_X, WgRecipeCatalog.BLAST_BONUS_Y, outputs.get(1), false);
            }
        }
    }

    private abstract static class Category<D extends Display> implements DisplayCategory<D> {
        private static final int PADDING = 4;

        private final CategoryIdentifier<D> id;
        private final String title;
        private final Renderer icon;
        private final Identifier texture;
        private final int height;

        Category(CategoryIdentifier<D> id, String title, ItemLike icon, Identifier texture, int height) {
            this.id = id;
            this.title = title;
            this.icon = EntryStacks.of(icon);
            this.texture = texture;
            this.height = height;
        }

        abstract void slots(D display, Point origin, List<Widget> widgets);

        static void slot(List<Widget> widgets, Point origin, int x, int y, EntryIngredient entries, boolean input) {
            Slot slot = Widgets.createSlot(new Point(origin.x + x, origin.y + y)).entries(entries).disableBackground();
            widgets.add(input ? slot.markInput() : slot.markOutput());
        }

        @Override
        public CategoryIdentifier<? extends D> getCategoryIdentifier() {
            return id;
        }

        @Override
        public Component getTitle() {
            return Component.translatable(title);
        }

        @Override
        public Renderer getIcon() {
            return icon;
        }

        @Override
        public int getDisplayWidth(D display) {
            return WgRecipeCatalog.WIDTH + PADDING * 2;
        }

        @Override
        public int getDisplayHeight() {
            return height + PADDING * 2;
        }

        @Override
        public List<Widget> setupDisplay(D display, Rectangle bounds) {
            List<Widget> widgets = new ArrayList<>();
            widgets.add(Widgets.createRecipeBase(bounds));
            Point origin = new Point(bounds.x + PADDING, bounds.y + PADDING);
            widgets.add(Widgets.createTexturedWidget(texture, origin.x, origin.y, WgRecipeCatalog.BACKGROUND_U, WgRecipeCatalog.BACKGROUND_V, WgRecipeCatalog.WIDTH, height));
            slots(display, origin, widgets);
            return widgets;
        }
    }
}
