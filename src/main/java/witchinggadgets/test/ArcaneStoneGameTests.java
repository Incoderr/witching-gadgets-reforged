package witchinggadgets.test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.crafting.TcRecipes;
import witchinggadgets.arcanestone.AgeingStoneBlock;
import witchinggadgets.arcanestone.ArcaneStoneFeature;
import witchinggadgets.arcanestone.EtherealWallBlock;
import witchinggadgets.arcanestone.EtherealWallBlockEntity;
import witchinggadgets.arcanestone.StoneExtruderBlock;

public final class ArcaneStoneGameTests {
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("arcane_stone")
        .add("recipes_registered", ArcaneStoneGameTests::recipes)
        .add("ethereal_wall_network_turns_passable", ArcaneStoneGameTests::etherealNetwork)
        .add("ethereal_wall_takes_a_disguise", ArcaneStoneGameTests::etherealDisguise)
        .add("ageing_stone_grows_babies", ArcaneStoneGameTests::ageing)
        .add("stone_extruder_fills_a_chest", ArcaneStoneGameTests::extruder);

    private ArcaneStoneGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void recipes(GameTestHelper helper) {
        for (String key : new String[] {"ETHEREALWALL", "AGEINGSTONE", "STONEEXTRUDER"}) {
            helper.assertTrue(TcRecipes.get(key) != null, "recipe " + key + " is missing");
        }
        helper.succeed();
    }

    private static void etherealNetwork(GameTestHelper helper) {
        BlockPos first = new BlockPos(1, 2, 1);
        for (int x = 1; x <= 3; x++) {
            helper.setBlock(new BlockPos(x, 2, 1), ArcaneStoneFeature.ETHEREAL_WALL.get());
        }
        BlockPos last = new BlockPos(3, 2, 1);
        helper.assertFalse(helper.getBlockState(last).getValue(EtherealWallBlock.POWERED), "wall powered without signal");
        helper.setBlock(first.west(), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(2, () -> {
            BlockState state = helper.getBlockState(last);
            helper.assertTrue(state.getValue(EtherealWallBlock.POWERED), "far wall of the network not powered");
            helper.assertTrue(state.getCollisionShape(helper.getLevel(), helper.absolutePos(last), CollisionContext.empty()).isEmpty(), "powered wall is still solid");
            helper.setBlock(first.west(), Blocks.AIR);
            helper.runAfterDelay(2, () -> {
                helper.assertFalse(helper.getBlockState(last).getValue(EtherealWallBlock.POWERED), "wall stayed powered");
                helper.succeed();
            });
        });
    }

    private static void etherealDisguise(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, ArcaneStoneFeature.ETHEREAL_WALL.get());
        EtherealWallBlockEntity wall = (EtherealWallBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        helper.assertTrue(EtherealWallBlock.canDisguiseAs(Blocks.BRICKS.defaultBlockState(), helper.getLevel(), helper.absolutePos(pos)), "bricks rejected");
        helper.assertFalse(EtherealWallBlock.canDisguiseAs(Blocks.CHEST.defaultBlockState(), helper.getLevel(), helper.absolutePos(pos)), "chest accepted");
        helper.assertFalse(EtherealWallBlock.canDisguiseAs(Blocks.TORCH.defaultBlockState(), helper.getLevel(), helper.absolutePos(pos)), "torch accepted");
        wall.setDisguise(Blocks.GLOWSTONE.defaultBlockState());
        helper.assertValueEqual(helper.getBlockState(pos).getLightEmission(helper.getLevel(), helper.absolutePos(pos)), 15, "disguise light");
        helper.succeed();
    }

    private static void ageing(GameTestHelper helper) {
        BlockPos stone = new BlockPos(2, 2, 2);
        helper.setBlock(stone, ArcaneStoneFeature.AGEING_STONE.get());
        Sheep lamb = helper.spawnWithNoFreeWill(EntityType.SHEEP, new BlockPos(3, 2, 2));
        lamb.setAge(-100);
        AgeingStoneBlock.ageNearby(helper.getLevel(), helper.absolutePos(stone));
        helper.assertValueEqual(lamb.getAge(), -99, "lamb age after one tick");
        lamb.discard();
        helper.succeed();
    }

    private static void extruder(GameTestHelper helper) {
        BlockPos machine = new BlockPos(1, 2, 1);
        BlockPos chest = new BlockPos(1, 2, 2);
        helper.setBlock(chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH));
        helper.setBlock(machine, ArcaneStoneFeature.STONE_EXTRUDER.get().defaultBlockState().setValue(StoneExtruderBlock.FACING, Direction.SOUTH));
        helper.runAfterDelay(45, () -> {
            ChestBlockEntity container = (ChestBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(chest));
            helper.assertTrue(container.countItem(Items.COBBLESTONE) >= 1, "no cobblestone in the chest");
            helper.succeed();
        });
    }
}
