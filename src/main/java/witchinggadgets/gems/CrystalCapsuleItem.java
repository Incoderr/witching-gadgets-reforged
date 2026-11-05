package witchinggadgets.gems;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import javax.annotation.Nullable;
import thaumcraft.platform.Transfer;
import witchinggadgets.registry.CreativeVariants;

public class CrystalCapsuleItem extends Item implements CreativeVariants {
    public static final int CAPACITY = 1000;

    public CrystalCapsuleItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static Fluid fluid(ItemStack stack) {
        String id = GemsFeature.CAPSULE_FLUID.get(stack);
        if (id == null) {
            return null;
        }
        ResourceLocation key = ResourceLocation.tryParse(id);
        if (key == null) {
            return null;
        }
        Fluid fluid = BuiltInRegistries.FLUID.get(key);
        return fluid == Fluids.EMPTY ? null : fluid;
    }

    public static ItemStack filled(Fluid fluid) {
        ItemStack stack = new ItemStack(GemsFeature.CRYSTAL_CAPSULE.get());
        GemsFeature.CAPSULE_FLUID.set(stack, BuiltInRegistries.FLUID.getKey(fluid).toString());
        return stack;
    }

    public static ItemStack empty() {
        return new ItemStack(GemsFeature.CRYSTAL_CAPSULE.get());
    }

    public static ItemStack use(Level level, BlockPos pos, ItemStack stack) {
        Fluid stored = fluid(stack);
        if (stored != null) {
            BlockState state = stored.defaultFluidState().createLegacyBlock();
            if (state.isAir()) {
                return ItemStack.EMPTY;
            }
            level.setBlock(pos, state, 11);
            stack.shrink(1);
            return empty();
        }
        BlockState state = level.getBlockState(pos);
        FluidState fluid = state.getFluidState();
        if (!(state.getBlock() instanceof LiquidBlock) || fluid.isEmpty() || !fluid.isSource()) {
            return ItemStack.EMPTY;
        }
        ItemStack filled = filled(fluid.getType());
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
        stack.shrink(1);
        return filled;
    }

    private static boolean tryTank(Level level, BlockPos pos, Direction side, ItemStack stack, Player player) {
        if (!Transfer.hasFluids(level, pos, side)) {
            return false;
        }
        Fluid stored = fluid(stack);
        if (stored == null) {
            FluidStack drained = Transfer.extractFluid(level, pos, side, candidate -> true, CAPACITY, true);
            if (drained.getAmount() < CAPACITY) {
                return false;
            }
            drained = Transfer.extractFluid(level, pos, side, candidate -> true, CAPACITY, false);
            give(player, filled(drained.getFluid()));
            stack.shrink(1);
            return true;
        }
        FluidStack content = new FluidStack(stored, CAPACITY);
        if (!Transfer.insertFluid(level, pos, side, content, true).isEmpty()) {
            return false;
        }
        Transfer.insertFluid(level, pos, side, content, false);
        give(player, empty());
        stack.shrink(1);
        return true;
    }

    private static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(player.blockInteractionRange()));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        if (tryTank(level, hit.getBlockPos(), hit.getDirection(), stack, player)) {
            return InteractionResultHolder.success(stack);
        }
        BlockPos target = hit.getBlockPos().relative(hit.getDirection());
        BlockState state = level.getBlockState(target);
        if (!level.mayInteract(player, target) || !player.mayUseItemAt(target, hit.getDirection(), stack) || !state.isAir() && state.isSolid()) {
            return InteractionResultHolder.success(stack);
        }
        ItemStack result = use(level, target, stack);
        if (!result.isEmpty()) {
            give(player, result);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void creativeStacks(Consumer<ItemStack> output) {
        output.accept(empty());
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid != Fluids.EMPTY && fluid.defaultFluidState().isSource() && !fluid.defaultFluidState().createLegacyBlock().isAir()) {
                output.accept(filled(fluid));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> builder, TooltipFlag flag) {
        Fluid fluid = fluid(stack);
        if (fluid != null) {
            builder.add(fluid.getFluidType().getDescription());
        }
    }
}
