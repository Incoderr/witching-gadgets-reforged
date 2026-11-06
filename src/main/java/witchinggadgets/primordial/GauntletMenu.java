package witchinggadgets.primordial;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thaumcraft.platform.Menus;
import witchinggadgets.gems.InfusedGemItem;

public class GauntletMenu extends AbstractContainerMenu {
    private static final int[][] POSITIONS = {{60, 6}, {100, 6}, {57, 42}, {80, 53}, {103, 42}};

    private final ItemStack gauntlet;
    private final SimpleContainer storage;

    public GauntletMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(PrimordialGauntletItem.SLOTS), ItemStack.EMPTY, -1);
    }

    private GauntletMenu(int containerId, Inventory inventory, SimpleContainer storage, ItemStack gauntlet, int lockedSlot) {
        super(PrimordialFeature.GAUNTLET_MENU.get(), containerId);
        this.gauntlet = gauntlet;
        this.storage = storage;
        for (int slot = 0; slot < PrimordialGauntletItem.SLOTS; slot++) {
            addSlot(new Slot(storage, slot, POSITIONS[slot][0], POSITIONS[slot][1]) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof InfusedGemItem;
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            int index = column;
            addSlot(new Slot(inventory, column, 8 + column * 18, 142) {
                @Override
                public boolean mayPickup(Player player) {
                    return index != lockedSlot;
                }
            });
        }
    }

    public static void open(ServerPlayer player, InteractionHand hand) {
        ItemStack gauntlet = player.getItemInHand(hand);
        int lockedSlot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : -1;
        Menus.open(player, new SimpleMenuProvider((containerId, inventory, user) -> create(containerId, inventory, gauntlet, lockedSlot), gauntlet.getHoverName()));
    }

    private static GauntletMenu create(int containerId, Inventory inventory, ItemStack gauntlet, int lockedSlot) {
        SimpleContainer storage = new SimpleContainer(PrimordialGauntletItem.SLOTS) {
            @Override
            public void setChanged() {
                super.setChanged();
                PrimordialGauntletItem.setGems(gauntlet, getItems());
            }
        };
        NonNullList<ItemStack> gems = PrimordialGauntletItem.gems(gauntlet);
        for (int slot = 0; slot < gems.size(); slot++) {
            storage.setItem(slot, gems.get(slot));
        }
        return new GauntletMenu(containerId, inventory, storage, gauntlet, lockedSlot);
    }

    public ItemStack gauntlet() {
        return gauntlet;
    }

    public NonNullList<ItemStack> storage() {
        return storage.getItems();
    }

    public void storageChanged() {
        storage.setChanged();
        broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return gauntlet.isEmpty() || player.getMainHandItem() == gauntlet || player.getOffhandItem() == gauntlet;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < PrimordialGauntletItem.SLOTS) {
            if (!moveItemStackTo(stack, PrimordialGauntletItem.SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!(stack.getItem() instanceof InfusedGemItem) || !moveItemStackTo(stack, 0, PrimordialGauntletItem.SLOTS, false)) {
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
