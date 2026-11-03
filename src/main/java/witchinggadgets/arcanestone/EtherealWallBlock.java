package witchinggadgets.arcanestone;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class EtherealWallBlock extends Block implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final int MAX_NETWORK = 1024;

    public EtherealWallBlock(Properties properties) {
        super(properties.noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(POWERED) ? Shapes.empty() : Shapes.block();
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof EtherealWallBlockEntity wall) {
            return wall.disguise().map(disguise -> disguise.getLightEmission()).orElse(0);
        }
        return 0;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (!level.isClientSide()) {
            updateNetwork(level, pos);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !oldState.is(this)) {
            updateNetwork(level, pos);
        }
    }

    public static Set<BlockPos> network(Level level, BlockPos start) {
        Set<BlockPos> network = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        network.add(start);
        while (!queue.isEmpty() && network.size() < MAX_NETWORK) {
            BlockPos pos = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (!network.contains(next) && level.getBlockState(next).getBlock() instanceof EtherealWallBlock) {
                    network.add(next);
                    queue.add(next);
                }
            }
        }
        return network;
    }

    public static void updateNetwork(Level level, BlockPos start) {
        Set<BlockPos> network = network(level, start);
        boolean powered = network.stream().anyMatch(level::hasNeighborSignal);
        for (BlockPos pos : network) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof EtherealWallBlock && state.getValue(POWERED) != powered) {
                level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        BlockState disguise = blockItem.getBlock().defaultBlockState();
        if (!canDisguiseAs(disguise, level, pos)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof EtherealWallBlockEntity wall) {
            wall.setDisguise(disguise);
            level.getLightEngine().checkBlock(pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof EtherealWallBlockEntity wall) {
            wall.setDisguise(null);
            level.getLightEngine().checkBlock(pos);
        }
        return InteractionResult.SUCCESS;
    }

    public static boolean canDisguiseAs(BlockState disguise, BlockGetter level, BlockPos pos) {
        return !disguise.isAir()
            && !(disguise.getBlock() instanceof EtherealWallBlock)
            && !(disguise.getBlock() instanceof EntityBlock)
            && disguise.getRenderShape() == RenderShape.MODEL
            && disguise.isCollisionShapeFullBlock(level, pos);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EtherealWallBlockEntity(pos, state);
    }
}
