package witchinggadgets.compat.rei;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.function.Function;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
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

    static <D extends Display> DisplaySerializer<D> byKey(Function<String, @Nullable D> decode, Function<D, String> encode) {
        MapCodec<D> codec = Codec.STRING.fieldOf("key").flatXmap(key -> {
            D display = decode.apply(key);
            return display == null ? DataResult.error(() -> "Unknown Witching Gadgets recipe " + key) : DataResult.success(display);
        }, display -> DataResult.success(encode.apply(display)));
        StreamCodec<RegistryFriendlyByteBuf, D> streamCodec = ByteBufCodecs.STRING_UTF8.<RegistryFriendlyByteBuf>cast().map(key -> {
            D display = decode.apply(key);
            if (display == null) {
                throw new DecoderException("Unknown Witching Gadgets recipe " + key);
            }
            return display;
        }, encode);
        return DisplaySerializer.of(codec, streamCodec);
    }
}
