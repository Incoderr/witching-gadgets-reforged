package witchinggadgets.client.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.recipeview.BlastFurnaceView;
import witchinggadgets.recipeview.WgRecipeCatalog;

final class BlastFurnaceCategory implements IRecipeCategory<BlastFurnaceView> {
    static final IRecipeType<BlastFurnaceView> TYPE = IRecipeType.create(WitchingGadgets.id("infernal_blast_furnace"), BlastFurnaceView.class);

    private final IDrawable background;
    private final IDrawable icon;

    BlastFurnaceCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(WgRecipeCatalog.BLAST_FURNACE_TEXTURE, WgRecipeCatalog.BACKGROUND_U, WgRecipeCatalog.BACKGROUND_V, WgRecipeCatalog.WIDTH, WgRecipeCatalog.BLAST_FURNACE_HEIGHT);
        this.icon = helper.createDrawableItemLike(Blocks.NETHER_BRICKS);
    }

    @Override
    public IRecipeType<BlastFurnaceView> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.witchinggadgets.infernal_blast_furnace");
    }

    @Override
    public int getWidth() {
        return WgRecipeCatalog.WIDTH;
    }

    @Override
    public int getHeight() {
        return WgRecipeCatalog.BLAST_FURNACE_HEIGHT;
    }

    @Override
    public boolean needsRecipeBorder() {
        return false;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlastFurnaceView recipe, IFocusGroup focuses) {
        builder.addInputSlot(WgRecipeCatalog.BLAST_INPUT_X, WgRecipeCatalog.BLAST_INPUT_Y).addItemStacks(recipe.inputs());
        builder.addOutputSlot(WgRecipeCatalog.BLAST_OUTPUT_X, WgRecipeCatalog.BLAST_OUTPUT_Y).add(recipe.output());
        if (!recipe.bonus().isEmpty()) {
            builder.addOutputSlot(WgRecipeCatalog.BLAST_BONUS_X, WgRecipeCatalog.BLAST_BONUS_Y).add(recipe.bonus());
        }
    }

    @Override
    public void draw(BlastFurnaceView recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        background.draw(graphics);
    }
}
