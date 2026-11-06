package witchinggadgets.furnace;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record BlastFurnaceRecipe(String input, int inputCount, String output, int outputCount, int time, boolean special, @Nullable String bonus) {
    private static final List<BlastFurnaceRecipe> RECIPES = new ArrayList<>();

    public static BlastFurnaceRecipe add(String input, int inputCount, String output, int outputCount, int time, boolean special, @Nullable String bonus) {
        BlastFurnaceRecipe recipe = new BlastFurnaceRecipe(input, inputCount, output, outputCount, time, special, bonus);
        RECIPES.add(recipe);
        return recipe;
    }

    public static List<BlastFurnaceRecipe> all() {
        return List.copyOf(RECIPES);
    }

    public static @Nullable BlastFurnaceRecipe forInput(ItemStack stack) {
        for (BlastFurnaceRecipe recipe : RECIPES) {
            if (recipe.matches(stack) && !recipe.result().isEmpty()) {
                return recipe;
            }
        }
        return null;
    }

    private static boolean isTag(String id) {
        return id.startsWith("#");
    }

    private static TagKey<Item> tag(String id) {
        return TagKey.create(Registries.ITEM, Identifier.parse(id.substring(1)));
    }

    public static Optional<Item> resolve(@Nullable String id) {
        if (id == null) {
            return Optional.empty();
        }
        if (isTag(id)) {
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag(id))) {
                return Optional.of(holder.value());
            }
            return Optional.empty();
        }
        return BuiltInRegistries.ITEM.getOptional(Identifier.parse(id));
    }

    public boolean matches(ItemStack stack) {
        if (stack.isEmpty() || stack.getCount() < inputCount) {
            return false;
        }
        if (isTag(input)) {
            return stack.is(tag(input));
        }
        return resolve(input).map(stack::is).orElse(false);
    }

    public ItemStack result() {
        return resolve(output).map(item -> new ItemStack(item, outputCount)).orElse(ItemStack.EMPTY);
    }

    public ItemStack bonusStack() {
        return resolve(bonus).map(ItemStack::new).orElse(ItemStack.EMPTY);
    }
}
