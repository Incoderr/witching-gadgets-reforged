package witchinggadgets.client.jei;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import thaumcraft.crafting.TcIngredient;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.recipeview.WgRecipeCatalog;
import witchinggadgets.spinning.SpinningFeature;
import witchinggadgets.spinning.SpinningRecipe;

final class SpinningCategory implements IRecipeCategory<SpinningRecipe> {
    static final IRecipeType<SpinningRecipe> TYPE = IRecipeType.create(WitchingGadgets.id("spinning_wheel"), SpinningRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    SpinningCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(WgRecipeCatalog.SPINNING_TEXTURE, WgRecipeCatalog.BACKGROUND_U, WgRecipeCatalog.BACKGROUND_V, WgRecipeCatalog.WIDTH, WgRecipeCatalog.SPINNING_HEIGHT);
        this.icon = helper.createDrawableItemLike(SpinningFeature.SPINNING_WHEEL.get());
    }

    @Override
    public IRecipeType<SpinningRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.witchinggadgets.spinning_wheel");
    }

    @Override
    public int getWidth() {
        return WgRecipeCatalog.WIDTH;
    }

    @Override
    public int getHeight() {
        return WgRecipeCatalog.SPINNING_HEIGHT;
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
    public void setRecipe(IRecipeLayoutBuilder builder, SpinningRecipe recipe, IFocusGroup focuses) {
        List<TcIngredient> inputs = recipe.inputs();
        for (int index = 0; index < inputs.size(); index++) {
            builder.addInputSlot(WgRecipeCatalog.SPINNING_INPUT_X, WgRecipeCatalog.SPINNING_INPUT_Y + 18 * index).addItemStacks(inputs.get(index).displayStacks());
        }
        ItemStack output = recipe.output().create();
        builder.addOutputSlot(WgRecipeCatalog.SPINNING_OUTPUT_X, WgRecipeCatalog.SPINNING_OUTPUT_Y).add(output);
    }

    @Override
    public void draw(SpinningRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        background.draw(graphics);
    }
}
