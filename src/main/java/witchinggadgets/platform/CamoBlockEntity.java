package witchinggadgets.platform;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.platform.CamouflagedBlockEntity;

public abstract class CamoBlockEntity extends CamouflagedBlockEntity {
    protected CamoBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public abstract @Nullable BlockState getDisguise();

    protected void disguiseChanged() {
        if (level != null && level.isClientSide()) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 8);
        }
    }
}
