package witchinggadgets.gear;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.platform.Baubles;
import witchinggadgets.platform.Gear;

public final class AbilitySlots {
    private static final int ARMOR = 9;
    private static final int BAUBLES = 13;
    private static final int GEAR = 100;
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    private static final Map<Supplier<? extends Item>, GearAbility> EXTERNAL = new HashMap<>();

    private AbilitySlots() {
    }

    public static void register(Supplier<? extends Item> item, GearAbility ability) {
        EXTERNAL.put(item, ability);
    }

    private static @Nullable GearAbility ability(ItemStack stack) {
        if (stack.getItem() instanceof GearAbility ability) {
            return ability;
        }
        for (Map.Entry<Supplier<? extends Item>, GearAbility> entry : EXTERNAL.entrySet()) {
            if (stack.is(entry.getKey().get())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public record Entry(ItemStack stack, int slot) {
    }

    public static List<Entry> list(Player player) {
        List<Entry> list = new ArrayList<>();
        for (int armor = 0; armor < ARMOR_SLOTS.length; armor++) {
            add(list, player, player.getItemBySlot(ARMOR_SLOTS[armor]), ARMOR + armor, false);
        }
        for (int bauble = 0; bauble < Baubles.slotCount(); bauble++) {
            add(list, player, Baubles.get(player, bauble), BAUBLES + bauble, false);
        }
        for (GearSlot gear : GearSlot.values()) {
            add(list, player, Gear.get(player, gear), GEAR + gear.ordinal(), false);
        }
        int selected = player.getInventory().getSelectedSlot();
        ItemStack held = player.getInventory().getItem(selected);
        if (usable(player, held, true)) {
            list.add(list.size() / 2, new Entry(held, selected));
        }
        return list;
    }

    private static void add(List<Entry> list, Player player, ItemStack stack, int slot, boolean inHand) {
        if (usable(player, stack, inHand)) {
            list.add(new Entry(stack, slot));
        }
    }

    private static boolean usable(Player player, ItemStack stack, boolean inHand) {
        GearAbility ability = stack.isEmpty() ? null : ability(stack);
        return ability != null && ability.canActivate(player, stack, inHand);
    }

    public static void activate(Player player, int slot) {
        if (slot >= 0 && slot < Inventory.getSelectionSize()) {
            if (slot == player.getInventory().getSelectedSlot()) {
                activate(player, player.getInventory().getItem(slot), true);
            }
        } else if (slot >= ARMOR && slot < BAUBLES) {
            activate(player, player.getItemBySlot(ARMOR_SLOTS[slot - ARMOR]), false);
        } else if (slot >= BAUBLES && slot < BAUBLES + Baubles.slotCount()) {
            ItemStack stack = Baubles.get(player, slot - BAUBLES);
            if (activate(player, stack, false)) {
                Baubles.set(player, slot - BAUBLES, stack);
            }
        } else if (slot >= GEAR && slot < GEAR + GearSlot.values().length) {
            if (activate(player, Gear.get(player, GearSlot.values()[slot - GEAR]), false)) {
                Gear.sync(player);
            }
        }
    }

    private static boolean activate(Player player, ItemStack stack, boolean inHand) {
        if (!usable(player, stack, inHand)) {
            return false;
        }
        ability(stack).activate(player, stack);
        return true;
    }
}
