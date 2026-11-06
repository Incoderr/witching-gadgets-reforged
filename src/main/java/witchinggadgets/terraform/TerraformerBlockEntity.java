package witchinggadgets.terraform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.essentia.EssentiaTransport;
import thaumcraft.platform.BiomeEdit;
import thaumcraft.vis.VisNet;

public class TerraformerBlockEntity extends TcBlockEntity implements EssentiaTransport {
    public static final int EVENT_VENT = 1;
    public static final int COST = 4;

    private @Nullable Aspect suction;
    private AspectList essentia = new AspectList();
    private int tick;
    private int drawDelay;

    public TerraformerBlockEntity(BlockPos pos, BlockState state) {
        super(TerraformFeature.TERRAFORMER_ENTITY.get(), pos, state);
    }

    public @Nullable TerraformFocus focus() {
        return level != null && level.getBlockState(worldPosition.above()).getBlock() instanceof TerraformFocusBlock block ? block.focus() : null;
    }

    public @Nullable Aspect suction() {
        return suction;
    }

    public AspectList essentia() {
        return essentia;
    }

    public void addEssentia(Aspect aspect, int amount) {
        essentia.add(aspect, amount);
        sync();
    }

    public void serverTick() {
        tick++;
        TerraformFocus focus = focus();
        Aspect wanted = focus == null ? null : focus.aspect();
        if (wanted != suction) {
            suction = wanted;
            sync();
        }
        if (focus != null && suction != null && level instanceof ServerLevel serverLevel) {
            if (drawEssentia()) {
                addEssentia(suction, 1);
            }
            if (tick >= 20 && essentia.getAmount(suction) >= COST) {
                transform(serverLevel, focus);
            }
        }
        if (tick >= 20) {
            tick = 0;
        }
    }

    public boolean transform(ServerLevel level, TerraformFocus focus) {
        Holder<Biome> biome = level.registryAccess().lookupOrThrow(Registries.BIOME).get(focus.biome()).orElse(null);
        if (biome == null) {
            return false;
        }
        RandomSource random = level.getRandom();
        BlockPos target = offset(random);
        int attempts = 0;
        while (attempts < 40 && BiomeEdit.getColumnBiome(level, target).is(focus.biome())) {
            attempts++;
            target = offset(random);
        }
        if (BiomeEdit.getColumnBiome(level, target).is(focus.biome()) || VisNet.drainVis(level, worldPosition, Aspect.EARTH, COST) != COST) {
            return false;
        }
        BiomeEdit.setColumn(level, target, biome);
        essentia.remove(focus.aspect(), COST);
        sync();
        level.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_VENT, focus.aspect().getColor());
        return true;
    }

    private BlockPos offset(RandomSource random) {
        return worldPosition.offset(random.nextInt(16) - random.nextInt(16), 0, random.nextInt(16) - random.nextInt(16));
    }

    private boolean drawEssentia() {
        if (++drawDelay % 5 != 0 || !(level.getBlockEntity(worldPosition.below()) instanceof EssentiaTransport transport)) {
            return false;
        }
        if (!transport.canOutputTo(Direction.UP)) {
            return false;
        }
        return transport.getSuctionAmount(Direction.UP) < getSuctionAmount(Direction.DOWN) && transport.takeEssentia(suction, 1, Direction.UP) == 1;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
        suction = aspect;
    }

    @Override
    public @Nullable Aspect getSuctionType(Direction face) {
        return suction;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return suction != null ? 128 : 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        if (!canInputFrom(face)) {
            return 0;
        }
        addEssentia(aspect, amount);
        return amount;
    }

    @Override
    public @Nullable Aspect getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        essentia = input.read("essentia", AspectList.CODEC).orElseGet(AspectList::new);
        tick = input.getIntOr("tick", 0);
        suction = input.getString("suction").map(Aspect::getAspect).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("essentia", AspectList.CODEC, essentia);
        output.putInt("tick", tick);
        if (suction != null) {
            output.putString("suction", suction.getTag());
        }
    }
}
