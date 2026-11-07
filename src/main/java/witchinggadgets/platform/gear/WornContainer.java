package witchinggadgets.platform.gear;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.platform.Bauble;
import thaumcraft.platform.Baubles;
import witchinggadgets.gear.GearItem;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.platform.Gear;

public final class WornContainer implements Container {
    private final Player player;
    private final boolean gear;

    private WornContainer(Player player, boolean gear) {
        this.player = player;
        this.gear = gear;
    }

    public static WornContainer gear(Player player) {
        return new WornContainer(player, true);
    }

    public static WornContainer baubles(Player player) {
        return new WornContainer(player, false);
    }

    public Player player() {
        return player;
    }

    @Override
    public int getContainerSize() {
        return gear ? GearSlot.values().length : Baubles.slotCount();
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < getContainerSize(); slot++) {
            if (!getItem(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return gear ? Gear.get(player, GearSlot.values()[slot]) : Baubles.get(player, slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = getItem(slot);
        if (stack.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack taken = stack.copyWithCount(Math.min(amount, stack.getCount()));
        ItemStack rest = stack.copyWithCount(stack.getCount() - taken.getCount());
        setItem(slot, rest);
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = getItem(slot);
        setItem(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (gear) {
            Gear.set(player, GearSlot.values()[slot], stack);
        } else {
            Baubles.set(player, slot, stack);
        }
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player user) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (gear) {
            return stack.getItem() instanceof GearItem item && item.gearSlot(stack) == GearSlot.values()[slot] && item.canEquipGear(stack, player);
        }
        return stack.getItem() instanceof Bauble bauble && bauble.getBaubleType(stack) == Baubles.slotType(slot) && bauble.canEquip(stack, player);
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < getContainerSize(); slot++) {
            setItem(slot, ItemStack.EMPTY);
        }
    }
}
