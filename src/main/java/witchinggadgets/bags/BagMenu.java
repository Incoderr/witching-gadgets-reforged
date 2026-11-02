package witchinggadgets.bags;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import thaumcraft.platform.Menus;

public class BagMenu extends AbstractContainerMenu {
    private static final int SLOTS = BagItem.SIZE;

    private final ItemStack bag;

    public BagMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(SLOTS), ItemStack.EMPTY, -1);
    }

    private BagMenu(int containerId, Inventory inventory, SimpleContainer storage, ItemStack bag, int lockedSlot) {
        super(BagsFeature.BAG_MENU.get(), containerId);
        this.bag = bag;
        for (int slot = 0; slot < SLOTS; slot++) {
            addSlot(new Slot(storage, slot, 35 + slot % 6 * 18, 9 + slot / 6 * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return !(stack.getItem() instanceof BagItem);
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 82 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            int index = column;
            addSlot(new Slot(inventory, column, 8 + column * 18, 140) {
                @Override
                public boolean mayPickup(Player player) {
                    return index != lockedSlot;
                }
            });
        }
    }

    public static void open(ServerPlayer player, InteractionHand hand) {
        ItemStack bag = player.getItemInHand(hand);
        int lockedSlot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : -1;
        Menus.open(player, new SimpleMenuProvider((containerId, inventory, user) -> create(containerId, inventory, bag, lockedSlot), bag.getHoverName()));
    }

    private static BagMenu create(int containerId, Inventory inventory, ItemStack bag, int lockedSlot) {
        SimpleContainer storage = new SimpleContainer(SLOTS) {
            @Override
            public void setChanged() {
                super.setChanged();
                BagsFeature.CONTENTS.set(bag, ItemContainerContents.fromItems(getItems()));
            }
        };
        BagsFeature.CONTENTS.getOrDefault(bag, ItemContainerContents.EMPTY).copyInto(storage.getItems());
        return new BagMenu(containerId, inventory, storage, bag, lockedSlot);
    }

    @Override
    public boolean stillValid(Player player) {
        return bag.isEmpty() || player.getMainHandItem() == bag || player.getOffhandItem() == bag;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) {
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
