package witchinggadgets.devices;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import javax.annotation.Nullable;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.platform.Transfer;

public class SaunaStoveBlockEntity extends TcBlockEntity {
    public static final int CAPACITY = 4000;
    public static final int BUCKET = 1000;
    public static final int DURATION = 1200;
    private static final int AREA_LIMIT = 1200;
    private static final int STEPS_PER_TICK = 30;
    private static final int MAX_BOX = 32;
    private static final Map<Integer, SaunaStoveBlockEntity> SERVER_TARGETS = new HashMap<>();
    private static final Map<Integer, SaunaStoveBlockEntity> CLIENT_TARGETS = new HashMap<>();

    private final Transfer.FluidTank tank = new Transfer.FluidTank(CAPACITY, stack -> stack.is(Fluids.WATER), true, this::sync);
    private final ArrayDeque<BlockPos> open = new ArrayDeque<>();
    private final Set<BlockPos> closed = new HashSet<>();
    private final Set<BlockPos> checked = new HashSet<>();
    private List<AABB> boxes = List.of();
    private boolean recheck;
    private int tick;

    public SaunaStoveBlockEntity(BlockPos pos, BlockState state) {
        super(DevicesFeature.SAUNA_STOVE_ENTITY.get(), pos, state);
    }

    public Transfer.FluidTank tank() {
        return tank;
    }

    public @Nullable Transfer.FluidTank exposedFluids(@Nullable Direction side) {
        return side == Direction.UP ? null : tank;
    }

    public int tick() {
        return tick;
    }

    public List<AABB> boxes() {
        return boxes;
    }

    public boolean fill(FluidStack stack) {
        if (!stack.is(Fluids.WATER) || tank.getAmount() >= CAPACITY) {
            return false;
        }
        tank.setFluid(new FluidStack(Fluids.WATER, Math.min(CAPACITY, tank.getAmount() + stack.getAmount())));
        return true;
    }

    public void prepareAreaCheck() {
        recheck = true;
        open.clear();
        closed.clear();
        checked.clear();
        open.add(worldPosition.east());
        open.add(worldPosition.west());
        open.add(worldPosition.south());
        open.add(worldPosition.north());
        open.add(worldPosition.above());
    }

    void tickStove() {
        Level level = getLevel();
        if (tick > 0 && level.getGameTime() % 10 == 0) {
            tick--;
            if (boxes.isEmpty() && !recheck) {
                prepareAreaCheck();
            }
        }
        if (!level.isClientSide() && !recheck && tick <= 0 && tank.getAmount() >= BUCKET) {
            int remaining = tank.getAmount() - BUCKET;
            tank.setFluid(remaining > 0 ? new FluidStack(Fluids.WATER, remaining) : FluidStack.EMPTY);
            prepareAreaCheck();
            tick = DURATION;
            sync();
        }
        if (recheck) {
            checkArea();
        }
        if (tick > 0 && level.getGameTime() % 20 == 0) {
            Map<Integer, SaunaStoveBlockEntity> targets = targets(level);
            for (AABB box : boxes) {
                for (Player player : level.getEntitiesOfClass(Player.class, box)) {
                    targets.put(player.getId(), this);
                }
            }
        }
    }

    private void checkArea() {
        int steps = 0;
        while (steps < STEPS_PER_TICK && closed.size() < AREA_LIMIT && !open.isEmpty()) {
            steps++;
            BlockPos next = open.poll();
            if (!checked.add(next)) {
                continue;
            }
            int dx = worldPosition.getX() - next.getX();
            int dz = worldPosition.getZ() - next.getZ();
            int dy = next.getY() - worldPosition.getY();
            if (passable(next) && dx * dx + dz * dz < 64 && dy < (overWater(next) ? 2 : 6) && dy > -6) {
                closed.add(next);
                for (Direction direction : Direction.values()) {
                    BlockPos neighbour = next.relative(direction);
                    if (!checked.contains(neighbour) && !closed.contains(neighbour) && !open.contains(neighbour) && passable(neighbour)) {
                        open.add(neighbour);
                    }
                }
            }
        }
        if (closed.size() >= AREA_LIMIT || open.isEmpty()) {
            recheck = false;
            boxes = merge(closed);
            open.clear();
            checked.clear();
        }
    }

    private boolean passable(BlockPos pos) {
        Level level = getLevel();
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.getCollisionShape(level, pos).isEmpty();
    }

    private boolean overWater(BlockPos pos) {
        Level level = getLevel();
        for (int y = worldPosition.getY() - 6; y < worldPosition.getY() + 6; y++) {
            if (level.getFluidState(new BlockPos(pos.getX(), y, pos.getZ())).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    public static List<AABB> merge(Set<BlockPos> cells) {
        List<BlockPos> order = new ArrayList<>(cells);
        order.sort(Comparator.<BlockPos>comparingInt(BlockPos::getY).thenComparingInt(BlockPos::getZ).thenComparingInt(BlockPos::getX));
        Set<BlockPos> used = new HashSet<>();
        List<AABB> result = new ArrayList<>();
        for (BlockPos start : order) {
            if (used.contains(start)) {
                continue;
            }
            int maxX = start.getX() + 1;
            while (maxX < start.getX() + MAX_BOX && free(cells, used, maxX, start.getY(), start.getZ())) {
                maxX++;
            }
            int maxZ = start.getZ() + 1;
            while (maxZ < start.getZ() + MAX_BOX && row(cells, used, start.getX(), maxX, start.getY(), maxZ)) {
                maxZ++;
            }
            int maxY = start.getY() + 1;
            while (maxY < start.getY() + MAX_BOX && layer(cells, used, start.getX(), maxX, maxY, start.getZ(), maxZ)) {
                maxY++;
            }
            for (int y = start.getY(); y < maxY; y++) {
                for (int z = start.getZ(); z < maxZ; z++) {
                    for (int x = start.getX(); x < maxX; x++) {
                        used.add(new BlockPos(x, y, z));
                    }
                }
            }
            result.add(new AABB(start.getX(), start.getY(), start.getZ(), maxX, maxY, maxZ));
        }
        return List.copyOf(result);
    }

    private static boolean free(Set<BlockPos> cells, Set<BlockPos> used, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return cells.contains(pos) && !used.contains(pos);
    }

    private static boolean row(Set<BlockPos> cells, Set<BlockPos> used, int minX, int maxX, int y, int z) {
        for (int x = minX; x < maxX; x++) {
            if (!free(cells, used, x, y, z)) {
                return false;
            }
        }
        return true;
    }

    private static boolean layer(Set<BlockPos> cells, Set<BlockPos> used, int minX, int maxX, int y, int minZ, int maxZ) {
        for (int z = minZ; z < maxZ; z++) {
            if (!row(cells, used, minX, maxX, y, z)) {
                return false;
            }
        }
        return true;
    }

    public boolean steams(Player player) {
        return tick > 0 && contains(player);
    }

    private boolean contains(Player player) {
        AABB bounds = player.getBoundingBox();
        for (AABB box : boxes) {
            if (box.intersects(bounds)) {
                return true;
            }
        }
        return false;
    }

    private static Map<Integer, SaunaStoveBlockEntity> targets(Level level) {
        return level.isClientSide() ? CLIENT_TARGETS : SERVER_TARGETS;
    }

    public static @Nullable SaunaStoveBlockEntity steaming(Player player) {
        Map<Integer, SaunaStoveBlockEntity> targets = targets(player.level());
        SaunaStoveBlockEntity stove = targets.get(player.getId());
        if (stove == null) {
            return null;
        }
        if (!stove.isRemoved() && stove.getLevel() == player.level() && stove.steams(player)) {
            return stove;
        }
        targets.remove(player.getId());
        return null;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tick = input.getIntOr("tick", 0);
        tank.setFluid(input.read("Fluid", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("tick", tick);
        output.store("Fluid", FluidStack.OPTIONAL_CODEC, tank.getFluid());
    }
}
