package witchinggadgets.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import org.jspecify.annotations.Nullable;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.blockentity.WardedBlockEntity;

public abstract class CamoBlockEntity extends TcBlockEntity {
    protected CamoBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract @Nullable BlockState getDisguise();

    @Override
    public ModelData getModelData() {
        BlockState disguise = getDisguise();
        return disguise == null ? ModelData.EMPTY : ModelData.builder().with(WardedBlockEntity.DISGUISE, disguise).build();
    }

    protected void disguiseChanged() {
        if (level != null && level.isClientSide()) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 8);
        }
    }
}
