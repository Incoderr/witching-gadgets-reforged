package witchinggadgets.test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.registry.ModItems;
import witchinggadgets.furnace.BlastFurnaceRecipe;
import witchinggadgets.furnace.BlastFurnaceStructure;
import witchinggadgets.furnace.FurnaceFeature;
import witchinggadgets.furnace.InfernalBlastFurnaceBlock;
import witchinggadgets.furnace.InfernalBlastFurnaceBlockEntity;

public final class FurnaceGameTests {
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("furnace")
        .add("recipes_registered", FurnaceGameTests::recipes)
        .add("structure_forms_and_triples_clusters", FurnaceGameTests::smelt)
        .add("breaking_restores_the_structure", FurnaceGameTests::disassemble);

    private FurnaceGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void recipes(GameTestHelper helper) {
        helper.assertTrue(TcRecipes.get(FurnaceFeature.RESEARCH) != null, "compound recipe is missing");
        BlastFurnaceRecipe recipe = BlastFurnaceRecipe.forInput(new ItemStack(ModItems.NATIVE_IRON_CLUSTER.get()));
        helper.assertTrue(recipe != null, "iron cluster recipe is missing");
        helper.assertTrue(recipe.result().is(Items.IRON_INGOT) && recipe.result().getCount() == 3, "iron cluster result");
        helper.assertTrue(BlastFurnaceRecipe.forInput(new ItemStack(Items.CHICKEN)) == null, "chicken melts");
        helper.succeed();
    }

    private static BlockPos build(GameTestHelper helper) {
        BlockPos master = helper.absolutePos(new BlockPos(3, 1, 3));
        for (int yy = 0; yy <= 2; yy++) {
            for (int xx = -1; xx <= 1; xx++) {
                for (int zz = -1; zz <= 1; zz++) {
                    helper.getLevel().setBlock(master.offset(xx, yy, zz), BlastFurnaceStructure.original(xx, yy, zz), 3);
                }
            }
        }
        return master;
    }

    private static void smelt(GameTestHelper helper) {
        BlockPos master = build(helper);
        BlockPos found = BlastFurnaceStructure.findMaster(helper.getLevel(), master.offset(1, 0, 1));
        helper.assertTrue(master.equals(found), "structure not recognised: " + found);
        BlastFurnaceStructure.form(helper.getLevel(), master, Direction.SOUTH);
        helper.assertTrue(helper.getLevel().getBlockState(master.above(2)).getValue(InfernalBlastFurnaceBlock.POSITION) == InfernalBlastFurnaceBlock.LAVA, "lava position");
        InfernalBlastFurnaceBlockEntity furnace = (InfernalBlastFurnaceBlockEntity) helper.getLevel().getBlockEntity(master);
        furnace.addInput(new ItemStack(ModItems.NATIVE_IRON_CLUSTER.get()));
        for (int tick = 0; tick < 460; tick++) {
            furnace.serverTick();
        }
        int ingots = 0;
        for (ItemEntity item : helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(master).inflate(4))) {
            if (item.getItem().is(Items.IRON_INGOT)) {
                ingots += item.getItem().getCount();
            }
        }
        helper.assertValueEqual(ingots, 3, "tripled ingots");
        helper.assertTrue(furnace.inputs().isEmpty(), "cluster left in the furnace");
        helper.succeed();
    }

    private static void disassemble(GameTestHelper helper) {
        BlockPos master = build(helper);
        BlastFurnaceStructure.form(helper.getLevel(), master, Direction.NORTH);
        BlockPos corner = master.offset(-1, 1, -1);
        helper.getLevel().removeBlock(corner, false);
        helper.assertTrue(helper.getLevel().getBlockState(master).is(Blocks.OBSIDIAN), "master not restored");
        helper.assertTrue(helper.getLevel().getBlockState(master.above(2)).is(Blocks.LAVA), "lava not restored");
        helper.assertTrue(helper.getLevel().getBlockState(master.offset(1, 2, 0)).is(Blocks.NETHER_BRICK_STAIRS), "stair not restored");
        helper.assertTrue(helper.getLevel().getBlockState(corner).isAir(), "broken block came back");
        helper.succeed();
    }
}
