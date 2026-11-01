package witchinggadgets.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.platform.gear.GearAttachments;

public final class Gear {
    private Gear() {
    }

    public static ItemStack get(Player player, GearSlot slot) {
        return GearAttachments.inventory(player).getItem(slot.ordinal());
    }

    public static void set(Player player, GearSlot slot, ItemStack stack) {
        GearAttachments.inventory(player).setItem(slot.ordinal(), stack);
    }

    public static List<ItemStack> stacks(Player player) {
        List<ItemStack> stacks = new ArrayList<>();
        for (GearSlot slot : GearSlot.values()) {
            stacks.add(get(player, slot));
        }
        return stacks;
    }

    public static ItemStack find(LivingEntity entity, Predicate<ItemStack> predicate) {
        if (entity instanceof Player player) {
            for (ItemStack stack : stacks(player)) {
                if (!stack.isEmpty() && predicate.test(stack)) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
