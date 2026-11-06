package witchinggadgets.terraform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import thaumcraft.fx.Fx;

public class TerraformerBlock extends Block implements EntityBlock {
    public TerraformerBlock(Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id == TerraformerBlockEntity.EVENT_VENT) {
            if (level.isClientSide()) {
                for (int j = 0; j < 4; j++) {
                    for (Direction direction : Direction.Plane.HORIZONTAL) {
                        Fx.get().ventParticles(level,
                            pos.getX() + 0.5 + 0.6875 * direction.getStepX(), pos.getY() + 0.875, pos.getZ() + 0.5 + 0.6875 * direction.getStepZ(),
                            0.1875, 0.25, 0.1875, param, 2.0F);
                    }
                }
            }
            return true;
        }
        return super.triggerEvent(state, level, pos, id, param);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TerraformerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != TerraformFeature.TERRAFORMER_ENTITY.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> ((TerraformerBlockEntity) entity).serverTick();
    }
}
