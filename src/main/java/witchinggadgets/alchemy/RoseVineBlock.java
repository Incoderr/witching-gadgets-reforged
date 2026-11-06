package witchinggadgets.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;
import thaumcraft.wand.WandInteractable;

public class RoseVineBlock extends Block implements WandInteractable {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 5);
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;

    public RoseVineBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0).setValue(DOWN, false).setValue(NORTH, false).setValue(SOUTH, false).setValue(WEST, false).setValue(EAST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, DOWN, NORTH, SOUTH, WEST, EAST);
    }

    private static boolean solid(BlockGetter level, BlockPos pos, Direction face) {
        return level.getBlockState(pos).isFaceSturdy(level, pos, face);
    }

    public static boolean canRoot(BlockGetter level, BlockPos pos) {
        return solid(level, pos.below(), Direction.UP);
    }

    public static boolean hasClimbableWall(BlockGetter level, BlockPos pos) {
        return solid(level, pos.south(), Direction.NORTH)
            || solid(level, pos.east(), Direction.EAST)
            || solid(level, pos.north(), Direction.SOUTH)
            || solid(level, pos.west(), Direction.WEST);
    }

    public BlockState supported(BlockState state, BlockGetter level, BlockPos pos) {
        return state
            .setValue(DOWN, canRoot(level, pos))
            .setValue(NORTH, solid(level, pos.north(), Direction.SOUTH))
            .setValue(SOUTH, solid(level, pos.south(), Direction.NORTH))
            .setValue(WEST, solid(level, pos.west(), Direction.WEST))
            .setValue(EAST, solid(level, pos.east(), Direction.EAST));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return supported(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return supported(state, level, pos);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        spread(level, pos, state, random, false);
    }

    public void spread(Level level, BlockPos pos, BlockState state, RandomSource random, boolean ignoreRandom) {
        int age = state.getValue(AGE);
        if (age < 5) {
            level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
            return;
        }
        BlockPos above = pos.above();
        boolean placeable = level.isEmptyBlock(above) && (canRoot(level, above) || hasClimbableWall(level, above));
        if (placeable && (ignoreRandom || random.nextInt(4) == 0)
            && (solid(level, above.north(), Direction.SOUTH) || solid(level, above.south(), Direction.NORTH)
            || solid(level, above.west(), Direction.WEST) || solid(level, above.east(), Direction.EAST))) {
            level.setBlockAndUpdate(above, supported(defaultBlockState(), level, above));
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level instanceof ServerLevel serverLevel && entity instanceof LivingEntity living) {
            living.hurt(serverLevel.damageSources().generic(), 1.0F);
        }
    }

    @Override
    public InteractionResult onWandUseOn(Level level, BlockPos pos, Player player, ItemStack wand, Direction side) {
        if (player.isCreative() && !level.isClientSide()) {
            spread(level, pos, level.getBlockState(pos), level.getRandom(), true);
        }
        return InteractionResult.SUCCESS;
    }
}
