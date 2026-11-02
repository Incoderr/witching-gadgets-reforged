package witchinggadgets.gear;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface GearAbility {
    boolean canActivate(Player player, ItemStack stack);

    void activate(Player player, ItemStack stack);
}
