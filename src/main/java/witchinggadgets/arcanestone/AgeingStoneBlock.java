package witchinggadgets.arcanestone;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import javax.annotation.Nullable;

public class AgeingStoneBlock extends Block implements EntityBlock {
    public AgeingStoneBlock(Properties properties) {
        super(properties);
    }

    public static AABB area(BlockPos pos) {
        return new AABB(pos.getX() - 3, pos.getY() - 2, pos.getZ() - 3, pos.getX() + 4, pos.getY() + 3, pos.getZ() + 4);
    }

    public static void ageNearby(Level level, BlockPos pos) {
        for (AgeableMob mob : level.getEntitiesOfClass(AgeableMob.class, area(pos))) {
            int age = mob.getAge();
            if (age < 0) {
                mob.setAge(age + 1);
            } else if (age > 0) {
                mob.setAge(age - 1);
            }
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AgeingStoneBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != ArcaneStoneFeature.AGEING_STONE_ENTITY.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> ageNearby(tickLevel, pos);
    }
}
