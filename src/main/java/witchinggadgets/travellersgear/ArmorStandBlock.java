package witchinggadgets.travellersgear;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import thaumcraft.platform.Menus;

public class ArmorStandBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ArmorStandBlock(Properties properties) {
        super(properties.noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!(level.getBlockEntity(pos) instanceof ArmorStandBlockEntity stand)) {
            return Shapes.block();
        }
        double top = stand.flag(ArmorStandBlockEntity.RENDER_ARMOR) ? 32.0 : stand.flag(ArmorStandBlockEntity.RENDER_ARMOR + 1) ? 29.0 : 16.0;
        double depth = stand.flag(ArmorStandBlockEntity.RENDER_TABLE) ? 0.0 : 4.0;
        return state.getValue(FACING).getAxis() == Direction.Axis.Z
            ? Block.box(0.0, 0.0, depth, 16.0, top, 16.0 - depth)
            : Block.box(depth, 0.0, 0.0, 16.0 - depth, top, 16.0);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = getShape(state, level, pos, context);
        return Shapes.join(shape, Block.box(0.0, 0.0, 0.0, 16.0, 24.0, 16.0), net.minecraft.world.phys.shapes.BooleanOp.AND);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof ArmorStandBlockEntity stand) {
            Menus.open(serverPlayer, stand, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArmorStandBlockEntity(pos, state);
    }
}
