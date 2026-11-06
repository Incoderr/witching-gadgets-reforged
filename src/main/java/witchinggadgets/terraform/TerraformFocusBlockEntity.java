package witchinggadgets.terraform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TerraformFocusBlockEntity extends BlockEntity {
    public TerraformFocusBlockEntity(BlockPos pos, BlockState state) {
        super(TerraformFeature.FOCUS_ENTITY.get(), pos, state);
    }

    public TerraformFocus focus() {
        return ((TerraformFocusBlock) getBlockState().getBlock()).focus();
    }
}
