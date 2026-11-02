package witchinggadgets.registry;

import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;

// Items that show several stacks in the creative tab instead of the default one.
public interface CreativeVariants {
    void creativeStacks(Consumer<ItemStack> output);
}
