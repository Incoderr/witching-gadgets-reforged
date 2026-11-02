package witchinggadgets.cloaks;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import thaumcraft.platform.Menus;

public class CloakStorageMenu extends AbstractContainerMenu {
    private static final int SLOTS = MagicCloakItem.STORAGE_SIZE;

    private final SimpleContainer storage;
    private final ItemStack cloak;

    public CloakStorageMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(SLOTS), ItemStack.EMPTY);
    }

    private CloakStorageMenu(int containerId, Inventory inventory, SimpleContainer storage, ItemStack cloak) {
        super(CloaksFeature.STORAGE_MENU.get(), containerId);
        this.storage = storage;
        this.cloak = cloak;
        for (int slot = 0; slot < SLOTS; slot++) {
            addSlot(new Slot(storage, slot, 8 + slot % 9 * 18, 9 + slot / 9 * 18));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 82 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 140));
        }
    }

    public static void open(ServerPlayer player, ItemStack cloak) {
        Menus.open(player, new SimpleMenuProvider((containerId, inventory, user) -> create(containerId, inventory, cloak), cloak.getHoverName()));
    }

    private static CloakStorageMenu create(int containerId, Inventory inventory, ItemStack cloak) {
        SimpleContainer storage = new SimpleContainer(SLOTS) {
            @Override
            public void setChanged() {
                super.setChanged();
                CloaksFeature.STORAGE.set(cloak, ItemContainerContents.fromItems(getItems()));
            }
        };
        CloaksFeature.STORAGE.getOrDefault(cloak, ItemContainerContents.EMPTY).copyInto(storage.getItems());
        return new CloakStorageMenu(containerId, inventory, storage, cloak);
    }

    @Override
    public boolean stillValid(Player player) {
        return cloak.isEmpty() || CloakEvents.activeCloak(player) == cloak;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < SLOTS) {
            if (!moveItemStackTo(stack, SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }
}
