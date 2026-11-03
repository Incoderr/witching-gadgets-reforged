package witchinggadgets.arcanestone;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AgeingStoneBlockEntity extends BlockEntity {
    public AgeingStoneBlockEntity(BlockPos pos, BlockState state) {
        super(ArcaneStoneFeature.AGEING_STONE_ENTITY.get(), pos, state);
    }
}
