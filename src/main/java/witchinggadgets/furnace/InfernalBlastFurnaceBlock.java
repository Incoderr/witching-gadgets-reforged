package witchinggadgets.furnace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class InfernalBlastFurnaceBlock extends Block implements EntityBlock {
    public static final IntegerProperty POSITION = IntegerProperty.create("position", 0, 26);
    public static final IntegerProperty HEAT = IntegerProperty.create("heat", 0, 2);
    public static final int LAVA = 22;
    public static final int MASTER = 4;
    public static final int EVENT_FIZZ = 5;

    private static final VoxelShape[] TOP = new VoxelShape[9];

    static {
        for (int p = 0; p < 9; p++) {
            TOP[p] = Shapes.or(
                Block.box(0, 0, 0, 16, 8, 16),
                Block.box(p % 3 == 0 ? 8 : 0, 8, p < 3 ? 8 : 0, (p + 1) % 3 == 0 ? 8 : 16, 16, p > 5 ? 8 : 16)
            );
        }
    }

    public InfernalBlastFurnaceBlock(Properties properties) {
        super(properties.noOcclusion().lightLevel(InfernalBlastFurnaceBlock::light));
        registerDefaultState(stateDefinition.any().setValue(POSITION, MASTER).setValue(HEAT, 0));
    }

    private static int light(BlockState state) {
        int position = state.getValue(POSITION);
        return position == 10 || position == 12 || position == 14 || position == 16 ? 13 : position == LAVA ? 15 : 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POSITION, HEAT);
    }

    public static BlockPos master(BlockPos pos, int position) {
        return pos.offset(-(position % 3 - 1), -(position / 9), -((position / 3) % 3 - 1));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int position = state.getValue(POSITION);
        if (position == LAVA) {
            return Shapes.empty();
        }
        return position >= 18 ? TOP[position - 18] : Shapes.block();
    }

    @Override
    protected VoxelShape getEntityInsideCollisionShape(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return state.getValue(POSITION) == LAVA ? Block.box(0, 0, 0, 16, 14, 16) : Shapes.empty();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (state.getValue(POSITION) != LAVA || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (entity instanceof ItemEntity item) {
            if (BlastFurnaceRecipe.forInput(item.getItem()) == null) {
                level.blockEvent(pos, this, EVENT_FIZZ, 0);
                item.discard();
                return;
            }
            if (level.getBlockEntity(master(pos, LAVA)) instanceof InfernalBlastFurnaceBlockEntity master) {
                master.addInput(item.getItem().copy());
                item.discard();
            }
        } else if (entity instanceof LivingEntity living && !living.fireImmune()) {
            living.hurtServer(serverLevel, serverLevel.damageSources().lava(), 3.0F);
            living.igniteForSeconds(10.0F);
        }
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id == EVENT_FIZZ) {
            if (level.isClientSide()) {
                for (int i = 0; i < 3; i++) {
                    level.addParticle(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
                }
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 2.5, pos.getZ() + 0.5, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F, false);
            }
            return true;
        }
        return super.triggerEvent(state, level, pos, id, param);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        if (!level.getBlockState(pos).is(this)) {
            BlastFurnaceStructure.disassemble(level, master(pos, state.getValue(POSITION)), pos);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfernalBlastFurnaceBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != FurnaceFeature.BLAST_FURNACE_ENTITY.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> ((InfernalBlastFurnaceBlockEntity) entity).serverTick();
    }
}
