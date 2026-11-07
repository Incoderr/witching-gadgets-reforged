package witchinggadgets.compat.rei;

import java.util.List;
import java.util.function.Function;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import witchinggadgets.WitchingGadgets;

public final class WgRei {
    public static final CategoryIdentifier<SpinningDisplay> SPINNING = CategoryIdentifier.of(WitchingGadgets.id("spinning_wheel"));
    public static final CategoryIdentifier<BlastFurnaceDisplay> BLAST_FURNACE = CategoryIdentifier.of(WitchingGadgets.id("infernal_blast_furnace"));

    private WgRei() {
    }

    static EntryIngredient stack(ItemStack stack) {
        return stack.isEmpty() ? EntryIngredient.empty() : EntryIngredients.of(stack);
    }

    static EntryIngredient stacks(List<ItemStack> stacks) {
        return stacks.isEmpty() ? EntryIngredient.empty() : EntryIngredients.ofItemStacks(stacks);
    }

    static <D extends Display> DisplaySerializer<D> byKey(Function<String, D> decode, Function<D, String> encode) {
        return new DisplaySerializer<>() {
            @Override
            public CompoundTag save(CompoundTag tag, D display) {
                tag.putString("key", encode.apply(display));
                return tag;
            }

            @Override
            public D read(CompoundTag tag) {
                String key = tag.getString("key");
                D display = decode.apply(key);
                if (display == null) {
                    throw new IllegalArgumentException("Unknown Witching Gadgets recipe " + key);
                }
                return display;
            }
        };
    }
}
