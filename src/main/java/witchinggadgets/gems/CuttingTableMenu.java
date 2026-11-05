package witchinggadgets.gems;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;

public class CuttingTableMenu extends AbstractContainerMenu {
    public static final int BUTTON_PREVIOUS = 0;
    public static final int BUTTON_NEXT = 1;
    private static final int MACHINE_SLOTS = CuttingTableBlockEntity.OUTPUT + 1;
    private static final int PLAYER_SLOTS_END = MACHINE_SLOTS + 36;

    private final Container container;
    private final @Nullable CuttingTableBlockEntity table;
    private final ContainerData data;

    public CuttingTableMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(MACHINE_SLOTS), null, new SimpleContainerData(1));
    }

    public CuttingTableMenu(int containerId, Inventory inventory, CuttingTableBlockEntity table) {
        this(containerId, inventory, table, table, new ContainerData() {
            @Override
            public int get(int index) {
                return table.targetCut();
            }

            @Override
            public void set(int index, int value) {
                table.setTargetCut(value);
            }

            @Override
            public int getCount() {
                return 1;
            }
        });
    }

    private CuttingTableMenu(int containerId, Inventory inventory, Container container, @Nullable CuttingTableBlockEntity table, ContainerData data) {
        super(GemsFeature.CUTTING_TABLE_MENU.get(), containerId);
        this.container = container;
        this.table = table;
        this.data = data;
        checkContainerSize(container, MACHINE_SLOTS);
        addSlot(new Slot(container, CuttingTableBlockEntity.GEM, 42, 24));
        for (int index = 0; index < 3; index++) {
            addSlot(new Slot(container, 1 + index, 60 + 20 * index, 56) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return table == null ? CuttingTableBlockEntity.isEssence(stack) : table.canAcceptEssence(stack);
                }
            });
        }
        addSlot(new Slot(container, CuttingTableBlockEntity.OUTPUT, 118, 24) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                if (table != null) {
                    table.consumeInputs();
                }
                super.onTake(player, stack);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        addDataSlots(data);
    }

    public int targetCut() {
        return data.get(0);
    }

    public @Nullable Aspect infusingAspect() {
        Aspect best = null;
        int amount = 0;
        java.util.Map<Aspect, Integer> totals = new java.util.LinkedHashMap<>();
        for (int slot = 1; slot <= 3; slot++) {
            ItemStack stack = slots.get(slot).getItem();
            thaumcraft.aspect.AspectList aspects = thaumcraft.aspect.AspectKeys.ASPECTS.get(stack);
            if (stack.isEmpty() || aspects == null) {
                continue;
            }
            for (Aspect aspect : aspects.getAspects()) {
                totals.merge(aspect, aspects.getAmount(aspect), Integer::sum);
            }
        }
        for (java.util.Map.Entry<Aspect, Integer> entry : totals.entrySet()) {
            if (entry.getValue() > amount) {
                amount = entry.getValue();
                best = entry.getKey();
            }
        }
        return best;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_PREVIOUS && id != BUTTON_NEXT) {
            return false;
        }
        data.set(0, Math.floorMod(data.get(0) + (id == BUTTON_NEXT ? 1 : -1), GemCut.values().length));
        broadcastChanges();
        return true;
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
        if (index == CuttingTableBlockEntity.OUTPUT) {
            if (!moveItemStackTo(stack, MACHINE_SLOTS, PLAYER_SLOTS_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, original);
            return original;
        }
        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, MACHINE_SLOTS, PLAYER_SLOTS_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (slots.get(1).mayPlace(stack)) {
            if (!moveItemStackTo(stack, 1, 4, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
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
