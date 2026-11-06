package witchinggadgets.recipeview;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import thaumcraft.crafting.TcIngredient;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.furnace.BlastFurnaceRecipe;
import witchinggadgets.registry.ModCreativeTabs;
import witchinggadgets.spinning.SpinningRecipe;

public final class WgRecipeCatalog {
    public static final Identifier SPINNING_TEXTURE = WitchingGadgets.id("textures/gui/nei/spinningwheel.png");
    public static final Identifier BLAST_FURNACE_TEXTURE = WitchingGadgets.id("textures/gui/nei/blastfurnace.png");
    public static final int BACKGROUND_U = 5;
    public static final int BACKGROUND_V = 11;
    public static final int WIDTH = 166;
    public static final int SPINNING_HEIGHT = 100;
    public static final int BLAST_FURNACE_HEIGHT = 62;
    public static final int SPINNING_INPUT_X = 21;
    public static final int SPINNING_INPUT_Y = 9;
    public static final int SPINNING_OUTPUT_X = 126;
    public static final int SPINNING_OUTPUT_Y = 46;
    public static final int BLAST_INPUT_X = 24;
    public static final int BLAST_INPUT_Y = 24;
    public static final int BLAST_OUTPUT_X = 126;
    public static final int BLAST_OUTPUT_Y = 14;
    public static final int BLAST_BONUS_X = 126;
    public static final int BLAST_BONUS_Y = 39;

    private WgRecipeCatalog() {
    }

    public static List<SpinningRecipe> spinning() {
        return SpinningRecipe.all().stream().filter(WgRecipeCatalog::visible).toList();
    }

    public static @Nullable SpinningRecipe spinning(String key) {
        for (SpinningRecipe recipe : spinning()) {
            if (recipe.key().equals(key)) {
                return recipe;
            }
        }
        return null;
    }

    public static List<BlastFurnaceView> blastFurnace() {
        List<BlastFurnaceView> views = new ArrayList<>();
        List<BlastFurnaceRecipe> recipes = BlastFurnaceRecipe.all();
        for (int index = 0; index < recipes.size(); index++) {
            BlastFurnaceView view = view("blast_furnace:" + index, recipes.get(index));
            if (view != null) {
                views.add(view);
            }
        }
        return views;
    }

    public static @Nullable BlastFurnaceView blastFurnace(String key) {
        for (BlastFurnaceView view : blastFurnace()) {
            if (view.key().equals(key)) {
                return view;
            }
        }
        return null;
    }

    public static List<ItemStack> hiddenStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(ModCreativeTabs.TAB_ICON.get()));
        BuiltInRegistries.ITEM.getOptional(WitchingGadgets.id("brain_jerky")).ifPresent(item -> stacks.add(new ItemStack(item)));
        return stacks;
    }

    private static boolean visible(SpinningRecipe recipe) {
        if (!recipe.output().exists()) {
            return false;
        }
        for (TcIngredient input : recipe.inputs()) {
            if (input.displayStacks().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static @Nullable BlastFurnaceView view(String key, BlastFurnaceRecipe recipe) {
        ItemStack output = recipe.result();
        List<ItemStack> inputs = inputs(recipe.input(), recipe.inputCount());
        if (output.isEmpty() || inputs.isEmpty()) {
            return null;
        }
        return new BlastFurnaceView(key, inputs, output, recipe.bonusStack(), recipe.time(), recipe.special());
    }

    private static List<ItemStack> inputs(String id, int count) {
        List<ItemStack> stacks = new ArrayList<>();
        if (id.startsWith("#")) {
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM, Identifier.parse(id.substring(1))))) {
                stacks.add(new ItemStack(holder, count));
            }
        } else {
            BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).ifPresent(item -> stacks.add(new ItemStack(item, count)));
        }
        return stacks;
    }
}
