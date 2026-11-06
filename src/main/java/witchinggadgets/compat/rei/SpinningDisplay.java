package witchinggadgets.compat.rei;

import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import witchinggadgets.recipeview.WgRecipeCatalog;
import witchinggadgets.spinning.SpinningRecipe;

public final class SpinningDisplay extends BasicDisplay {
    public static final DisplaySerializer<SpinningDisplay> SERIALIZER = WgRei.byKey(key -> {
        SpinningRecipe recipe = WgRecipeCatalog.spinning(key);
        return recipe == null ? null : new SpinningDisplay(recipe);
    }, display -> display.recipe.key());

    private final SpinningRecipe recipe;

    public SpinningDisplay(SpinningRecipe recipe) {
        super(recipe.inputs().stream().map(input -> WgRei.stacks(input.displayStacks())).toList(), List.of(WgRei.stack(recipe.output().create())), Optional.empty());
        this.recipe = recipe;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return WgRei.SPINNING;
    }

    @Override
    public DisplaySerializer<SpinningDisplay> getSerializer() {
        return SERIALIZER;
    }
}
