package witchinggadgets.gems;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectKeys;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import thaumcraft.registry.ModItems;

public class CuttingTableBlockEntity extends TcBlockEntity implements WorldlyContainer, MenuProvider {
    public static final int GEM = 0;
    public static final int OUTPUT = 4;
    private static final int[] SLOTS = {0, 1, 2, 3, 4};

    private final NonNullList<ItemStack> items = NonNullList.withSize(OUTPUT, ItemStack.EMPTY);
    private int targetCut;

    public CuttingTableBlockEntity(BlockPos pos, BlockState state) {
        super(GemsFeature.CUTTING_TABLE_ENTITY.get(), pos, state);
    }

    public int targetCut() {
        return targetCut;
    }

    public void setTargetCut(int targetCut) {
        this.targetCut = Math.floorMod(targetCut, GemCut.values().length);
        setChanged();
        sync();
    }

    public static boolean isEssence(ItemStack stack) {
        return stack.is(ModItems.ESSENTIA_PHIAL.get()) || stack.is(ModItems.WISP_ESSENCE.get());
    }

    @Nullable
    public static Aspect topAspect(ItemStack stack) {
        AspectList aspects = AspectKeys.ASPECTS.get(stack);
        if (aspects == null || aspects.isEmpty()) {
            return null;
        }
        List<Aspect> sorted = aspects.getAspectsSortedAmount();
        return sorted.isEmpty() ? null : sorted.get(0);
    }

    @Nullable
    public Aspect infusingAspect() {
        AspectList total = new AspectList();
        for (int slot = 1; slot <= 3; slot++) {
            AspectList aspects = AspectKeys.ASPECTS.get(items.get(slot));
            if (!items.get(slot).isEmpty() && aspects != null) {
                total.add(aspects);
            }
        }
        List<Aspect> sorted = total.getAspectsSortedAmount();
        return sorted.isEmpty() ? null : sorted.get(0);
    }

    public boolean canAcceptAspect(@Nullable Aspect aspect) {
        Aspect current = infusingAspect();
        return aspect != null && aspect.isPrimal() && (current == null || aspect == current);
    }

    public boolean canAcceptEssence(ItemStack stack) {
        return isEssence(stack) && canAcceptAspect(topAspect(stack));
    }

    public ItemStack output() {
        ItemStack gem = items.get(GEM);
        if (gem.isEmpty() || !GemAffinities.isGem(gem) || level == null) {
            return ItemStack.EMPTY;
        }
        Aspect aspect = infusingAspect();
        if (aspect == null) {
            return ItemStack.EMPTY;
        }
        GemCut cut = GemCut.byIndex(targetCut);
        ItemStack stack = InfusedGemItem.create(aspect, cut);
        int amplifier = 0;
        for (int slot = 1; slot <= 3; slot++) {
            if (!items.get(slot).isEmpty()) {
                amplifier++;
            }
        }
        int brittle = cut == GemCut.OVAL ? 1 : 0;
        if (amplifier > 0) {
            if (GemAffinities.affinities(gem).contains(aspect)) {
                if (amplifier - brittle > 0) {
                    InfusedGemItem.enchant(level, stack, GemsFeature.GEMSTONE_POTENCY, amplifier - brittle);
                }
            } else {
                if (GemAffinities.aversions(gem).contains(aspect)) {
                    amplifier++;
                }
                InfusedGemItem.enchant(level, stack, GemsFeature.GEMSTONE_BRITTLE, amplifier);
            }
        }
        return stack;
    }

    public void consumeInputs() {
        for (int slot = 0; slot < OUTPUT; slot++) {
            if (!items.get(slot).isEmpty()) {
                items.get(slot).shrink(1);
            }
        }
        setChanged();
        sync();
    }

    public List<ItemStack> items() {
        return items;
    }

    @Override
    public int getContainerSize() {
        return OUTPUT + 1;
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
        return slot == OUTPUT ? output() : items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot == OUTPUT) {
            return output();
        }
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
            sync();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return slot == OUTPUT ? ItemStack.EMPTY : ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == OUTPUT) {
            return;
        }
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
        sync();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == OUTPUT) {
            return false;
        }
        return slot == GEM ? !isEssence(stack) : canAcceptEssence(stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return slot != OUTPUT;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot != OUTPUT;
    }

    @Override
    public boolean stillValid(Player player) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, player);
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
        return Component.translatable("block.witchinggadgets.cutting_table");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CuttingTableMenu(containerId, inventory, this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input.tag(), items, input.lookup());
        targetCut = input.getIntOr("targetGemCut", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output.tag(), items, output.lookup());
        output.putInt("targetGemCut", targetCut);
    }
}
