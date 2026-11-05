package witchinggadgets.gems;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import thaumcraft.registry.ModBlocks;

public class GemcuttingToolsItem extends Item {
    public GemcuttingToolsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || !level.getBlockState(pos).is(ModBlocks.TABLE.get())) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, GemsFeature.CUTTING_TABLE.get().defaultBlockState().setValue(CuttingTableBlock.FACING, CuttingTableBlock.facingFor(player)), 3);
        }
        return InteractionResult.SUCCESS;
    }
}
