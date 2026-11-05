package witchinggadgets.gems;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;

public class GlimmerBlockEntity extends TcBlockEntity {
    private int tick;
    private int lifetime = 20 * 20;

    public GlimmerBlockEntity(BlockPos pos, BlockState state) {
        super(GemsFeature.GLIMMER_ENTITY.get(), pos, state);
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
        setChanged();
    }

    public int lifetime() {
        return lifetime;
    }

    public void serverTick() {
        if (tick < lifetime) {
            tick++;
        } else if (level != null) {
            level.removeBlock(worldPosition, false);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tick = input.getIntOr("tickCount", 0);
        lifetime = input.getIntOr("tickMax", 20 * 20);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("tickCount", tick);
        output.putInt("tickMax", lifetime);
    }
}
