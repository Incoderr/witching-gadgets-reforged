package witchinggadgets.travellersgear;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import thaumcraft.blockentity.TcBlockEntity;

public class ArmorStandBlockEntity extends TcBlockEntity implements Container, MenuProvider {
    public static final int ARMOR = 0;
    public static final int BAUBLES = 4;
    public static final int GEAR = 8;
    public static final int SIZE = 11;

    public static final int RENDER_FLOOR = 0;
    public static final int RENDER_TABLE = 1;
    public static final int RENDER_ARMOR = 2;
    public static final int DISPLAY_BAUBLES = 6;
    public static final int RENDER_BAUBLES = 10;
    public static final int DISPLAY_GEAR = 14;
    public static final int RENDER_GEAR = 17;
    public static final int FLAG_COUNT = 20;
    public static final int ALL_FLAGS = (1 << FLAG_COUNT) - 1;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int flags = ALL_FLAGS;

    public ArmorStandBlockEntity(BlockPos pos, BlockState state) {
        super(TravellersGearFeature.ARMOR_STAND_ENTITY.get(), pos, state);
    }

    public int flags() {
        return flags;
    }

    public boolean flag(int bit) {
        return (flags & 1 << bit) != 0;
    }

    public void setFlags(int flags) {
        this.flags = flags & ALL_FLAGS;
        setChanged();
        sync();
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
            sync();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
        sync();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, pos, items);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.witchinggadgets.armor_stand");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ArmorStandMenu(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input, items);
        flags = input.getIntOr("flags", ALL_FLAGS) & ALL_FLAGS;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        output.putInt("flags", flags);
    }
}
