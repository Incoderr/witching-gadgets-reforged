package witchinggadgets.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.registry.ModItems;

public class LabelLibraryBlockEntity extends TcBlockEntity implements WorldlyContainer, MenuProvider {
    public static final int INPUT = 0;
    public static final int OUTPUT = 1;
    private static final int[] SLOTS = {INPUT};

    private ItemStack labels = ItemStack.EMPTY;
    private @Nullable Aspect aspect;

    public LabelLibraryBlockEntity(BlockPos pos, BlockState state) {
        super(DevicesFeature.LABEL_LIBRARY_ENTITY.get(), pos, state);
    }

    public static boolean isLabel(ItemStack stack) {
        return stack.is(ModItems.JAR_LABEL.get());
    }

    public @Nullable Aspect aspect() {
        return aspect;
    }

    public void setAspect(@Nullable Aspect aspect) {
        this.aspect = aspect;
        sync();
    }

    public ItemStack output() {
        if (aspect == null || labels.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack label = new ItemStack(ModItems.JAR_LABEL.get());
        AspectKeys.ASPECTS.set(label, new AspectList().add(aspect, 0));
        return label;
    }

    public void consumeLabel() {
        removeItem(INPUT, 1);
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return labels.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == INPUT ? labels : output();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != INPUT) {
            return output();
        }
        ItemStack result = labels.split(amount);
        if (!result.isEmpty()) {
            sync();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != INPUT) {
            return ItemStack.EMPTY;
        }
        ItemStack result = labels;
        labels = ItemStack.EMPTY;
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == INPUT) {
            labels = stack;
            stack.limitSize(getMaxStackSize(stack));
            sync();
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == INPUT && isLabel(stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        labels = ItemStack.EMPTY;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide() && !labels.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), labels);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.witchinggadgets.label_library");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new LabelLibraryMenu(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        labels = input.read("labels", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        aspect = input.getString("aspect").map(Aspect::getAspect).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("labels", ItemStack.OPTIONAL_CODEC, labels);
        if (aspect != null) {
            output.putString("aspect", aspect.getTag());
        }
    }
}
