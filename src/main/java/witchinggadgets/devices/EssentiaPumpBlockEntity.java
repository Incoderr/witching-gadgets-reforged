package witchinggadgets.devices;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.device.EssentiaMirrorBlockEntity;
import thaumcraft.device.MirrorBlock;
import thaumcraft.essentia.EssentiaTransport;
import thaumcraft.network.EssentiaSourceFxPayload;
import thaumcraft.platform.ModNetwork;

public class EssentiaPumpBlockEntity extends TcBlockEntity implements EssentiaTransport {
    public static final int RANGE = 8;

    private @Nullable Aspect aspect;
    private int amount;

    public EssentiaPumpBlockEntity(BlockPos pos, BlockState state) {
        super(DevicesFeature.ESSENTIA_PUMP_ENTITY.get(), pos, state);
    }

    public Direction facing() {
        return getBlockState().getValue(EssentiaPumpBlock.FACING);
    }

    public @Nullable Aspect aspect() {
        return aspect;
    }

    public int amount() {
        return amount;
    }

    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Direction facing = facing();
        Aspect wanted = level.getBlockEntity(worldPosition.relative(facing)) instanceof EssentiaTransport transport ? transport.getSuctionType(facing.getOpposite()) : null;
        if (wanted == null || aspect != null && aspect != wanted || amount >= 1) {
            return;
        }
        for (EssentiaMirrorBlockEntity mirror : mirrors()) {
            if (mirror.takeFromContainer(wanted, 1)) {
                aspect = wanted;
                amount++;
                sync();
                BlockPos source = mirror.getBlockPos();
                ModNetwork.sendToNear(serverLevel, null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 32.0, new EssentiaSourceFxPayload(
                    worldPosition,
                    (byte) (worldPosition.getX() - source.getX()),
                    (byte) (worldPosition.getY() - source.getY()),
                    (byte) (worldPosition.getZ() - source.getZ()),
                    wanted.getColor()
                ));
                return;
            }
        }
    }

    public List<EssentiaMirrorBlockEntity> mirrors() {
        List<EssentiaMirrorBlockEntity> list = new ArrayList<>();
        Direction back = facing().getOpposite();
        for (int h = -RANGE; h <= RANGE; h++) {
            for (int w = -RANGE; w <= RANGE; w++) {
                for (int l = 1; l < RANGE; l++) {
                    BlockPos pos;
                    if (back.getStepY() != 0) {
                        pos = worldPosition.offset(w, l * back.getStepY(), h);
                    } else if (back.getStepX() != 0) {
                        pos = worldPosition.offset(l * back.getStepX(), h, w);
                    } else {
                        pos = worldPosition.offset(w, h, l * back.getStepZ());
                    }
                    if (level.getBlockEntity(pos) instanceof EssentiaMirrorBlockEntity mirror && canSee(mirror)) {
                        list.add(mirror);
                    }
                }
            }
        }
        return list;
    }

    private boolean canSee(EssentiaMirrorBlockEntity mirror) {
        Direction back = facing().getOpposite();
        Vec3 from = new Vec3(worldPosition.getX() + 0.5 + back.getStepX(), worldPosition.getY() + 0.5 + back.getStepY(), worldPosition.getZ() + 0.5 + back.getStepZ());
        BlockPos mirrorPos = mirror.getBlockPos();
        Direction face = mirror.getBlockState().getValue(MirrorBlock.FACING);
        Vec3 to = new Vec3(mirrorPos.getX() + 0.5 + face.getStepX(), worldPosition.getY() + 0.5 + face.getStepY(), mirrorPos.getZ() + 0.5 + face.getStepZ());
        BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(worldPosition) || hit.getBlockPos().equals(mirrorPos);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == facing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == facing();
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public @Nullable Aspect getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        if (this.amount >= amount) {
            this.amount -= amount;
            if (this.amount <= 0) {
                this.aspect = null;
            }
            sync();
            return amount;
        }
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public @Nullable Aspect getEssentiaType(Direction face) {
        return aspect;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return amount;
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
        aspect = input.getString("aspect").map(Aspect::getAspect).orElse(null);
        amount = input.getIntOr("amount", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (aspect != null) {
            output.putString("aspect", aspect.getTag());
        }
        output.putInt("amount", amount);
    }
}
