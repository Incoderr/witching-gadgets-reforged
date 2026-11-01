package witchinggadgets.test;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.crafting.TcRecipes;
import witchinggadgets.materials.MaterialsFeature;
import witchinggadgets.spinning.SpinningFeature;
import witchinggadgets.spinning.SpinningRecipe;
import witchinggadgets.spinning.SpinningWheelBlock;
import witchinggadgets.spinning.SpinningWheelBlockEntity;

public final class SpinningGameTests {
    private static final BlockPos WHEEL = new BlockPos(1, 1, 1);
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("spinning")
        .add("recipes_match_in_any_order", SpinningGameTests::recipesMatchInAnyOrder)
        .add("wheel_spins_yarn_after_full_progress", SpinningGameTests::wheelSpinsYarn)
        .add("wheel_waits_for_a_free_output", SpinningGameTests::wheelWaitsForFreeOutput)
        .add("wheel_has_an_arcane_recipe", SpinningGameTests::wheelHasArcaneRecipe);

    private SpinningGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void recipesMatchInAnyOrder(GameTestHelper helper) {
        ItemStack string = new ItemStack(Items.STRING);
        ItemStack gold = new ItemStack(Items.GOLD_NUGGET);
        SpinningRecipe golden = SpinningRecipe.find(List.of(gold, string, ItemStack.EMPTY, gold, string));
        helper.assertTrue(golden != null && golden.output().create().is(MaterialsFeature.GOLDEN_THREAD.get()), "golden thread was not found");
        helper.assertValueEqual(golden.output().count(), 2, "golden thread count");
        helper.assertTrue(SpinningRecipe.find(List.of(string, string, string)) == null, "three strings must not spin");
        helper.assertTrue(SpinningRecipe.find(List.of(string, string, string, string, string)) == null, "five strings must not spin");
        helper.succeed();
    }

    private static SpinningWheelBlockEntity placeWheel(GameTestHelper helper) {
        helper.setBlock(WHEEL, SpinningFeature.SPINNING_WHEEL.get().defaultBlockState().setValue(SpinningWheelBlock.FACING, Direction.NORTH));
        return (SpinningWheelBlockEntity) helper.getBlockEntity(WHEEL, SpinningWheelBlockEntity.class);
    }

    private static void wheelSpinsYarn(GameTestHelper helper) {
        SpinningWheelBlockEntity wheel = placeWheel(helper);
        for (int slot = 0; slot < 4; slot++) {
            wheel.setItem(slot, new ItemStack(Items.STRING, 2));
        }
        for (int tick = 0; tick < SpinningWheelBlockEntity.MAX_PROGRESS; tick++) {
            wheel.serverTick();
        }
        helper.assertTrue(wheel.getItem(SpinningWheelBlockEntity.OUTPUT).isEmpty(), "yarn appeared before the progress was full");
        helper.assertValueEqual(wheel.progress(), SpinningWheelBlockEntity.MAX_PROGRESS, "progress before the craft");
        wheel.serverTick();
        ItemStack output = wheel.getItem(SpinningWheelBlockEntity.OUTPUT);
        helper.assertTrue(output.is(MaterialsFeature.SIMPLE_THREAD.get()) && output.getCount() == 2, "expected 2 yarn but was " + output);
        helper.assertValueEqual(wheel.getItem(0).getCount(), 1, "string left in slot 0");
        helper.assertValueEqual(wheel.progress(), 0, "progress after the craft");
        helper.succeed();
    }

    private static void wheelWaitsForFreeOutput(GameTestHelper helper) {
        SpinningWheelBlockEntity wheel = placeWheel(helper);
        for (int slot = 0; slot < 4; slot++) {
            wheel.setItem(slot, new ItemStack(Items.STRING));
        }
        wheel.setItem(SpinningWheelBlockEntity.OUTPUT, new ItemStack(Items.STICK));
        for (int tick = 0; tick < 10; tick++) {
            wheel.serverTick();
        }
        helper.assertValueEqual(wheel.progress(), 0, "progress with a blocked output");
        wheel.setItem(SpinningWheelBlockEntity.OUTPUT, new ItemStack(MaterialsFeature.SIMPLE_THREAD.get(), 63));
        wheel.serverTick();
        helper.assertValueEqual(wheel.progress(), 0, "progress with a full output");
        helper.succeed();
    }

    private static void wheelHasArcaneRecipe(GameTestHelper helper) {
        helper.assertTrue(TcRecipes.get(SpinningFeature.RESEARCH) != null, "arcane recipe " + SpinningFeature.RESEARCH + " is missing");
        helper.succeed();
    }
}
