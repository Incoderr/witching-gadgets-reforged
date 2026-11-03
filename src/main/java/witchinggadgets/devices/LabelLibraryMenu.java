package witchinggadgets.devices;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.research.Knowledge;

public class LabelLibraryMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;
    private final @Nullable LabelLibraryBlockEntity library;

    public LabelLibraryMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(2), new SimpleContainerData(1), null);
    }

    public LabelLibraryMenu(int containerId, Inventory inventory, LabelLibraryBlockEntity library) {
        this(containerId, inventory, library, new ContainerData() {
            @Override
            public int get(int index) {
                Aspect aspect = library.aspect();
                return aspect == null ? 0 : indexOf(aspect) + 1;
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 1;
            }
        }, library);
    }

    private LabelLibraryMenu(int containerId, Inventory inventory, Container container, ContainerData data, @Nullable LabelLibraryBlockEntity library) {
        super(DevicesFeature.LABEL_LIBRARY_MENU.get(), containerId);
        this.container = container;
        this.data = data;
        this.library = library;
        addSlot(new Slot(container, LabelLibraryBlockEntity.INPUT, 8, 8) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return LabelLibraryBlockEntity.isLabel(stack);
            }
        });
        addSlot(new Slot(container, LabelLibraryBlockEntity.OUTPUT, 8, 51) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                if (LabelLibraryMenu.this.library != null) {
                    LabelLibraryMenu.this.library.consumeLabel();
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

    public static int indexOf(Aspect aspect) {
        Aspect[] values = Aspect.values();
        for (int index = 0; index < values.length; index++) {
            if (values[index] == aspect) {
                return index;
            }
        }
        return -1;
    }

    public @Nullable Aspect selected() {
        int index = data.get(0) - 1;
        Aspect[] values = Aspect.values();
        return index >= 0 && index < values.length ? values[index] : null;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        Aspect[] values = Aspect.values();
        if (library == null || id < 0 || id >= values.length || !Knowledge.get(player).hasDiscoveredAspect(values[id])) {
            return false;
        }
        library.setAspect(values[id]);
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
