package witchinggadgets.spinning;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SpinningWheelMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = SpinningWheelBlockEntity.INPUTS + 1;
    private static final int PLAYER_SLOTS_END = MACHINE_SLOTS + 36;

    private final Container container;
    private final ContainerData data;

    public SpinningWheelMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(MACHINE_SLOTS), new SimpleContainerData(1));
    }

    public SpinningWheelMenu(int containerId, Inventory inventory, SpinningWheelBlockEntity wheel) {
        this(containerId, inventory, wheel, new ContainerData() {
            @Override
            public int get(int index) {
                return wheel.progress();
            }

            @Override
            public void set(int index, int value) {
                wheel.setProgress(value);
            }

            @Override
            public int getCount() {
                return 1;
            }
        });
    }

    private SpinningWheelMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(SpinningFeature.SPINNING_WHEEL_MENU.get(), containerId);
        this.container = container;
        this.data = data;
        checkContainerSize(container, MACHINE_SLOTS);
        for (int slot = 0; slot < SpinningWheelBlockEntity.INPUTS; slot++) {
            addSlot(new Slot(container, slot, 16, 16 + 24 * slot));
        }
        addSlot(new Slot(container, SpinningWheelBlockEntity.OUTPUT, 142, 64) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 16 + column * 18, 151 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 16 + column * 18, 209));
        }
        addDataSlots(data);
    }

    public int progressScaled(int max) {
        return (int) Math.ceil((double) data.get(0) / SpinningWheelBlockEntity.MAX_PROGRESS * max);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, MACHINE_SLOTS, PLAYER_SLOTS_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, MACHINE_SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }
}
