package witchinggadgets.client.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.recipeview.WgRecipeCatalog;
import witchinggadgets.spinning.SpinningFeature;

@JeiPlugin
public final class WgJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return WitchingGadgets.id("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new SpinningCategory(helper), new BlastFurnaceCategory(helper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(SpinningCategory.TYPE, WgRecipeCatalog.spinning());
        registration.addRecipes(BlastFurnaceCategory.TYPE, WgRecipeCatalog.blastFurnace());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(SpinningCategory.TYPE, SpinningFeature.SPINNING_WHEEL.get());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, WgRecipeCatalog.hiddenStacks());
    }
}
