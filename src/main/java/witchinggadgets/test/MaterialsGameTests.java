package witchinggadgets.test;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistry;
import witchinggadgets.WitchingGadgets;
import witchinggadgets.materials.MaterialsFeature;

public final class MaterialsGameTests {
    private static final WgGameTestSuite SUITE = new WgGameTestSuite("materials")
        .add("tools_and_photos_do_not_stack", MaterialsGameTests::toolsAndPhotosDoNotStack)
        .add("void_metal_block_recipes_exist", MaterialsGameTests::voidMetalBlockRecipesExist)
        .add("materials_have_wg_aspects", MaterialsGameTests::materialsHaveWgAspects);

    private MaterialsGameTests() {
    }

    public static void register(IEventBus modEventBus) {
        SUITE.register(modEventBus);
    }

    private static void toolsAndPhotosDoNotStack(GameTestHelper helper) {
        helper.assertValueEqual(new ItemStack(MaterialsFeature.ARCANE_ABACUS.get()).getMaxStackSize(), 1, "abacus stack size");
        helper.assertValueEqual(new ItemStack(MaterialsFeature.GEMCUTTING_TOOLS.get()).getMaxStackSize(), 1, "gemcutting tools stack size");
        helper.assertValueEqual(new ItemStack(MaterialsFeature.DEVELOPED_PHOTO.get()).getMaxStackSize(), 1, "developed photo stack size");
        helper.assertValueEqual(new ItemStack(MaterialsFeature.GUIDING_STRING.get()).getMaxStackSize(), 1, "guiding string stack size");
        helper.assertValueEqual(new ItemStack(MaterialsFeature.SIMPLE_THREAD.get()).getMaxStackSize(), 64, "yarn stack size");
        helper.succeed();
    }

    private static void voidMetalBlockRecipesExist(GameTestHelper helper) {
        var recipes = helper.getLevel().getServer().getRecipeManager();
        for (String name : new String[] {"void_metal_block", "void_metal_ingot_from_block"}) {
            helper.assertTrue(recipes.byKey(ResourceKey.create(Registries.RECIPE, WitchingGadgets.id(name))).isPresent(), "recipe " + name + " is missing");
        }
        helper.succeed();
    }

    private static void materialsHaveWgAspects(GameTestHelper helper) {
        AspectList yarn = AspectRegistry.active().getObjectTags(MaterialsFeature.SIMPLE_THREAD.get());
        helper.assertTrue(yarn != null && yarn.getAmount(Aspect.CLOTH) == 1 && yarn.size() == 1, "yarn aspects are " + yarn);
        AspectList fleece = AspectRegistry.active().getObjectTags(MaterialsFeature.BEWITCHED_CLOTH.get());
        helper.assertTrue(fleece != null && fleece.getAmount(Aspect.CLOTH) == 3 && fleece.getAmount(Aspect.MAGIC) == 2 && fleece.getAmount(Aspect.TAINT) == 1, "bewitched fleece aspects are " + fleece);
        helper.succeed();
    }
}
