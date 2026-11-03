package witchinggadgets.bags;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import thaumcraft.platform.Menus;

public class VoidBagMenu extends AbstractContainerMenu {
    public static final int FILTERS = BagItem.SIZE;

    private final SimpleContainer filters;
    private final ItemStack bag;

    public VoidBagMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(FILTERS), ItemStack.EMPTY, -1);
    }

    private VoidBagMenu(int containerId, Inventory inventory, SimpleContainer filters, ItemStack bag, int lockedSlot) {
        super(BagsFeature.VOID_BAG_MENU.get(), containerId);
        this.filters = filters;
        this.bag = bag;
        for (int slot = 0; slot < FILTERS; slot++) {
            addSlot(new Slot(filters, slot, 29 + slot % 6 * 20, 7 + slot / 6 * 20) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return false;
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

    private static VoidBagMenu create(int containerId, Inventory inventory, ItemStack bag, int lockedSlot) {
        SimpleContainer filters = new SimpleContainer(FILTERS) {
            @Override
            public void setChanged() {
                super.setChanged();
                BagsFeature.CONTENTS.set(bag, ItemContainerContents.fromItems(getItems()));
            }
        };
        BagsFeature.CONTENTS.getOrDefault(bag, ItemContainerContents.EMPTY).copyInto(filters.getItems());
        return new VoidBagMenu(containerId, inventory, filters, bag, lockedSlot);
    }

    @Override
    public void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player) {
        if (slotIndex >= 0 && slotIndex < FILTERS) {
            ItemStack carried = getCarried();
            filters.setItem(slotIndex, carried.isEmpty() || carried.getItem() instanceof BagItem ? ItemStack.EMPTY : carried.copyWithCount(1));
            return;
        }
        super.clicked(slotIndex, buttonNum, containerInput, player);
    }

    @Override
    public boolean stillValid(Player player) {
        return bag.isEmpty() || player.getMainHandItem() == bag || player.getOffhandItem() == bag;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
