package witchinggadgets.platform.gear;

import java.util.Optional;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class GearMenu extends AbstractContainerMenu {
    public static final int RESULT_SLOT = 0;
    public static final int CRAFT_START = 1;
    public static final int CRAFT_END = 5;
    public static final int ARMOR_START = 5;
    public static final int ARMOR_END = 9;
    public static final int GEAR_START = 9;
    public static final int GEAR_END = 13;
    public static final int BAUBLE_START = 13;
    public static final int BAUBLE_END = 17;
    public static final int INVENTORY_START = 17;
    public static final int INVENTORY_END = 44;
    public static final int HOTBAR_START = 44;
    public static final int HOTBAR_END = 53;

    public interface Layout {
        int x(int slot);

        int y(int slot);
    }

    public static Layout layout = new Layout() {
        @Override
        public int x(int slot) {
            return 0;
        }

        @Override
        public int y(int slot) {
            return 0;
        }
    };

    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private final Player owner;
    private final long hidden;
    private final CraftingContainer craftSlots = new TransientCraftingContainer(this, 2, 2);
    private final ResultContainer resultSlots = new ResultContainer();

    public GearMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, 0L);
    }

    public GearMenu(int containerId, Inventory inventory, long hidden) {
        super(GearMenus.EXPANDED.get(), containerId);
        this.owner = inventory.player;
        this.hidden = hidden;
        addSlot(new ResultSlot(owner, craftSlots, resultSlots, 0, layout.x(RESULT_SLOT), layout.y(RESULT_SLOT)));
        for (int index = 0; index < 4; index++) {
            place(new Slot(craftSlots, index, layout.x(CRAFT_START + index), layout.y(CRAFT_START + index)), craftSlots, index);
        }
        for (int index = 0; index < ARMOR_SLOTS.length; index++) {
            int slot = ARMOR_START + index;
            place(new WornArmorSlot(inventory, owner, ARMOR_SLOTS[index], 39 - index, layout.x(slot), layout.y(slot)), inventory, 39 - index);
        }
        WornContainer gear = WornContainer.gear(owner);
        for (int index = 0; index < GEAR_END - GEAR_START; index++) {
            int slot = GEAR_START + index;
            place(new WornSlot(gear, index, layout.x(slot), layout.y(slot)), gear, index);
        }
        WornContainer baubles = WornContainer.baubles(owner);
        for (int index = 0; index < BAUBLE_END - BAUBLE_START; index++) {
            int slot = BAUBLE_START + index;
            place(new WornSlot(baubles, index, layout.x(slot), layout.y(slot)), baubles, index);
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int index = column + (row + 1) * 9;
                int slot = INVENTORY_START + column + row * 9;
                place(new Slot(inventory, index, layout.x(slot), layout.y(slot)), inventory, index);
            }
        }
        for (int column = 0; column < 9; column++) {
            int slot = HOTBAR_START + column;
            place(new Slot(inventory, column, layout.x(slot), layout.y(slot)), inventory, column);
        }
    }

    public static long hiddenBit(int slot) {
        return 1L << slot;
    }

    public boolean isHidden(int slot) {
        return (hidden & hiddenBit(slot)) != 0L;
    }

    private void place(Slot slot, Container container, int index) {
        addSlot(isHidden(slots.size()) ? new HiddenSlot(container, index) : slot);
    }

    public static class WornSlot extends Slot {
        public WornSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return container.canPlaceItem(getContainerSlot(), stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    public static class WornArmorSlot extends Slot {
        private final Player owner;
        private final EquipmentSlot equipmentSlot;

        public WornArmorSlot(Inventory inventory, Player owner, EquipmentSlot equipmentSlot, int index, int x, int y) {
            super(inventory, index, x, y);
            this.owner = owner;
            this.equipmentSlot = equipmentSlot;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.canEquip(equipmentSlot, owner);
        }

        @Override
        public boolean mayPickup(Player player) {
            ItemStack stack = getItem();
            return (stack.isEmpty() || player.isCreative() || !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) && super.mayPickup(player);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static final class HiddenSlot extends Slot {
        HiddenSlot(Container container, int index) {
            super(container, index, -10000, -10000);
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }

    @Override
    public void slotsChanged(Container container) {
        Level level = owner.level();
        if (container != craftSlots || level.isClientSide() || !(owner instanceof ServerPlayer serverPlayer)) {
            return;
        }
        CraftingInput input = craftSlots.asCraftInput();
        ItemStack result = ItemStack.EMPTY;
        Optional<RecipeHolder<CraftingRecipe>> recipe = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        if (recipe.isPresent() && resultSlots.setRecipeUsed(level, serverPlayer, recipe.get())) {
            ItemStack assembled = recipe.get().value().assemble(input, level.registryAccess());
            if (assembled.isItemEnabled(level.enabledFeatures())) {
                result = assembled;
            }
        }
        resultSlots.setItem(0, result);
        setRemoteSlot(0, result);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 0, result));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        resultSlots.clearContent();
        if (!player.level().isClientSide()) {
            clearContainer(player, craftSlots);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private int freeSlot(ItemStack stack, int start, int end) {
        for (int index = start; index < end; index++) {
            Slot target = slots.get(index);
            if (target.isActive() && !target.hasItem() && target.mayPlace(stack)) {
                return index;
            }
        }
        return -1;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack clicked = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            clicked = stack.copy();
            EquipmentSlot equipmentSlot = player.getEquipmentSlotForItem(clicked);
            int gearTarget = freeSlot(clicked, GEAR_START, GEAR_END);
            int baubleTarget = freeSlot(clicked, BAUBLE_START, BAUBLE_END);
            if (slotIndex == RESULT_SLOT) {
                if (!moveItemStackTo(stack, INVENTORY_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, clicked);
            } else if (slotIndex < BAUBLE_END) {
                if (!moveItemStackTo(stack, INVENTORY_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !slots.get(8 - equipmentSlot.getIndex()).hasItem() && slots.get(8 - equipmentSlot.getIndex()).isActive()) {
                int position = 8 - equipmentSlot.getIndex();
                if (!moveItemStackTo(stack, position, position + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (gearTarget >= 0) {
                if (!moveItemStackTo(stack, gearTarget, gearTarget + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (baubleTarget >= 0) {
                if (!moveItemStackTo(stack, baubleTarget, baubleTarget + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex < INVENTORY_END) {
                if (!moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY, clicked);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == clicked.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
            if (slotIndex == RESULT_SLOT) {
                player.drop(stack, false);
            }
        }
        return clicked;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack carried, Slot target) {
        return target.container != resultSlots && super.canTakeItemForPickAll(carried, target);
    }
}
