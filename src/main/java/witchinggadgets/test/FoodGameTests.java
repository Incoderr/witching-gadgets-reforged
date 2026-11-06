package witchinggadgets.test;

import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import witchinggadgets.food.FoodFeature;

public final class FoodGameTests {
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("food")
        .add("shears_and_food", FoodGameTests::items);

    private FoodGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void items(GameTestHelper helper) {
        ItemStack shears = new ItemStack(FoodFeature.THAUMIUM_SHEARS.get());
        helper.assertValueEqual(shears.getMaxDamage(), 400, "shears durability");
        helper.assertTrue(shears.getDestroySpeed(Blocks.COBWEB.defaultBlockState()) > 1.0F, "shears do not cut cobwebs");
        helper.assertTrue(new ItemStack(FoodFeature.SWEETWART.get()).has(DataComponents.FOOD), "sweetwart is not food");
        helper.assertTrue(new ItemStack(FoodFeature.NETHERCAKE.get()).has(DataComponents.FOOD), "nethercake is not food");
        helper.assertTrue(new ItemStack(FoodFeature.BRAIN_JERKY.get()).has(DataComponents.FOOD), "brain jerky is not food");
        helper.succeed();
    }
}
