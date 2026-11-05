package witchinggadgets.test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.crafting.TcRecipes;
import witchinggadgets.alchemy.AlchemyFeature;
import witchinggadgets.alchemy.RoseVineBlock;
import witchinggadgets.devices.DevicesFeature;
import witchinggadgets.devices.EssentiaPumpBlock;
import witchinggadgets.devices.EssentiaPumpBlockEntity;

public final class AlchemyGameTests {
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("alchemy")
        .add("recipes_registered", AlchemyGameTests::recipes)
        .add("rose_vine_grows_and_climbs", AlchemyGameTests::vine)
        .add("rose_vine_hurts", AlchemyGameTests::thorns)
        .add("pump_takes_nothing_without_suction", AlchemyGameTests::pump);

    private AlchemyGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void recipes(GameTestHelper helper) {
        for (String key : new String[] {"ROSEVINE", "ALCHEMICALTRANSMOGRIFY_GRASS", "ALCHEMICALTRANSMOGRIFY_MYCEL", "ALCHEMICALTRANSMOGRIFY_SAND", "ALCHEMICALTRANSMOGRIFY_FLINT", "PURECINNABAR", "MIRRORPUMP"}) {
            helper.assertTrue(TcRecipes.get(key) != null, "recipe " + key + " is missing");
        }
        helper.succeed();
    }

    private static void vine(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.setBlock(pos.north(), Blocks.STONE);
        helper.setBlock(pos.north().above(), Blocks.STONE);
        RoseVineBlock block = AlchemyFeature.ROSE_VINE.get();
        BlockPos absolute = helper.absolutePos(pos);
        helper.getLevel().setBlockAndUpdate(absolute, block.supported(block.defaultBlockState(), helper.getLevel(), absolute));
        BlockState state = helper.getBlockState(pos);
        helper.assertTrue(state.getValue(RoseVineBlock.DOWN) && state.getValue(RoseVineBlock.NORTH), "vine supports " + state);
        helper.assertFalse(state.getValue(RoseVineBlock.SOUTH), "south support without a wall");
        RandomSource random = RandomSource.create(1L);
        for (int step = 0; step < 5; step++) {
            block.spread(helper.getLevel(), absolute, helper.getLevel().getBlockState(absolute), random, true);
        }
        helper.assertValueEqual(helper.getBlockState(pos).getValue(RoseVineBlock.AGE), 5, "grown age");
        helper.assertTrue(helper.getBlockState(pos.above()).isAir(), "climbed before maturity");
        block.spread(helper.getLevel(), absolute, helper.getLevel().getBlockState(absolute), random, true);
        BlockState above = helper.getBlockState(pos.above());
        helper.assertTrue(above.is(block) && above.getValue(RoseVineBlock.AGE) == 0 && above.getValue(RoseVineBlock.NORTH), "vine did not climb the wall");
        helper.succeed();
    }

    private static void thorns(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, AlchemyFeature.ROSE_VINE.get());
        Zombie zombie = helper.spawn(EntityTypes.ZOMBIE, new Vec3(2.5, 1.0, 2.5));
        float health = zombie.getHealth();
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(zombie.getHealth() < health, "thorns did not hurt");
            helper.succeed();
        });
    }

    private static void pump(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, DevicesFeature.ESSENTIA_PUMP.get().defaultBlockState().setValue(EssentiaPumpBlock.FACING, Direction.EAST));
        EssentiaPumpBlockEntity pump = (EssentiaPumpBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        helper.assertTrue(pump != null && pump.facing() == Direction.EAST, "pump facing");
        pump.serverTick();
        helper.assertValueEqual(pump.amount(), 0, "pump pulled without suction");
        helper.assertTrue(pump.canOutputTo(Direction.EAST) && !pump.canOutputTo(Direction.WEST) && !pump.canInputFrom(Direction.EAST), "pump sides");
        helper.succeed();
    }
}
