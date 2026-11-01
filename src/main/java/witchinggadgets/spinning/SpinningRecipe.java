package witchinggadgets.spinning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;

public record SpinningRecipe(String key, TcStack output, List<TcIngredient> inputs) {
    private static final List<SpinningRecipe> RECIPES = new ArrayList<>();

    public static SpinningRecipe register(String key, TcStack output, TcIngredient... inputs) {
        for (SpinningRecipe recipe : RECIPES) {
            if (recipe.key().equals(key)) {
                throw new IllegalStateException("Spinning recipe " + key + " is already registered");
            }
        }
        SpinningRecipe recipe = new SpinningRecipe(key, output, List.of(inputs));
        RECIPES.add(recipe);
        return recipe;
    }

    public static List<SpinningRecipe> all() {
        return Collections.unmodifiableList(RECIPES);
    }

    public static @Nullable SpinningRecipe find(List<ItemStack> stacks) {
        List<ItemStack> present = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                present.add(stack);
            }
        }
        for (SpinningRecipe recipe : RECIPES) {
            if (recipe.matches(present)) {
                return recipe;
            }
        }
        return null;
    }

    public boolean matches(List<ItemStack> stacks) {
        if (stacks.size() != inputs.size()) {
            return false;
        }
        List<TcIngredient> remaining = new ArrayList<>(inputs);
        for (ItemStack stack : stacks) {
            boolean found = false;
            for (int index = 0; index < remaining.size(); index++) {
                if (remaining.get(index).test(stack)) {
                    remaining.remove(index);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return remaining.isEmpty();
    }
}
