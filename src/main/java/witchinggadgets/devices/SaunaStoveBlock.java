package witchinggadgets.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import javax.annotation.Nullable;
import witchinggadgets.fx.WgFx;

public class SaunaStoveBlock extends Block implements EntityBlock {
    public SaunaStoveBlock(Properties properties) {
        super(properties.noOcclusion().lightLevel(state -> 8));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(stack.getItem() instanceof BucketItem bucket) || bucket.content != Fluids.WATER) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SaunaStoveBlockEntity stove && stove.fill(new FluidStack(Fluids.WATER, SaunaStoveBlockEntity.BUCKET))) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
            level.playSound(null, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.33F, 1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof SaunaStoveBlockEntity stove) {
            stove.prepareAreaCheck();
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof SaunaStoveBlockEntity stove && stove.tick() > 0) {
            WgFx.get().saunaSteam(level, pos);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SaunaStoveBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != DevicesFeature.SAUNA_STOVE_ENTITY.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> ((SaunaStoveBlockEntity) entity).tickStove();
    }
}
