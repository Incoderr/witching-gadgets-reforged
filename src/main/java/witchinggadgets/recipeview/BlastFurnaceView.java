package witchinggadgets.recipeview;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public record BlastFurnaceView(String key, List<ItemStack> inputs, ItemStack output, ItemStack bonus, int time, boolean special) {
}
