package witchinggadgets.spinning;

import java.util.List;
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
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.blockentity.TcBlockEntity;

public class SpinningWheelBlockEntity extends TcBlockEntity implements Container, MenuProvider {
    public static final int INPUTS = 5;
    public static final int OUTPUT = 5;
    public static final int MAX_PROGRESS = 120;

    private final NonNullList<ItemStack> items = NonNullList.withSize(INPUTS + 1, ItemStack.EMPTY);
    private int progress;

    public SpinningWheelBlockEntity(BlockPos pos, BlockState state) {
        super(SpinningFeature.SPINNING_WHEEL_ENTITY.get(), pos, state);
    }

    public void serverTick() {
        SpinningRecipe recipe = recipe();
        if (recipe != null && canStack(items.get(OUTPUT), recipe.output().create())) {
            if (progress < MAX_PROGRESS) {
                progress++;
            } else {
                craft(recipe);
                progress = 0;
            }
            setChanged();
        } else {
            progress = 0;
        }
    }

    public int progress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public @Nullable SpinningRecipe recipe() {
        return SpinningRecipe.find(items.subList(0, INPUTS));
    }

    private void craft(SpinningRecipe recipe) {
        ItemStack output = recipe.output().create();
        ItemStack current = items.get(OUTPUT);
        if (current.isEmpty()) {
            items.set(OUTPUT, output);
        } else {
            current.grow(output.getCount());
        }
        for (int slot = 0; slot < INPUTS; slot++) {
            removeItem(slot, 1);
        }
    }

    private static boolean canStack(ItemStack current, ItemStack output) {
        if (output.isEmpty() || current.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(current, output)) {
            return false;
        }
        int total = current.getCount() + output.getCount();
        return total <= current.getMaxStackSize() && total <= output.getMaxStackSize();
    }

    public List<ItemStack> items() {
        return items;
    }

    @Override
    public int getContainerSize() {
        return items.size();
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
        return Component.translatable("block.witchinggadgets.spinning_wheel");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SpinningWheelMenu(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input.tag(), items, input.lookup());
        progress = input.getIntOr("progress", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output.tag(), items, output.lookup());
        output.putInt("progress", progress);
    }
}
