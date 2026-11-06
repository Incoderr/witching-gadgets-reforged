package witchinggadgets.test;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.TcRecipes;
import witchinggadgets.terraform.TerraformFeature;
import witchinggadgets.terraform.TerraformFocus;
import witchinggadgets.terraform.TerraformerBlockEntity;

public final class TerraformGameTests {
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("terraform")
        .add("recipes_registered", TerraformGameTests::recipes)
        .add("focus_sets_suction", TerraformGameTests::suction);

    private TerraformGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void recipes(GameTestHelper helper) {
        for (String key : new String[] {"TERRAFORMER", "TERRAFORMER_PLAINS", "TERRAFORMFOCUS_COLDTAIGA", "TERRAFORMFOCUS_DESERT", "TERRAFORMFOCUS_JUNGLE", "TERRAFORMFOCUS_HELL", "TERRAFORMFOCUS_MUSHROOM", "TERRAFORMFOCUS_TAINT"}) {
            helper.assertTrue(TcRecipes.get(key) != null, "recipe " + key + " is missing");
        }
        helper.succeed();
    }

    private static void suction(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, TerraformFeature.TERRAFORMER.get());
        TerraformerBlockEntity terraformer = (TerraformerBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        terraformer.serverTick();
        helper.assertTrue(terraformer.suction() == null, "suction without a focus");
        helper.setBlock(pos.above(), TerraformFeature.FOCI.get(TerraformFocus.DESERT).get());
        terraformer.serverTick();
        helper.assertTrue(terraformer.suction() == Aspect.FIRE, "desert focus suction");
        helper.assertTrue(terraformer.getSuctionAmount(net.minecraft.core.Direction.DOWN) == 128, "suction amount");
        terraformer.addEssentia(Aspect.FIRE, 4);
        helper.assertTrue(!terraformer.transform(helper.getLevel(), TerraformFocus.DESERT) || terraformer.essentia().getAmount(Aspect.FIRE) == 0, "essentia not spent");
        helper.succeed();
    }
}
