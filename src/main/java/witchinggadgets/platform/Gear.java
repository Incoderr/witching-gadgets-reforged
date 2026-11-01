package witchinggadgets.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import witchinggadgets.gear.GearSlot;

public final class Gear {
    private Gear() {
    }

    public static String curioId(GearSlot slot) {
        return slot.name().toLowerCase(Locale.ROOT);
    }

    private static IDynamicStackHandler handler(LivingEntity entity, GearSlot slot) {
        return CuriosApi.getCuriosInventory(entity)
            .flatMap(inventory -> inventory.getStacksHandler(curioId(slot)))
            .map(stacks -> stacks.getStacks())
            .filter(stacks -> stacks.getSlots() > 0)
            .orElse(null);
    }

    public static ItemStack get(Player player, GearSlot slot) {
        IDynamicStackHandler stacks = handler(player, slot);
        return stacks == null ? ItemStack.EMPTY : stacks.getStackInSlot(0);
    }

    public static void set(Player player, GearSlot slot, ItemStack stack) {
        IDynamicStackHandler stacks = handler(player, slot);
        if (stacks != null) {
            stacks.setStackInSlot(0, stack);
        }
    }

    public static void sync(Player player) {
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
