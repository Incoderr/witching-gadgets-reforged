package witchinggadgets.arcanestone;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import javax.annotation.Nullable;
import witchinggadgets.platform.CamoBlockEntity;

public class EtherealWallBlockEntity extends CamoBlockEntity {
    private @Nullable BlockState disguise;

    public EtherealWallBlockEntity(BlockPos pos, BlockState state) {
        super(ArcaneStoneFeature.ETHEREAL_WALL_ENTITY.get(), pos, state);
    }

    @Override
    public @Nullable BlockState getDisguise() {
        return disguise;
    }

    public void setDisguise(@Nullable BlockState disguise) {
        this.disguise = disguise;
        setChanged();
        sync();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        BlockState previous = disguise;
        disguise = input.read("disguise", BlockState.CODEC).orElse(null);
        if (!Objects.equals(previous, disguise)) {
            disguiseChanged();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("disguise", BlockState.CODEC, disguise);
    }

    Optional<BlockState> disguise() {
        return Optional.ofNullable(disguise);
    }
}
