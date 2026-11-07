package witchinggadgets.travellersgear;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import thaumcraft.platform.Bauble;
import thaumcraft.platform.BaubleType;
import thaumcraft.platform.Baubles;
import thaumcraft.platform.baubles.BaubleAttachments;
import thaumcraft.platform.baubles.BaubleInventory;
import thaumcraft.platform.baubles.BaubleSlot;
import witchinggadgets.gear.GearItem;
import witchinggadgets.gear.GearSlot;
import witchinggadgets.platform.gear.GearAttachments;
import witchinggadgets.platform.gear.GearInventory;
import witchinggadgets.platform.gear.GearSlotView;

public class ArmorStandMenu extends AbstractContainerMenu {
    public static final int EQUIPMENT = ArmorStandBlockEntity.SIZE;
    public static final int STAND_START = EQUIPMENT;
    public static final int STAND_END = STAND_START + ArmorStandBlockEntity.SIZE;
    public static final int INVENTORY_END = STAND_END + 36;
    public static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    public static final GearSlot[] GEAR_SLOTS = {GearSlot.CLOAK, GearSlot.PAULDRON, GearSlot.VAMBRACE};
    public static final int[][] PLAYER_POSITIONS = {
        {4, 22}, {4, 40}, {4, 58}, {4, 76},
        {22, 4}, {22, 94}, {40, 94}, {76, 40},
        {40, 4}, {76, 22}, {76, 58},
    };
    public static final int[][] STAND_POSITIONS = {
        {120, 22}, {120, 40}, {120, 58}, {120, 76},
        {138, 22}, {138, 40}, {138, 58}, {138, 76},
        {156, 22}, {156, 40}, {156, 58},
    };

    private final Container stand;
    private final @Nullable ArmorStandBlockEntity blockEntity;
    private final Player player;

    public ArmorStandMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(pos) instanceof ArmorStandBlockEntity entity ? entity : null);
    }

    public ArmorStandMenu(int containerId, Inventory inventory, @Nullable ArmorStandBlockEntity blockEntity) {
        super(TravellersGearFeature.ARMOR_STAND_MENU.get(), containerId);
        this.player = inventory.player;
        this.blockEntity = blockEntity;
        this.stand = blockEntity != null ? blockEntity : new SimpleContainer(ArmorStandBlockEntity.SIZE);
        for (int index = 0; index < ARMOR_SLOTS.length; index++) {
            addSlot(new ArmorSlot(inventory, player, ARMOR_SLOTS[index], 39 - index, PLAYER_POSITIONS[index][0], PLAYER_POSITIONS[index][1], null));
        }
        BaubleInventory baubles = BaubleAttachments.inventory(player);
        for (int index = 0; index < 4; index++) {
            int slot = ArmorStandBlockEntity.BAUBLES + index;
            addSlot(new BaubleSlot(baubles, Baubles.slotType(index), index, PLAYER_POSITIONS[slot][0], PLAYER_POSITIONS[slot][1]));
        }
        GearInventory gear = GearAttachments.inventory(player);
        for (int index = 0; index < GEAR_SLOTS.length; index++) {
            int slot = ArmorStandBlockEntity.GEAR + index;
            addSlot(new GearSlotView(gear, GEAR_SLOTS[index], GEAR_SLOTS[index].ordinal(), PLAYER_POSITIONS[slot][0], PLAYER_POSITIONS[slot][1]));
        }
        for (int index = 0; index < ArmorStandBlockEntity.SIZE; index++) {
            addSlot(new StandSlot(stand, index, STAND_POSITIONS[index][0], STAND_POSITIONS[index][1], player));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 120 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 178));
        }
    }

    public @Nullable ArmorStandBlockEntity blockEntity() {
        return blockEntity;
    }

    static boolean valid(int index, ItemStack stack, Player player) {
        if (stack.isEmpty()) {
            return false;
        }
        if (index < ArmorStandBlockEntity.BAUBLES) {
            return player.getEquipmentSlotForItem(stack) == ARMOR_SLOTS[index];
        }
        if (index < ArmorStandBlockEntity.GEAR) {
            BaubleType type = Baubles.slotType(index - ArmorStandBlockEntity.BAUBLES);
            return stack.getItem() instanceof Bauble bauble && bauble.getBaubleType(stack) == type;
        }
        return stack.getItem() instanceof GearItem item && item.gearSlot(stack) == GEAR_SLOTS[index - ArmorStandBlockEntity.GEAR];
    }

    private static final class StandSlot extends Slot {
        private final Player player;

        StandSlot(Container container, int index, int x, int y, Player player) {
            super(container, index, x, y);
            this.player = player;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return valid(getContainerSlot(), stack, player);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stand.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem() && index >= STAND_END) {
            return ItemStack.EMPTY;
        }
        if (index < STAND_END) {
            int pair = index % EQUIPMENT;
            Slot worn = slots.get(pair);
            Slot shown = slots.get(STAND_START + pair);
            ItemStack wornStack = worn.getItem();
            ItemStack shownStack = shown.getItem();
            if (!shownStack.isEmpty() && !worn.mayPlace(shownStack) || !wornStack.isEmpty() && !shown.mayPlace(wornStack) || !wornStack.isEmpty() && !worn.mayPickup(player) || !shownStack.isEmpty() && !shown.mayPickup(player)) {
                return ItemStack.EMPTY;
            }
            worn.set(shownStack);
            shown.set(wornStack);
            worn.setChanged();
            shown.setChanged();
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        for (int target = 0; target < EQUIPMENT; target++) {
            if (valid(target, stack, player) && !slots.get(target).hasItem() && slots.get(target).mayPlace(stack)) {
                return move(slot, stack, target);
            }
        }
        for (int target = 0; target < EQUIPMENT; target++) {
            if (valid(target, stack, player) && !slots.get(STAND_START + target).hasItem()) {
                return move(slot, stack, STAND_START + target);
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack move(Slot from, ItemStack stack, int target) {
        ItemStack original = stack.copy();
        if (!moveItemStackTo(stack, target, target + 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            from.setByPlayer(ItemStack.EMPTY, original);
        } else {
            from.setChanged();
        }
        return ItemStack.EMPTY;
    }
}
