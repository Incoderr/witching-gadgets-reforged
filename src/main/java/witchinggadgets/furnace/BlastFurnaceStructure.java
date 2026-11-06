package witchinggadgets.furnace;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.Fluids;

public final class BlastFurnaceStructure {
    private static final ThreadLocal<Boolean> CHANGING = ThreadLocal.withInitial(() -> false);

    private BlastFurnaceStructure() {
    }

    public static int position(int xx, int yy, int zz) {
        return yy * 9 + (zz + 1) * 3 + (xx + 1);
    }

    public static Block brick(int position) {
        if (position < 9 && position != InfernalBlastFurnaceBlock.MASTER) {
            return Blocks.NETHER_BRICKS;
        }
        if (position == 10 || position == 12 || position == 13 || position == 14 || position == 16) {
            return Blocks.SOUL_SAND;
        }
        return Blocks.OBSIDIAN;
    }

    public static Direction stairFacing(int xx, int zz) {
        return xx == -1 ? Direction.EAST : xx == 1 ? Direction.WEST : zz == -1 ? Direction.SOUTH : Direction.NORTH;
    }

    private static boolean stair(Level level, BlockPos pos, Direction... facings) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.NETHER_BRICK_STAIRS) || state.getValue(StairBlock.HALF) != Half.BOTTOM) {
            return false;
        }
        for (Direction facing : facings) {
            if (state.getValue(StairBlock.FACING) == facing) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValid(Level level, BlockPos master) {
        int x = master.getX();
        int y = master.getY();
        int z = master.getZ();
        boolean valid = stair(level, new BlockPos(x - 1, y + 2, z - 1), Direction.EAST, Direction.SOUTH)
            && stair(level, new BlockPos(x - 1, y + 2, z), Direction.EAST)
            && stair(level, new BlockPos(x - 1, y + 2, z + 1), Direction.EAST, Direction.NORTH)
            && stair(level, new BlockPos(x, y + 2, z - 1), Direction.SOUTH)
            && stair(level, new BlockPos(x, y + 2, z + 1), Direction.NORTH)
            && stair(level, new BlockPos(x + 1, y + 2, z - 1), Direction.WEST, Direction.SOUTH)
            && stair(level, new BlockPos(x + 1, y + 2, z), Direction.WEST)
            && stair(level, new BlockPos(x + 1, y + 2, z + 1), Direction.WEST, Direction.NORTH);
        for (int yy = 0; yy <= 1 && valid; yy++) {
            for (int xx = -1; xx <= 1 && valid; xx++) {
                for (int zz = -1; zz <= 1 && valid; zz++) {
                    valid = level.getBlockState(master.offset(xx, yy, zz)).is(brick(position(xx, yy, zz)));
                }
            }
        }
        return valid;
    }

    public static List<BlockPos> nearbyLava(Level level, BlockPos clicked) {
        List<BlockPos> result = new ArrayList<>();
        for (int yy = 0; yy <= 2; yy++) {
            for (int xx = -1; xx <= 1; xx++) {
                for (int zz = -1; zz <= 1; zz++) {
                    BlockPos pos = clicked.offset(xx, yy, zz);
                    if (level.getFluidState(pos).is(Fluids.LAVA) && level.getBlockState(pos).is(Blocks.LAVA)) {
                        result.add(pos);
                    }
                }
            }
        }
        return result;
    }

    public static BlockPos findMaster(Level level, BlockPos clicked) {
        for (BlockPos lava : nearbyLava(level, clicked)) {
            BlockPos master = lava.below(2);
            if (isValid(level, master)) {
                return master;
            }
        }
        return null;
    }

    public static void form(Level level, BlockPos master, Direction facing) {
        CHANGING.set(true);
        try {
            BlockState base = FurnaceFeature.INFERNAL_BLAST_FURNACE.get().defaultBlockState();
            for (int yy = 0; yy <= 2; yy++) {
                for (int zz = -1; zz <= 1; zz++) {
                    for (int xx = -1; xx <= 1; xx++) {
                        BlockPos pos = master.offset(xx, yy, zz);
                        level.setBlock(pos, base.setValue(InfernalBlastFurnaceBlock.POSITION, position(xx, yy, zz)), Block.UPDATE_ALL);
                    }
                }
            }
        } finally {
            CHANGING.set(false);
        }
        if (level.getBlockEntity(master) instanceof InfernalBlastFurnaceBlockEntity entity) {
            entity.setFacing(facing);
        }
    }

    public static void setHeat(Level level, BlockPos master, int heat) {
        CHANGING.set(true);
        try {
            for (int yy = 0; yy <= 2; yy++) {
                for (int zz = -1; zz <= 1; zz++) {
                    for (int xx = -1; xx <= 1; xx++) {
                        BlockPos pos = master.offset(xx, yy, zz);
                        BlockState state = level.getBlockState(pos);
                        if (state.getBlock() instanceof InfernalBlastFurnaceBlock && state.getValue(InfernalBlastFurnaceBlock.HEAT) != heat) {
                            level.setBlock(pos, state.setValue(InfernalBlastFurnaceBlock.HEAT, heat), Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        } finally {
            CHANGING.set(false);
        }
    }

    public static void disassemble(Level level, BlockPos master, BlockPos broken) {
        if (CHANGING.get()) {
            return;
        }
        CHANGING.set(true);
        try {
            for (int yy = 0; yy <= 2; yy++) {
                for (int xx = -1; xx <= 1; xx++) {
                    for (int zz = -1; zz <= 1; zz++) {
                        BlockPos pos = master.offset(xx, yy, zz);
                        if (pos.equals(broken) || !(level.getBlockState(pos).getBlock() instanceof InfernalBlastFurnaceBlock)) {
                            continue;
                        }
                        level.setBlock(pos, original(xx, yy, zz), Block.UPDATE_ALL);
                    }
                }
            }
        } finally {
            CHANGING.set(false);
        }
    }

    public static BlockState original(int xx, int yy, int zz) {
        if (yy < 2) {
            return brick(position(xx, yy, zz)).defaultBlockState();
        }
        if (xx == 0 && zz == 0) {
            return Blocks.LAVA.defaultBlockState();
        }
        return Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, stairFacing(xx, zz)).setValue(StairBlock.HALF, Half.BOTTOM);
    }
}
