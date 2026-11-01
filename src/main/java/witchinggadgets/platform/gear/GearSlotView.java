package witchinggadgets.platform.gear;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import witchinggadgets.gear.GearItem;
import witchinggadgets.gear.GearSlot;

public class GearSlotView extends Slot {
    private final GearInventory gear;
    private final GearSlot type;

    public GearSlotView(GearInventory gear, GearSlot type, int index, int x, int y) {
        super(gear, index, x, y);
        this.gear = gear;
        this.type = type;
    }

    public GearSlot type() {
        return type;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return GearInventory.isValidFor(type, stack, gear.player());
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack stack = getItem();
        return !stack.isEmpty() && stack.getItem() instanceof GearItem;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
