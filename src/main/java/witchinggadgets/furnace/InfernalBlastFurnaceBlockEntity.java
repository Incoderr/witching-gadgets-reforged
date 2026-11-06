package witchinggadgets.furnace;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.alchemy.BellowsBlock;
import thaumcraft.alchemy.BellowsBlockEntity;
import thaumcraft.aspect.Aspect;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import thaumcraft.essentia.EssentiaTransport;
import thaumcraft.vis.VisNet;

public class InfernalBlastFurnaceBlockEntity extends TcBlockEntity implements EssentiaTransport {
    private Direction facing = Direction.NORTH;
    private int speedupTick;
    private int processTick;
    private int recipeTime;
    private boolean specialFuel;
    private boolean active;
    private final List<ItemStack> inputs = new ArrayList<>();

    public InfernalBlastFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(FurnaceFeature.BLAST_FURNACE_ENTITY.get(), pos, state);
    }

    public int position() {
        return getBlockState().getValue(InfernalBlastFurnaceBlock.POSITION);
    }

    public BlockPos masterPos() {
        return InfernalBlastFurnaceBlock.master(worldPosition, position());
    }

    @Nullable
    public InfernalBlastFurnaceBlockEntity master() {
        return level != null && level.getBlockEntity(masterPos()) instanceof InfernalBlastFurnaceBlockEntity master ? master : null;
    }

    public Direction facing() {
        return facing;
    }

    public void setFacing(Direction facing) {
        this.facing = facing;
        setChanged();
    }

    public int speedupTick() {
        return speedupTick;
    }

    public List<ItemStack> inputs() {
        return inputs;
    }

    @Nullable
    private static Direction port(int position) {
        return switch (position) {
            case 1 -> Direction.NORTH;
            case 3 -> Direction.WEST;
            case 5 -> Direction.EAST;
            case 7 -> Direction.SOUTH;
            default -> null;
        };
    }

    private static boolean vent(int position) {
        return position == 10 || position == 12 || position == 14 || position == 16;
    }

    public void serverTick() {
        if (level == null) {
            return;
        }
        int position = position();
        Direction port = port(position);
        if (port != null && level.getGameTime() % 5 == 0) {
            InfernalBlastFurnaceBlockEntity master = master();
            if (master != null && drawEssentia(port)) {
                master.speedupTick += 600;
                master.setChanged();
            }
        }
        if (vent(position)) {
            InfernalBlastFurnaceBlockEntity master = master();
            if (master != null && master.speedupTick <= 0) {
                master.speedupTick = VisNet.drainVis(level, worldPosition, Aspect.FIRE, 5);
            }
        }
        if (position == InfernalBlastFurnaceBlock.MASTER) {
            masterTick();
        }
    }

    private boolean drawEssentia(Direction port) {
        if (!(level.getBlockEntity(worldPosition.relative(port)) instanceof EssentiaTransport transport)) {
            return false;
        }
        Direction side = port.getOpposite();
        if (!transport.canOutputTo(side)) {
            return false;
        }
        return transport.getSuctionAmount(side) < getSuctionAmount(port) && transport.takeEssentia(Aspect.FIRE, 1, side) == 1;
    }

    private void masterTick() {
        boolean cooking = false;
        if (processTick > 0) {
            processTick--;
            cooking = true;
            if (speedupTick > 0) {
                speedupTick--;
            }
            processTick = Math.min(processTick, calculateTime());
        }
        if (processTick <= 0 && !inputs.isEmpty()) {
            ItemStack input = inputs.getFirst();
            BlastFurnaceRecipe recipe = BlastFurnaceRecipe.forInput(input);
            if (cooking) {
                if (recipe != null) {
                    outputItem(recipe.result());
                    ItemStack bonus = recipe.bonusStack();
                    if (!bonus.isEmpty()) {
                        int count = 0;
                        int bellows = getBellows();
                        if (bellows == 0) {
                            if (level.getRandom().nextInt(4) == 0) {
                                count++;
                            }
                        } else {
                            for (int b = 0; b < bellows; b++) {
                                if (level.getRandom().nextFloat() < 0.44F) {
                                    count++;
                                }
                            }
                        }
                        if (count > 0) {
                            outputItem(bonus.copyWithCount(count));
                        }
                    }
                    input.shrink(recipe.inputCount());
                }
                if (recipe == null || input.isEmpty()) {
                    inputs.removeFirst();
                }
                cooking = false;
                specialFuel = false;
            } else if (recipe != null) {
                recipeTime = recipe.time();
                processTick = calculateTime();
                specialFuel = recipe.special();
                cooking = true;
            } else {
                inputs.removeFirst();
            }
            setChanged();
        }
        if (cooking != active) {
            active = cooking;
            setChanged();
            BlastFurnaceStructure.setHeat(level, worldPosition, active ? specialFuel ? 2 : 1 : 0);
        }
    }

    public int calculateTime() {
        return recipeTime / (speedupTick > 0 ? 2 : 1) - getBellows() * 40;
    }

    public int getBellows() {
        int bellows = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos pos = worldPosition.relative(direction, 2);
            BlockState state = level.getBlockState(pos);
            if (level.getBlockEntity(pos) instanceof BellowsBlockEntity && state.getValue(BellowsBlock.FACING) == direction.getOpposite() && !level.hasNeighborSignal(pos)) {
                bellows++;
            }
        }
        return bellows;
    }

    private void outputItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        Container container = HopperBlockEntity.getContainerAt(level, worldPosition.relative(facing, 2).above());
        if (container != null) {
            stack = HopperBlockEntity.addItem(null, container, stack, facing.getOpposite());
        }
        if (!stack.isEmpty()) {
            ItemEntity item = new ItemEntity(level, worldPosition.getX() + 0.5 + facing.getStepX() * 1.66, worldPosition.getY() + 1.4, worldPosition.getZ() + 0.5 + facing.getStepZ() * 1.66, stack.copy());
            item.setDeltaMovement(0.075 * facing.getStepX(), 0.025, 0.075 * facing.getStepZ());
            level.addFreshEntity(item);
        }
    }

    public void addInput(ItemStack stack) {
        for (ItemStack existing : inputs) {
            if (ItemStack.isSameItemSameComponents(existing, stack) && existing.getCount() + stack.getCount() <= stack.getMaxStackSize()) {
                existing.grow(stack.getCount());
                setChanged();
                return;
            }
        }
        inputs.add(stack);
        setChanged();
    }

    @Override
    public boolean isConnectable(Direction face) {
        return port(position()) == face;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        int position = position();
        return position == 1 || position == 3 || position == 4 || position == 5 || position == 7;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    @Nullable
    public Aspect getSuctionType(Direction face) {
        return Aspect.FIRE;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        InfernalBlastFurnaceBlockEntity master = master();
        return master != null && master.speedupTick < 40 ? 128 : 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    @Nullable
    public Aspect getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        facing = Direction.from3DDataValue(input.getIntOr("facing", Direction.NORTH.get3DDataValue()));
        speedupTick = input.getIntOr("speedupTick", 0);
        processTick = input.getIntOr("processTick", 0);
        recipeTime = input.getIntOr("recipeTime", 0);
        active = input.getBooleanOr("active", false);
        specialFuel = input.getBooleanOr("specialFuel", false);
        inputs.clear();
        input.read("inputs", ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(list -> list.stream().filter(stack -> !stack.isEmpty()).forEach(inputs::add));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("facing", facing.get3DDataValue());
        output.putInt("speedupTick", speedupTick);
        output.putInt("processTick", processTick);
        output.putInt("recipeTime", recipeTime);
        output.putBoolean("active", active);
        output.putBoolean("specialFuel", specialFuel);
        output.store("inputs", ItemStack.OPTIONAL_CODEC.listOf(), List.copyOf(inputs));
    }
}
