package witchinggadgets.registry;

import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;

public interface CreativeVariants {
    void creativeStacks(Consumer<ItemStack> output);
}
