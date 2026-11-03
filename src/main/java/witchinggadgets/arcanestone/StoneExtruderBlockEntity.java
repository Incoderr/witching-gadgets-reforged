package witchinggadgets.arcanestone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import thaumcraft.platform.Transfer;

public class StoneExtruderBlockEntity extends TcBlockEntity {
    public static final int CYCLE = 40;
    public static final int FIZZ = 32;

    private int tick;

    public StoneExtruderBlockEntity(BlockPos pos, BlockState state) {
        super(ArcaneStoneFeature.STONE_EXTRUDER_ENTITY.get(), pos, state);
    }

    public int tick() {
        return tick;
    }

    void serverTick() {
        if (!canWork()) {
            tick = 0;
            return;
        }
        tick++;
        Level level = getLevel();
        if (tick == FIZZ) {
            level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F);
        }
        if (tick >= CYCLE) {
            extrude();
            level.playSound(null, worldPosition, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.5F, 2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F);
            tick = 0;
        }
    }

    private Direction facing() {
        return getBlockState().getValue(StoneExtruderBlock.FACING);
    }

    public boolean canWork() {
        Level level = getLevel();
        if (level == null || getBlockState().getValue(StoneExtruderBlock.POWERED)) {
            return false;
        }
        Direction facing = facing();
        BlockPos target = worldPosition.relative(facing);
        return !Transfer.hasItems(level, target, facing.getOpposite())
            || Transfer.insert(level, target, facing.getOpposite(), new ItemStack(Items.COBBLESTONE), true).isEmpty();
    }

    private void extrude() {
        Level level = getLevel();
        Direction facing = facing();
        BlockPos target = worldPosition.relative(facing);
        ItemStack cobble = new ItemStack(Items.COBBLESTONE);
        if (Transfer.hasItems(level, target, facing.getOpposite())) {
            cobble = Transfer.insert(level, target, facing.getOpposite(), cobble, false);
        }
        if (cobble.isEmpty()) {
            return;
        }
        ItemEntity item;
        if (facing.getAxis() == Direction.Axis.Y) {
            item = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5 + facing.getStepY() * 0.66, worldPosition.getZ() + 0.5, cobble);
            item.setDeltaMovement(0.025, 0.075 * facing.getStepY(), 0.025);
        } else {
            item = new ItemEntity(level, worldPosition.getX() + 0.5 + facing.getStepX() * 0.66, worldPosition.getY() + 0.4, worldPosition.getZ() + 0.5 + facing.getStepZ() * 0.66, cobble);
            item.setDeltaMovement(0.075 * facing.getStepX(), 0.025, 0.075 * facing.getStepZ());
        }
        level.addFreshEntity(item);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tick = input.getIntOr("tick", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("tick", tick);
    }
}
