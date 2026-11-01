package witchinggadgets.gear;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface GearItem {
    GearSlot gearSlot(ItemStack stack);

    default void onGearTick(ItemStack stack, Player player) {
    }

    default void onGearEquipped(ItemStack stack, Player player) {
    }

    default void onGearUnequipped(ItemStack stack, Player player) {
    }

    default boolean canEquipGear(ItemStack stack, Player player) {
        return true;
    }
}
